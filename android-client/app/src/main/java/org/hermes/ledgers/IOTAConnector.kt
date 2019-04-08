package org.hermes.ledgers


import android.util.Log
import java.lang.Exception
import java.util.ArrayList
import jota.IotaAPI
import jota.model.Transfer
import jota.utils.*

import org.hermes.Metric20
import org.hermes.iota.Seed
import org.hermes.utils.toTrytes



class IOTAConnector(val protocol: String, val uri: String, val port: String, val seed: Seed) {

    val loggingTag: String = "IOTAConnector"
    val api: IotaAPI

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

    fun sendData(sample: Metric20) {
        try {
            // validate seed
            if (!InputValidator.isValidSeed(seed.toString())) {
                Log.e(loggingTag, "The seed loaded to the service is incorrect!")
                return
            }

            // TODO: Save the index of the previous
            val address = api.getNextAvailableAddress(seed.toString(), 2, true).first()
            if (address == null) {
                Log.e(loggingTag, "Could not get a new address for IOTA")
                return
            }

            val normalizedAddress = Checksum.removeChecksum(address)
            val depth = 3
            val minWeightMagnitude = 14

            val transfers = ArrayList<Transfer>()
            transfers.add(Transfer(normalizedAddress, 0, sample.toCarbon20String().toTrytes(), ""))

            api.sendTransfer(seed.toString(), 2, depth, minWeightMagnitude, transfers, null,
                normalizedAddress, false, false, null)
        } catch (e: Exception) {
            Log.e(loggingTag, "There was an error while trying to send a sample to IOTA: $e")
        }
    }
}