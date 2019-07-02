package org.hermes.ledgers

import android.content.SharedPreferences
import android.util.Log
import org.hermes.CryptoRepository
import org.hermes.SensorRepository
import org.hermes.iota.Seed
import org.iota.jota.pow.SpongeFactory
import org.iota.jota.utils.IotaAPIUtils
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


class UnknownClient: Exception()

@Singleton
class IOTAAddressManager @Inject constructor(
    cryptoRepository: CryptoRepository,
    sensorRepository: SensorRepository,
    @param:Named("auth") val iotaPrefs: SharedPreferences
) {

    val loggingTag = "IOTAAddressManager"

    private val unConfirmedAddresses = HashMap<String, Pair<String, Int>>()

    private val seed = cryptoRepository.IOTASeed().toString()

    private val sensorEventBus = sensorRepository.eventBus

    fun getAddress(clientUUID: String): Triple<String, String, String> {
        val newAddressIndex = iotaPrefs.getInt("${clientUUID}_latest_address_index", 1000) + 1
        val previousAddress = iotaPrefs.getString("${clientUUID}_latest_address", "")!!
        val nextAddressIndex = newAddressIndex + 1

        val newAddress = IotaAPIUtils.newAddress(seed, Seed.DEFAULT_SEED_SECURITY,
            newAddressIndex, true, SpongeFactory.create(SpongeFactory.Mode.KERL))
        val nextAddress = IotaAPIUtils.newAddress(seed, Seed.DEFAULT_SEED_SECURITY,
            nextAddressIndex, true, SpongeFactory.create(SpongeFactory.Mode.KERL))

        Log.d(loggingTag, "$clientUUID -- New IOTA address for client $clientUUID is: $newAddressIndex, $newAddress")

        unConfirmedAddresses[clientUUID] = Pair(newAddress, newAddressIndex)
        return Triple(previousAddress, newAddress, nextAddress)
    }

    /**
     * Marks the last address created for the client as confirmed
     * @throws UnknownClient
     */
    fun confirmAddress(clientUUID: String) {
        if (!unConfirmedAddresses.containsKey(clientUUID))
            throw UnknownClient()
        val latestAddressIndexPair = unConfirmedAddresses[clientUUID]!!
        val prefs = iotaPrefs.edit()
        if (!iotaPrefs.contains("${clientUUID}_root_address")) {
            prefs.putString("${clientUUID}_root_address", latestAddressIndexPair.first)
            val message = sensorEventBus.obtainMessage()
            message.obj = Pair(SensorRepository.MessageType.NOTIFY_SENSOR_ROOT_ADDRESS, clientUUID)
            message.data.putString("root_address", latestAddressIndexPair.first)
            sensorEventBus.sendMessage(message)
        }

        prefs
            .putString("${clientUUID}_latest_address", latestAddressIndexPair.first)
            .putInt("${clientUUID}_latest_address_index", latestAddressIndexPair.second)
            .apply()

        val message = sensorEventBus.obtainMessage()
        message.obj = Pair(SensorRepository.MessageType.NOTIFY_SENSOR_LATEST_ADDRESS, clientUUID)
        message.data.putString("latest_address", latestAddressIndexPair.first)
        sensorEventBus.sendMessage(message)
        unConfirmedAddresses.remove(clientUUID)
    }

    fun hasUnconfirmedAddressFor(clientUUID: String): Boolean = unConfirmedAddresses[clientUUID] != null

}