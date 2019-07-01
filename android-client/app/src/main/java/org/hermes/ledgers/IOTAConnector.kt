package org.hermes.ledgers


import android.content.SharedPreferences
import android.os.Handler
import android.os.Message
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.lang.Exception
import java.lang.StringBuilder
import javax.inject.Inject
import javax.inject.Named
import kotlinx.coroutines.*
import org.apache.commons.lang3.StringUtils
import org.hermes.*
import org.iota.jota.IotaAPI
import org.iota.jota.model.Bundle
import org.iota.jota.model.Transaction
import org.iota.jota.pow.SpongeFactory
import org.iota.jota.utils.Constants
import org.iota.jota.utils.InputValidator
import org.iota.jota.utils.IotaAPIUtils
import org.threeten.bp.OffsetDateTime

import org.hermes.crypto.SecP256K1PrivKey
import org.hermes.entities.Event
import org.hermes.iota.IOTA
import org.hermes.iota.Seed
import org.hermes.utils.prepend
import org.hermes.utils.sign
import org.hermes.utils.splitInChunks
import org.hermes.utils.toTrytes


class IOTAConnector(val seed: Seed, private val privateKey: SecP256K1PrivKey, app: HermesClientApp) {

    val loggingTag: String = "IOTAConnector"

    val EMPTY_TAG: String = StringUtils.rightPad("", 27, "9")

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
            .sign(header = "digest:", privateKey = privateKey, separator = "::")

        Log.d(loggingTag, "$clientUUID -- Cleartext payload: $clearTextPayload")

        return clearTextPayload
            .toTrytes()
            .splitInChunks(IOTA.Transaction.SIGNATURE_MESSAGE_FRAGMENT)
            .map { StringUtils.rightPad(it, Constants.MESSAGE_LENGTH, '9') }
            .toMutableList()
    }

    fun sendData(samples: Array<Metric20?>, clientUUID: String, asyncConfirmation: Boolean,
                 blockUntilConfirmation: Boolean, sensorId: String = "", eventBus: Handler
    ): Array<Metric20?> {
        // Validate seed
        if (!InputValidator.isValidSeed(seed.toString())) {
            Log.e(loggingTag, "The seed loaded to the service is incorrect!")
            return samples
        }
        val (previousAddress, newAddress, nextAddress) = prepareAddress(clientUUID)
        val trytes = prepareTransactions(previousAddress, newAddress, nextAddress, clientUUID, *samples)

        var i = 1
        while (i <= 3 && !broadcastBundle(clientUUID, trytes, newAddress, i, samples.size, 3, eventBus)) {
            if (i == 3) {
                Log.e(loggingTag, "Could not broadcast transactions. Aborting!")
                return samples
            }
            Thread.sleep(10 * 1000)
            i += 1
        }

        if (asyncConfirmation) {
            if (blockUntilConfirmation) runBlocking {
                checkResultOfTransactions(arrayOf(newAddress), clientUUID, samples.size, eventBus)
            }
            else CoroutineScope(BACKGROUND.asCoroutineDispatcher())
                .launch { checkResultOfTransactions(arrayOf(newAddress), clientUUID, samples.size, eventBus) }
        }
        return Array(0) { null }
    }

    private fun prepareAddress(clientUUID: String): List<String> {
        val newAddressIndex = prefs.getInt("latest_addr_index", 1000) + 1
        val previousAddress = prefs.getString("latest_addr_used", "")!!
        Log.d(loggingTag, "$clientUUID -- Next IOTA address index to use is: $newAddressIndex")
        val newAddress = IotaAPIUtils.newAddress(seed.toString(), Seed.DEFAULT_SEED_SECURITY,
            newAddressIndex, true, SpongeFactory.create(SpongeFactory.Mode.KERL))
        val nextAddress = IotaAPIUtils.newAddress(seed.toString(), Seed.DEFAULT_SEED_SECURITY,
            newAddressIndex + 1, true, SpongeFactory.create(SpongeFactory.Mode.KERL))

        if (previousAddress == "") {
            Log.i(loggingTag, "This is the first packet to be attached. Storing first address index $newAddress")
            prefs.edit()
                .putString("root_address", newAddress)
                .apply()
        }

        prefs.edit()
            .putInt("latest_addr_index", newAddressIndex)
            .putString("latest_addr_used", newAddress)
            .apply()

        Log.d(loggingTag,
            "$clientUUID -- Address that will be used for the next sample broadcast is $newAddress")

        return listOf(previousAddress, newAddress, nextAddress)
    }

    private fun prepareTransactions(previousAddress: String, newAddress: String, nextAddress: String,
                                    clientUUID: String, vararg samples: Metric20?): Array<String> {
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
                                sampleNum: Int, maxTries: Int, eventBus: Handler): Boolean {
        val depth = 3
        val minWeightMagnitude = 14

        val eventMessage = StringBuilder()
            .append("$clientUUID -- Broadcasting ")
            .append(bundleTrytes.size)
            .append(" transaction" + (if (bundleTrytes.size > 1) "s" else ""))
            .append(" to address: ")
            .append(address)
            .append(" ($currentTry/$maxTries tries)")
            .toString()

        Log.i(loggingTag, eventMessage)
        val msg = "There was an error while trying to broadcast packets to the Tangle: "
        try {
            Log.d(loggingTag, "Attempting to broadcast transactions to address $address")
            api.sendTrytes(bundleTrytes, depth, minWeightMagnitude, null)
            Log.d(loggingTag, "Broadcast to address $address succeeded")
            eventBus.sendMessage(eventBus.obtainMessage().apply{
                obj = Pair(MetadataRepository.DataType.PACKETS_BROADCAST, sampleNum) })
        } catch (e: Throwable) {
            Log.e(loggingTag, "$clientUUID -- $msg ${e.message}")
            db.eventDao().insertAll(Event(action = "broadcast failure ($currentTry/$maxTries)", resource = "iota",
                extraInfo = "$msg ${e.message}"))
            return false
        }

        val event = Event(action = "broadcast", resource = "iota", extraInfo = eventMessage)
        db.eventDao().insertAll(event)
        return true
    }

    private suspend fun checkResultOfTransactions(trxs: Array<String>, clientUUID: String,
                                                  packetsBroadcast: Int, eventBus: Handler) {
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
                eventBus.sendMessage(eventBus.obtainMessage().apply{
                    obj = Pair(MetadataRepository.DataType.PACKETS_CONFIRMED, packetsBroadcast) })
                eventBus.sendMessage(eventBus.obtainMessage().apply{
                    obj = Pair(MetadataRepository.DataType.IOTA_STREAM_ROOT_ADDRESS, fetchedTxs[0].address) })
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