package org.hermes.ledgers


import android.content.SharedPreferences
import android.util.Log
import java.lang.Exception
import java.lang.StringBuilder
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.*
import org.apache.commons.lang3.StringUtils
import org.hermes.BACKGROUND
import org.hermes.HermesClientApp
import org.hermes.HermesRoomDatabase
import org.iota.jota.IotaAPI
import org.iota.jota.model.Bundle
import org.iota.jota.model.Transaction
import org.iota.jota.utils.Constants
import org.iota.jota.utils.InputValidator
import org.hermes.crypto.SecP256K1PrivKey
import org.hermes.entities.Event
import org.hermes.IOTA
import org.hermes.Metric20
import org.hermes.bip44.iota.Seed
import org.hermes.repositories.MetadataRepository
import org.hermes.repositories.SensorRepository
import org.hermes.ternary.toTrytes
import org.hermes.utils.SQLiteTypeConverter
import org.hermes.utils.prepend
import org.hermes.utils.sign
import org.hermes.utils.splitInChunks
import org.threeten.bp.OffsetDateTime

class IOTAConnector(val seed: Seed, private val privateKey: SecP256K1PrivKey, app: HermesClientApp) {

    val loggingTag: String = "IOTAConnector"

    val EMPTY_TAG: String = StringUtils.rightPad("", 27, "9")

    @Inject
    lateinit var api: IotaAPI

    @Inject
    lateinit var db: HermesRoomDatabase

    @Inject
    lateinit var metadataRepository: MetadataRepository

    @Inject
    lateinit var sensorRepository: SensorRepository

    @Inject
    lateinit var addressManager: IOTAAddressManager

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
            .sign(header = "digest:", privateKey = privateKey, separator = "::")

        Log.d(loggingTag, "$clientUUID -- Cleartext payload: $clearTextPayload")
        Log.d(loggingTag, "$clientUUID -- Cleartext payload size: ${clearTextPayload.length}")

