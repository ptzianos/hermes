package org.hermes.ledgers


import android.util.Log
import java.lang.Exception
import java.lang.StringBuilder
import javax.inject.Inject
import jota.IotaAPI
import jota.model.Bundle
import jota.model.Transaction
import jota.utils.*
import kotlinx.coroutines.*
import org.apache.commons.lang3.StringUtils
import org.hermes.*
import org.threeten.bp.OffsetDateTime

import org.hermes.entities.Event
import org.hermes.iota.IOTA
import org.hermes.iota.Seed
import org.hermes.utils.splitInChunks
import org.hermes.utils.stripPaddingOfTrytes
import org.hermes.utils.toTrytes


class IOTAConnector(val seed: Seed, app: HermesClientApp) {

    val loggingTag: String = "IOTAConnector"

    val EMPTY_TAG = StringUtils.rightPad("", 27, "9")

    @Inject
    lateinit var api: IotaAPI

    @Inject
    lateinit var db: HermesRoomDatabase

    init { app.daggerHermesComponent.inject(this) }

    /**
     * Convert the data into strings separated by `::`, then convert them into trytes
     * and split into chunks of size equal to 2187.
     */
    private fun samplesToTrytes(vararg samples: Metric20?): MutableList<String> {
        if (samples.isEmpty())
            throw Exception("No samples provided")
        return samples
            .filterNotNull()
            .map { it.toCarbon20String() }
            .joinToString(separator = "::")
            .toTrytes()
            .splitInChunks(IOTA.Transaction.SIGNATURE_MESSAGE_FRAGMENT)
            .map { StringUtils.rightPad(it, Constants.MESSAGE_LENGTH, '9') }
            .toMutableList()
    }

    fun sendData(vararg samples: Metric20?, asyncConfirmation: Boolean, blockUntilConfirmation: Boolean) {
        try {
            // Validate seed
            if (!InputValidator.isValidSeed(seed.toString())) {
                Log.e(loggingTag, "The seed loaded to the service is incorrect!")
                return
            }

            // TODO: Save the index of the previous transaction to reduce search time
//            val newAddress = IotaAPIUtils.newAddress(seed, security, i, checksum, customCurl.clone())
            Log.i(loggingTag, "Getting address from Tangle")
            val address = api.getNextAvailableAddress(seed.toString(), 2, true).first()
            if (address == null) {
                Log.e(loggingTag, "Could not get a new address for IOTA!")
                val event = Event(action = "broadcast", resource = "iota", extraInfo = "Failed to get an address for IOTA")
                db.eventDao().insertAll(event)
                return
            }

            val normalizedAddress = Checksum.removeChecksum(address)
            Log.d(loggingTag, "Address that will be used for the next sample broadcast is $normalizedAddress")
            val depth = 3
            val minWeightMagnitude = 14
            val timestamp = OffsetDateTime.now().toEpochSecond()

            val carbon20SignatureFragments = samplesToTrytes(*samples)
            // Create empty transactions that will form the bundle. The number of transactions must be equal to the
            // number of chunks returned by the samplesToTrytes method.
            val carbon20Transactions = (0 until carbon20SignatureFragments.size)
                .map{ Transaction(normalizedAddress, 0, EMPTY_TAG, timestamp) }
                .toMutableList()
            Log.d(loggingTag, "Bundle will contain ${carbon20SignatureFragments.size} transactions")

            val bundle = Bundle(carbon20Transactions, carbon20Transactions.size).apply {
                this.finalize(null)
                this.addTrytes(carbon20SignatureFragments)
            }
            val trxTrytes = bundle.transactions.map { it.toTrytes() }.reversed()

            val eventMessage = StringBuilder()
                .append("Broadcasting of transactions with addresses: ")
                .append(bundle.transactions.map { it.address }.toTypedArray().joinToString())
                .toString()

            val event = Event(action = "broadcast", resource = "iota", extraInfo = eventMessage)
            db.eventDao().insertAll(event)

            Log.i(loggingTag, eventMessage)
            val trxs: List<Transaction> = api.sendTrytes(trxTrytes.toTypedArray(),
                depth, minWeightMagnitude, null)

            if (asyncConfirmation) {
                if (blockUntilConfirmation) runBlocking { checkResultOfTransactions(trxs) }
                else CoroutineScope(BACKGROUND.asCoroutineDispatcher())
                    .launch { checkResultOfTransactions(trxs) }
            }
        } catch (e: Exception) {
            Log.e(loggingTag, "There was an error while trying to broadcast a sample to IOTA: $e")
        }
    }

    private suspend fun checkResultOfTransactions(trxs: List<Transaction>) {
        val txsAddresses = trxs.map { it.address }.toTypedArray()
        val txsAddressesStr = txsAddresses.joinToString()

        for (i in 0 until 3) {
            delay(5 * 1000)

            Log.d(loggingTag, "Starting IOTA API call $i/3 for addresses: $txsAddresses.")

            val fetchedTxs = api.findTransactionObjectsByAddresses(txsAddresses)
            val successfulTxs = fetchedTxs.filter { it.hash.isNotEmpty() }

            if (successfulTxs.isNotEmpty()) {
                val eventMessage = StringBuilder()
                    .append("Broadcast was")
                    .append("successful for bundle: ")
                    .append(fetchedTxs.first().bundle)
                    .append("and txs: ")
                    .append(txsAddressesStr)

                val event = Event(action = "broadcast", resource = "iota", extraInfo = eventMessage.toString())
                db.eventDao().insertAll(event)
                Log.i(loggingTag, eventMessage.toString())
            }
            Log.d(loggingTag, "IOTA API call $i/3 was unsuccessful for addresses: $txsAddresses")
        }
        val eventMessage = StringBuilder()
            .append("Broadcast was")
            .append("unsuccessful for txs: ")
            .append(txsAddressesStr)
        Log.i(loggingTag, eventMessage.toString())
        Log.d(loggingTag, "There was some error while trying to get state of transactions: $trxs. Aborting")
    }
}