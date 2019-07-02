package org.hermes

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.MutableLiveData
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SensorRepository @Inject constructor() {

    val loggingTag = "SensorRepository"

    enum class MessageType {
        ADD_SENSOR,
        REMOVE_SENSOR,
        NOTIFY_SENSOR_ROOT_ADDRESS,
        NOTIFY_SENSOR_LATEST_ADDRESS,
        FLIP_SENSOR,
    }

    val eventBus: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            val msg = inputMessage.obj
            if (msg == null || msg !is Pair<*, *> || msg.first !is SensorRepository.MessageType) return
            val message = msg as Pair<SensorRepository.MessageType, *>
            when (message.first) {
                MessageType.ADD_SENSOR -> {
                    if (message.second !is LedgerService.Sensor) return
                    val sensor = message.second as LedgerService.Sensor
                    sensorListData.value?.add(sensor)
                    // Do this to notify clients that the data has changed
                    sensorListData.postValue(sensorListData.value)
                    activeSensorNum.postValue(sensorListData.value?.filter { it.active.get() }?.size ?: 0)
                    registry[sensor.uuid] = sensor
                    reverseRegistry[sensor] = sensor.uuid
                    rootAddresses[sensor.uuid] = initLiveData("")
                    latestAddresses[sensor.uuid] = initLiveData("")
                    Log.i(loggingTag, "A new sensor has registered with the application with uuid ${sensor.uuid}")
                }
                MessageType.REMOVE_SENSOR -> {
                    if (message.second !is LedgerService.Sensor) return
                    val sensor = message.second as LedgerService.Sensor
                    sensorListData.value?.remove(message.second as LedgerService.Sensor)
                    // Do this to notify clients that the data has changed
                    sensorListData.postValue(sensorListData.value)
                    activeSensorNum.postValue(sensorListData.value?.filter { it.active.get() }?.size ?: 0)
                    registry.remove(sensor.uuid)
                    reverseRegistry.remove(sensor)
                    rootAddresses.remove(sensor.uuid)
                    latestAddresses.remove(sensor.uuid)
                }
                MessageType.FLIP_SENSOR -> {
                    if (message.second !is LedgerService.Sensor) return
                    val sensor = message.second as LedgerService.Sensor
                    sensor.active.setAndNotify(!sensor.active.get())
                    activeSensorNum.postValue(sensorListData.value?.filter { it.active.get() }?.size ?: 0)
                }
                MessageType.NOTIFY_SENSOR_LATEST_ADDRESS ->
                    latestAddresses[message.second as String]?.value = inputMessage.data.getString("latest_address")
                MessageType.NOTIFY_SENSOR_ROOT_ADDRESS ->
                    rootAddresses[message.second as String]?.value = inputMessage.data.getString("root_address")
            }
        }
    }

    private fun <T> initLiveData(value: T): MutableLiveData<T> {
        return MutableLiveData<T>().apply { this.value = value }
    }

    val activeSensorNum: MutableLiveData<Int> = initLiveData(0)
    val registry = HashMap<String, LedgerService.Sensor>()
    val reverseRegistry = HashMap<LedgerService.Sensor, String>()
    val rootAddresses = HashMap<String, MutableLiveData<String>>()
    val latestAddresses = HashMap<String, MutableLiveData<String>>()
    val sensorListData: MutableLiveData<LinkedList<LedgerService.Sensor>> = initLiveData(LinkedList<LedgerService.Sensor>())

    fun fetchSensor(id: String, callback: (sensor: LedgerService.Sensor) -> Unit) {
        for (sensor in sensorListData.value!!) {
            if (sensor.dataId == id) {
                callback(sensor)
                return
            }
        }
    }
}