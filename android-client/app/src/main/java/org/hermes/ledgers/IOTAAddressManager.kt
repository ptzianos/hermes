package org.hermes.ledgers

import android.content.SharedPreferences
import android.util.Log
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import org.hermes.bip44.iota.Seed
import org.hermes.repositories.CryptoRepository
import org.hermes.repositories.SensorRepository
import org.iota.jota.pow.SpongeFactory
import org.iota.jota.utils.IotaAPIUtils


class UnknownClient: Exception()

@Singleton
class IOTAAddressManager @Inject constructor(
    cryptoRepository: CryptoRepository,
    private val sensorRepository: SensorRepository,
    @param:Named("auth") val iotaPrefs: SharedPreferences
) {

    val loggingTag = "IOTAAddressManager"

    private val unConfirmedAddresses = HashMap<String, Pair<String, Int>>()

    private val seed = cryptoRepository.IOTASeed().toString()

    private val sensorEventBus = sensorRepository.eventBus

    fun getAddress(clientUUID: String): Triple<String, String, String> {
        val previousAddress = sensorRepository.latestAddresses[clientUUID]?.value ?: ""
        val latestAddressIndex = sensorRepository.latestAddressIndices[clientUUID]?.value ?: 1000
        val newAddressIndex = latestAddressIndex + 1
        val nextAddressIndex = newAddressIndex + 1

        val newAddress = IotaAPIUtils.newAddress(seed, Seed.DEFAULT_SEED_SECURITY,
            newAddressIndex, true, SpongeFactory.create(SpongeFactory.Mode.KERL))
        val nextAddress = IotaAPIUtils.newAddress(seed, Seed.DEFAULT_SEED_SECURITY,
            nextAddressIndex, true, SpongeFactory.create(SpongeFactory.Mode.KERL))

        Log.d(loggingTag, "$clientUUID -- New IOTA address for client is: $newAddressIndex, $newAddress")

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
        val rootAddress = sensorRepository.rootAddresses[clientUUID]?.value
        if (rootAddress == null || rootAddress == "") {
            val message = sensorEventBus.obtainMessage()
            message.obj = Pair(SensorRepository.MessageType.NOTIFY_SENSOR_ROOT_ADDRESS, clientUUID)
            message.data.putString("root_address", latestAddressIndexPair.first)
            sensorEventBus.sendMessage(message)
        }

        val message = sensorEventBus.obtainMessage()
        message.obj = Pair(SensorRepository.MessageType.NOTIFY_SENSOR_LATEST_ADDRESS, clientUUID)
        message.data.putString("latest_address", latestAddressIndexPair.first)
        message.data.putInt("latest_address_index", latestAddressIndexPair.second)
        sensorEventBus.sendMessage(message)
        unConfirmedAddresses.remove(clientUUID)
    }

    fun failBroadcast(clientUUID: String) = unConfirmedAddresses.remove(clientUUID)

    fun hasUnconfirmedAddressFor(clientUUID: String): Boolean = unConfirmedAddresses.contains(clientUUID)

}