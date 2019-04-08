package org.hermes.ledgers


import android.util.Log
import java.lang.Exception
import java.lang.StringBuilder
import jota.IotaAPI
import jota.model.Bundle
import jota.model.Transaction
import jota.utils.*
import kotlinx.coroutines.*
import org.apache.commons.lang3.StringUtils
import org.threeten.bp.OffsetDateTime

import org.hermes.BACKGROUND
import org.hermes.HermesRoomDatabase
import org.hermes.iota.IOTA
import org.hermes.iota.Seed
import org.hermes.Metric20
import org.hermes.entities.Event
import org.hermes.utils.splitInChunks
import org.hermes.utils.stripPaddingOfTrytes
import org.hermes.utils.toTrytes


class IOTAConnector(val protocol: String, val uri: String, val port: String, val seed: Seed,
                    val db: HermesRoomDatabase?) {

    val loggingTag: String = "IOTAConnector"
    val api: IotaAPI

    val EMPTY_TAG = StringUtils.rightPad("", 27, "9")

    init {
        try {
            api = IotaAPI.Builder()
                .protocol(protocol)
                .host(uri)
                .port(port)
                .build()
        } catch(e: Exception) {
            Log.e(loggingTag, "There was an error while trying to initialize the connection: ${e}")
            throw e
        }
    }

    fun sendData(sample: Metric20, asyncConfirmation: Boolean, blockUntilConfirmation: Boolean) {
        try {
            // Validate seed
            if (!InputValidator.isValidSeed(seed.toString())) {
                Log.e(loggingTag, "The seed loaded to the service is incorrect!")
                return
            }

            // TODO: Save the index of the previous transaction to reduce search time
            Log.i(loggingTag, "Getting address from Tangle")
            val address = api.getNextAvailableAddress(seed.toString(), 2, true).first()
            if (address == null) {
                Log.e(loggingTag, "Could not get a new address for IOTA!")
                val event = Event(action = "broadcast", resource = "iota", extraInfo = "Failed to get an address for IOTA")
                db?.eventDao()?.insertAll(event)
                return
            }

            val normalizedAddress = Checksum.removeChecksum(address)
            val depth = 3
            val minWeightMagnitude = 14
            val timestamp = OffsetDateTime.now().toEpochSecond()

            val carbon20SignatureFragments = sample
                .toCarbon20String()
                .toTrytes()
                .splitInChunks(IOTA.Transaction.SIGNATURE_MESSAGE_FRAGMENT)
                .map { StringUtils.rightPad(it, Constants.MESSAGE_LENGTH, '9') }
                .toMutableList()

            val carbon20Transactions = carbon20SignatureFragments
                .map{
                    Transaction(normalizedAddress, 0, EMPTY_TAG, timestamp)
                }
                .toMutableList()

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
            db?.eventDao()?.insertAll(event)

            Log.i(loggingTag, eventMessage)
            val trxs: List<Transaction> = api.sendTrytes(trxTrytes.toTypedArray(),
                depth, minWeightMagnitude, null)

            if (asyncConfirmation) {
                if (blockUntilConfirmation) {
                    runBlocking { checkResultOfTransactions(trxs) }
                } else {
                    CoroutineScope(BACKGROUND.asCoroutineDispatcher())
                        .launch { checkResultOfTransactions(trxs) }
                }
            }
        } catch (e: Exception) {
            Log.e(loggingTag, "There was an error while trying to broadcast a sample to IOTA: $e")
        }
    }

    private suspend fun checkResultOfTransactions(trxs: List<Transaction>) {
        val txsAddresses = trxs.map { it.address }.toTypedArray()
        val txsAddressesStr = txsAddresses.joinToString()

        val successfulTxs = api.findTransactionObjectsByAddresses(txsAddresses)
            .map { it.hash.isNotEmpty() }

        val allSuccess = successfulTxs.reduce { acc, next -> acc && next }

        val eventMessage = StringBuilder()
            .append("Sending of transactions with addresses: ")
            .append(txsAddressesStr)
            .append(" was ")
            .append(if (allSuccess) "" else "un")
            .append("successful")

        val event = Event(action = "broadcast", resource = "iota", extraInfo = eventMessage.toString())
        db?.eventDao()?.insertAll(event)

        eventMessage
            .append(" with payload: ")
            .append(trxs.map { it.signatureFragments.stripPaddingOfTrytes() }.joinToString())

        Log.i(loggingTag, eventMessage.toString())
    }
}