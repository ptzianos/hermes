package org.hermes

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.hermes.daos.EventDao
import org.hermes.entities.Event
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton


@Singleton
class MetadataRepository @Inject constructor(
    private val db: HermesRoomDatabase,
    private val application: Application,
    @Named("iota") private val prefs: SharedPreferences
) {

    private val loggingTag = "MetadataRepository"

    val eventDao: EventDao = db.eventDao()
    private var ledgerServiceBootstrapped: Boolean = false
    var ledgerServiceRunning: AtomicBoolean = AtomicBoolean(true)
    private var sensorList: LinkedList<LedgerService.Sensor> = LinkedList()
    private var sensorListData: MutableLiveData<List<LedgerService.Sensor>> = {
        val mld = MutableLiveData<List<LedgerService.Sensor>>()
        mld.value = sensorList
        mld
    }()
    private val activeSensorNum: MutableLiveData<Int> = {
        val mld = MutableLiveData<Int>()
        mld.value = 0
        mld
    }()
    val ledgerServiceUptime: MutableLiveData<Int> = {
        val mld = MutableLiveData<Int>()
        mld.value = 0
        mld
    }()
    val packetBroadcastNum: MutableLiveData<Int> = {
        val mld = MutableLiveData<Int>()
        mld.value = 0
        mld
    }()
    val ledgerServiceRunningLiveData: MutableLiveData<Boolean> = {
        val mld = MutableLiveData<Boolean>()
        mld.value = ledgerServiceRunning.get()
        mld
    }()
    val rootIOTAAddress: MutableLiveData<String> = {
        val mld = MutableLiveData<String>()
        mld.value = prefs.getString("root_address", "")!!
        mld
    }()

    fun addSensor(sensor: LedgerService.Sensor) {
        sensorList.add(sensor)
        // Do this to notify clients that the data has changed
        sensorListData.postValue(sensorListData.value)
        activeSensorNum.postValue(sensorList.filter { it.active.get() }.size)
    }

    fun removeSensor(sensor: LedgerService.Sensor) {
        sensorList.remove(sensor)
        // Do this to notify clients that the data has changed
        sensorListData.postValue(sensorList)
        activeSensorNum.postValue(sensorList.filter { it.active.get() }.size)
    }

    fun refreshSensorList() {
        sensorListData.value = sensorList
        activeSensorNum.value = sensorList.filter { it.active.get() }.size
    }

    fun getSensorLiveData(): LiveData<List<LedgerService.Sensor>> {
        return sensorListData
    }

    /**
     * Returns the number of minutes the service has been running
     */
    fun getLedgerServiceUptime(): LiveData<Int> {
        return ledgerServiceUptime
    }

    fun getActiveSensorNumLiveData(): LiveData<Int> {
        return activeSensorNum
    }

    fun getPacketsBroadcast(): LiveData<Int> {
        return packetBroadcastNum
    }

    /**
     * Start the LedgerService if it's not running already
     */
    fun startLedgerService() {
        if (!ledgerServiceBootstrapped) {
            Log.i(loggingTag,"Ledger service is not running. Starting it now")
            val intent = Intent(application.applicationContext, LedgerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) application.startForegroundService(intent)
            else application.startService(intent)
            ledgerServiceBootstrapped = true
        } else
            Log.i(loggingTag, "Ledger service is already running")
    }

    fun fetchEvent(id: Int, callback: (event: Event) -> Unit) {
        CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
            callback(db.eventDao().findById(id))
        }
    }

    fun fetchSensor(id: String, callback: (sensor: LedgerService.Sensor) -> Unit) {
        for (sensor in sensorList) {
            if (sensor.dataId == id) {
                callback(sensor)
                return
            }
        }
    }
}