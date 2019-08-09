package org.hermes.repositories

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.hermes.BACKGROUND
import org.hermes.HermesRoomDatabase
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

import org.hermes.entities.Sensor
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock


@Singleton
class SensorRepository @Inject constructor(
    val db: HermesRoomDatabase
) {

    val loggingTag = "SensorRepository"

    enum class MessageType {
        ADD_SENSOR,
        FORCE_ADD_SENSOR,
        REMOVE_SENSOR,
        NOTIFY_SENSOR_ROOT_ADDRESS,
        NOTIFY_SENSOR_LATEST_ADDRESS,
        FLIP_SENSOR,
    }

    val eventBus: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            val msg = inputMessage.obj
            if (msg == null || msg !is Pair<*, *> || msg.first !is MessageType) return
            val message = msg as Pair<MessageType, *>
            when (message.first) {
                MessageType.ADD_SENSOR -> {
                    if (message.second !is Sensor) return
                    val sensor = message.second as Sensor
                    add(sensor)
                    Log.i(loggingTag, "A new sensor has registered with the application with uuid ${sensor.uuid}")
                }
                MessageType.FORCE_ADD_SENSOR -> {
                    if (message.second !is Sensor) return
                    val sensor = message.second as Sensor
                    add(sensor, store = false, force = true)
                    Log.i(loggingTag, "A new sensor has registered with the application with uuid ${sensor.uuid}")
                }
                MessageType.REMOVE_SENSOR -> {
                    if (message.second !is Sensor) return
                    val sensor = message.second as Sensor
                    sensorListData.value?.remove(message.second as Sensor)
                    // Do this to notify clients that the data has changed
                    sensorListData.postValue(sensorListData.value)
                    activeSensorNum.postValue(sensorListData.value?.filter { it.active.get() }?.size ?: 0)
                    registry.remove(sensor.uuid)
                    reverseRegistry.remove(sensor.dataId)
                    rootAddresses.remove(sensor.uuid)
                    latestAddresses.remove(sensor.uuid)
                }
                MessageType.FLIP_SENSOR -> {
                    if (message.second !is Sensor) return
                    val sensor = message.second as Sensor
                    sensor.active.setAndNotify(!sensor.active.get())
                    activeSensorNum.postValue(sensorListData.value?.filter { it.active.get() }?.size ?: 0)
                }
                MessageType.NOTIFY_SENSOR_LATEST_ADDRESS -> {
                    val addr = inputMessage.data.getString("latest_address") as String
                    val addrIndex = inputMessage.data.getInt("latest_address_index") as Int
                    val uuid = message.second as String
                    latestAddresses[uuid]?.value = addr
                    latestAddressIndices[uuid]?.value = addrIndex
                    CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
                        Log.d(loggingTag, "Updating latest address of $uuid")
                        db.sensorDao().updateLatestAddress(uuid, addr)
                        db.sensorDao().updateLatestAddressIndex(uuid, addrIndex)
                    }
                }
                MessageType.NOTIFY_SENSOR_ROOT_ADDRESS -> {
                    val addr = inputMessage.data.getString("root_address") as String
                    val uuid = message.second as String
                    rootAddresses[uuid]?.value = addr
                    CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
                        Log.d(loggingTag, "Updating root address of $uuid")
                        db.sensorDao().updateRootAddress(uuid, addr)
                    }
                }
            }
        }
    }

    private fun <T> initLiveData(value: T): MutableLiveData<T> {
        return MutableLiveData<T>().apply { this.value = value }
    }

    val activeSensorNum: MutableLiveData<Int> = initLiveData(0)
    val registry = HashMap<String, Sensor>()
    val reverseRegistry = HashMap<String, String>()
    val rootAddresses = HashMap<String, MutableLiveData<String>>()
    val latestAddresses = HashMap<String, MutableLiveData<String>>()
    val latestAddressIndices = HashMap<String, MutableLiveData<Int>>()
    val sensorListData: MutableLiveData<MutableList<Sensor>> = initLiveData(LinkedList<Sensor>())
    private val lock = ReentrantLock()

    fun fetchSensor(id: String, callback: (sensor: Sensor?) -> Unit) {
        callback(sensorListData.value?.firstOrNull { it.dataId == id })
    }

    fun add(sensor: Sensor, store: Boolean = true, force: Boolean = false) = lock.withLock {
        Log.d(loggingTag, "Sensor uuid: ${sensor.uuid} $force $store")
        if (!force && registry.containsKey(sensor.uuid)) return
        // Do this to notify clients that the data has changed
        sensorListData.postValue(sensorListData.value?.filter { it.uuid != sensor.uuid }?.toMutableList()?.apply {
            add(sensor)
        })
        activeSensorNum.postValue(sensorListData.value?.filter { it.active.get() }?.size ?: 0)
        registry[sensor.uuid] = sensor
        reverseRegistry[sensor.dataId] = sensor.uuid
        rootAddresses[sensor.uuid] = initLiveData(sensor.rootAddress ?: "")
        latestAddresses[sensor.uuid] = initLiveData(sensor.latestAddress ?: "")
        latestAddressIndices[sensor.uuid] = initLiveData(sensor.latestAddressIndex ?: 1000)
        if (store) {
            CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
                val sensorInDB = db.sensorDao().getByTag(sensor.dataId)
                if (sensorInDB == null) {
                    Log.i(loggingTag, "Storing sensor with UUID ${sensor.uuid}")
                    db.sensorDao().insertAll(sensor)
                }
            }
        }
    }

    init {
        CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
            db.sensorDao().getAll().forEach {
                eventBus.sendMessage(eventBus.obtainMessage().apply {
                    obj = Pair(MessageType.FORCE_ADD_SENSOR, it) })
            }
        }
    }
}