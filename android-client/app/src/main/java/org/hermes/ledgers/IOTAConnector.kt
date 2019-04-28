package org.hermes.ledgers


import android.content.SharedPreferences
import android.util.Log
import java.lang.Exception
import java.lang.StringBuilder
import java.security.KeyPair
import javax.inject.Inject
import javax.inject.Named
import jota.IotaAPI
import jota.model.Bundle
import jota.model.Transaction
import jota.pow.SpongeFactory
import jota.utils.*
import kotlinx.coroutines.*
import org.apache.commons.lang3.StringUtils
import org.threeten.bp.OffsetDateTime

import org.hermes.BACKGROUND
import org.hermes.HermesClientApp
import org.hermes.HermesRoomDatabase
import org.hermes.Metric20
import org.hermes.entities.Event
import org.hermes.iota.IOTA
import org.hermes.iota.Seed
import org.hermes.utils.prepend
import org.hermes.utils.sign
import org.hermes.utils.splitInChunks
import org.hermes.utils.toTrytes


class IOTAConnector(val seed: Seed, val keyPair: KeyPair, app: HermesClientApp) {

    val loggingTag: String = "IOTAConnector"

    val EMPTY_TAG = StringUtils.rightPad("", 27, "9")

    @Inject
    lateinit var api: IotaAPI

    @Inject
    lateinit var db: HermesRoomDatabase

    @field:[Inject Named("iota")]
    lateinit var prefs: SharedPreferences

    init { app.daggerHermesComponent.inject(this) }

    /**
     * Convert the data into strings separated by `::`, then convert them into trytes
     * and split into chunks of size equal to 2187.
     */
    private fun samplesToTrytes(vararg samples: Metric20?, clientUUID: String,
                                header: String = ""): MutableList<String> {
        if (samples.isEmpty())
            throw Exception("No samples provided")

        val clearTextPayload = samples
            .mapNotNull { it?.toCarbon20String() }
            .joinToString(separator = "::")
            .prepend(header)
            .sign(header = "digest:", privateKey = keyPair.private, separator = "::")

        Log.d(loggingTag, "$clientUUID -- Cleartext payload: $clearTextPayload")

        return clearTextPayload
            .toTrytes()
            .splitInChunks(IOTA.Transaction.SIGNATURE_MESSAGE_FRAGMENT)
            .map { StringUtils.rightPad(it, Constants.MESSAGE_LENGTH, '9') }
            .toMutableList()
    }

    fun sendData(vararg samples: Metric20?, clientUUID: String, asyncConfirmation: Boolean,
                 blockUntilConfirmation: Boolean) {
        try {
            // Validate seed
            if (!InputValidator.isValidSeed(seed.toString())) {
                Log.e(loggingTag, "The seed loaded to the service is incorrect!")
                return
            }

            val newAddressIndex = prefs.getInt("latest_addr_index", 1000) + 1
            val previousAddress = prefs.getString("latest_addr_used", "")
            Log.d(loggingTag, "$clientUUID -- Next IOTA address index to use is: $newAddressIndex")
            val newAddress = IotaAPIUtils.newAddress(seed.toString(), Seed.DEFAULT_SEED_SECURITY,
                newAddressIndex, false, SpongeFactory.create(SpongeFactory.Mode.KERL))
            val nextAddress = IotaAPIUtils.newAddress(seed.toString(), Seed.DEFAULT_SEED_SECURITY,
                newAddressIndex + 1, false, SpongeFactory.create(SpongeFactory.Mode.KERL))

            // TODO: This should be done only after the transactions have been committed to the ledger
            prefs.edit()
                .putInt("latest_addr_index", newAddressIndex)
                .putString("latest_addr_used", newAddress)
                .apply()

            Log.d(loggingTag,
                "$clientUUID -- Address that will be used for the next sample broadcast is $newAddress")
            val depth = 3
            val minWeightMagnitude = 14
            val timestamp = OffsetDateTime.now().toEpochSecond()

            // TODO: Add better support for multiplexing messages for many sensors streaming at the same time
            val header = StringBuilder()
                .append("next_address:")
                .append(nextAddress)
                .append("::")
                .append("previous_address:")
                .append(previousAddress)
                .append("::")
                .toString()

            val carbon20SignatureFragments = samplesToTrytes(*samples, clientUUID = clientUUID, header = header)
            // Create empty transactions that will form the bundle. The number of transactions must be equal to the
            // number of chunks returned by the samplesToTrytes method.
            val carbon20Transactions = (0 until carbon20SignatureFragments.size)
                .map{ Transaction(newAddress, 0, EMPTY_TAG, timestamp) }
                .toMutableList()
            Log.d(loggingTag, "$clientUUID -- Bundle will contain ${carbon20SignatureFragments.size} transactions")

            val bundle = Bundle(carbon20Transactions, carbon20Transactions.size).apply {
                this.finalize(null)
                this.addTrytes(carbon20SignatureFragments)
            }
            val trxTrytes = bundle.transactions.map { it.toTrytes() }.reversed()

            val eventMessage = StringBuilder()
                .append("$clientUUID -- Broadcasting of transactions with addresses: ")
                .append(bundle.transactions.map { it.address }.toTypedArray().joinToString())
                .toString()

            val event = Event(action = "broadcast", resource = "iota", extraInfo = eventMessage)
            db.eventDao().insertAll(event)

            Log.i(loggingTag, eventMessage)
            val trxs: List<Transaction> = api.sendTrytes(trxTrytes.toTypedArray(),
                depth, minWeightMagnitude, null)

            if (asyncConfirmation) {
                if (blockUntilConfirmation) runBlocking { checkResultOfTransactions(trxs, clientUUID) }
                else CoroutineScope(BACKGROUND.asCoroutineDispatcher())
                    .launch { checkResultOfTransactions(trxs, clientUUID) }
            }
        } catch (e: Exception) {
            Log.e(loggingTag, "$clientUUID -- There was an error while trying to broadcast a sample to IOTA: $e")
        }
    }

    private suspend fun checkResultOfTransactions(trxs: List<Transaction>, clientUUID: String) {
        val txsAddresses = trxs.map { it.address }.toTypedArray()
        val txsAddressesStr = txsAddresses.joinToString()

        for (i in 0 until 3) {
            delay(5 * 1000)

            Log.d(loggingTag, "$clientUUID -- Starting IOTA API call $i/3 for addresses: $txsAddressesStr.")

            val fetchedTxs = api.findTransactionObjectsByAddresses(txsAddresses)
            val successfulTxs = fetchedTxs.filter { it.hash.isNotEmpty() }

            if (successfulTxs.isNotEmpty()) {
                val eventMessage = StringBuilder()
                    .append("$clientUUID -- Broadcast was successful for bundle: ")
                    .append(fetchedTxs.first().bundle)
                    .append(" and txs: ")
                    .append(txsAddressesStr)

                val event = Event(action = "confirm attach", resource = "iota", extraInfo = eventMessage.toString())
                db.eventDao().insertAll(event)
                Log.i(loggingTag, eventMessage.toString())
                return
            }
            Log.d(loggingTag, "$clientUUID -- IOTA API call $i/3 was unsuccessful for addresses: $txsAddresses")
        }
        val eventMessage = StringBuilder()
            .append("$clientUUID -- Broadcast was unsuccessful for txs: ")
            .append(txsAddressesStr)
        Log.i(loggingTag, eventMessage.toString())
    }
}