        return clearTextPayload
            .toTrytes()
            .splitInChunks(IOTA.Transaction.SIGNATURE_MESSAGE_FRAGMENT)
            .map { StringUtils.rightPad(it, Constants.MESSAGE_LENGTH, '9') }
            .toMutableList()
    }

    fun sendData(samples: Array<Metric20?>, clientUUID: String, asyncConfirmation: Boolean,
        blockUntilConfirmation: Boolean
    ): Array<Metric20?> {
        // Validate seed
        if (!InputValidator.isValidSeed(seed.toString())) {
            Log.e(loggingTag, "The seed loaded to the service is incorrect!")
            return samples
        }
        if (addressManager.hasUnconfirmedAddressFor(clientUUID)) {
            Log.d(loggingTag, "There is another  broadcast already in progress for $clientUUID. Returning.")
            return samples
        }
        Log.d(loggingTag, "Broadcasting data of client with id $clientUUID at " +
                "${SQLiteTypeConverter.fromOffsetDateTime(OffsetDateTime.now())}")
        val (previousAddress, newAddress, nextAddress) = addressManager.getAddress(clientUUID)
        val trytes = prepareTransactions(previousAddress, newAddress, nextAddress, clientUUID, *samples)

        var i = 1
        while (i <= 3 && !broadcastBundle(clientUUID, trytes, newAddress, i, samples.size, 3)) {
            if (i == 3) {
                Log.e(loggingTag, "Could not broadcast transactions. Aborting!")
                metadataRepository.eventBus.sendMessage(metadataRepository.eventBus.obtainMessage().apply{
                    obj = Pair(MetadataRepository.MessageType.NOTIFY_FAILED_BROADCAST, null) })
                addressManager.failBroadcast(clientUUID)
                return samples
            }
            Thread.sleep(10 * 1000)
            i += 1
        }

        if (asyncConfirmation) {
            if (blockUntilConfirmation) runBlocking {
                checkResultOfTransactions(arrayOf(newAddress), clientUUID, samples.size)
            }
            else CoroutineScope(BACKGROUND.asCoroutineDispatcher())
                .launch { checkResultOfTransactions(arrayOf(newAddress), clientUUID, samples.size) }
        }
        return Array(0) { null }
    }

    private fun prepareTransactions(previousAddress: String, newAddress: String, nextAddress: String,
                                    clientUUID: String, vararg samples: Metric20?): Array<String> {
        val header = StringBuilder()
            .append(nextAddress)
            .append("::")
            .append(previousAddress)
            .append("::")
            .toString()

        val carbon20SignatureFragments = samplesToTrytes(*samples, clientUUID = clientUUID, header = header)
        // Create empty transactions that will form the bundle. The number of transactions must be equal to the
        // number of chunks returned by the samplesToTrytes method.
        val carbon20Transactions = (0 until carbon20SignatureFragments.size)
            .map{ Transaction(newAddress, 0, EMPTY_TAG, OffsetDateTime.now().toEpochSecond()) }
            .toMutableList()
        Log.d(loggingTag, "$clientUUID -- Bundle will contain ${carbon20SignatureFragments.size} transactions")

        val bundle = Bundle(carbon20Transactions, carbon20Transactions.size).apply {
            this.finalize(null)
            this.addTrytes(carbon20SignatureFragments)
        }
        return bundle.transactions.map { it.toTrytes() }.reversed().toTypedArray()
    }

    private fun broadcastBundle(clientUUID: String, bundleTrytes: Array<String>, address: String, currentTry: Int,
                                sampleNum: Int, maxTries: Int): Boolean {
        val depth = 3
        val minWeightMagnitude = 14

        val eventMessage = StringBuilder()
            .append("$clientUUID -- ")
            .append(address)
            .append(" -- Broadcasting ")
            .append(bundleTrytes.size)
            .append(" transaction" + (if (bundleTrytes.size > 1) "s" else ""))
            .append(" ($currentTry/$maxTries tries)")
            .toString()

        Log.i(loggingTag, eventMessage)
        try {
            Log.d(loggingTag, "Attempting to broadcast transactions to address $address")
            api.sendTrytes(bundleTrytes, depth, minWeightMagnitude, null)
            Log.d(loggingTag, "Broadcast to address $address succeeded")
            metadataRepository.eventBus.sendMessage(metadataRepository.eventBus.obtainMessage().apply{
                obj = Pair(MetadataRepository.MessageType.PACKETS_BROADCAST, sampleNum) })
        } catch (e: Throwable) {
            Log.e(loggingTag, StringBuilder()
                .append("$clientUUID -- ")
                .append(address)
                .append(" -- Broadcasting failed: ")
                .append(e.message)
                .append(" ($currentTry/$maxTries tries)")
                .toString())
            db.eventDao().insertAll(Event(action = "broadcast failure ($currentTry/$maxTries)", resource = "iota",
                extraInfo = "There was an error while trying to broadcast packets to the Tangle: ${e.message}"))
            return false
        }

        val event = Event(action = "broadcast", resource = "iota", extraInfo = eventMessage)
        db.eventDao().insertAll(event)
        return true
    }

    private suspend fun checkResultOfTransactions(trxs: Array<String>, clientUUID: String,
                                                  packetsBroadcast: Int) {
        val txsAddressesStr = trxs.joinToString()

        for (i in 1..3) {
            delay(5 * 1000)

            Log.d(loggingTag, "$clientUUID -- Starting IOTA API call $i/3 for addresses: $txsAddressesStr.")
            val unsuccessfulMsg = "$clientUUID -- IOTA API call $i/3 was unsuccessful for addresses: $txsAddressesStr"
            val fetchedTxs: List<Transaction>
            try {
                fetchedTxs = api.findTransactionObjectsByAddresses(trxs)
            } catch (e: Throwable) {
                Log.d(loggingTag, unsuccessfulMsg + " because of ${e.message}")
                continue
            }

            val successfulTxs = fetchedTxs.filter { it.hash.isNotEmpty() }

            if (successfulTxs.isNotEmpty()) {
                val eventMessage = StringBuilder()
                    .append("$clientUUID -- Broadcast was successful for bundle: ")
                    .append(fetchedTxs.first().bundle)
                    .append(" and txs: ")
                    .append(txsAddressesStr)

                val event = Event(action = "confirm attach", resource = "iota", extraInfo = eventMessage.toString())
                db.eventDao().insertAll(event)
                metadataRepository.eventBus.sendMessage(metadataRepository.eventBus.obtainMessage().apply{
                    obj = Pair(MetadataRepository.MessageType.PACKETS_CONFIRMED, packetsBroadcast) })
                try {
                    addressManager.confirmAddress(clientUUID)
                } catch (e: UnknownClient) {
                    Log.e(loggingTag, "There seems to be no address for client: $clientUUID")
                }
                Log.i(loggingTag, eventMessage.toString())
                return
            }
            Log.d(loggingTag, unsuccessfulMsg)
        }
        val eventMessage = StringBuilder()
            .append("$clientUUID -- Broadcast was unsuccessful for txs: ")
            .append(txsAddressesStr)
        Log.i(loggingTag, eventMessage.toString())
    }
}