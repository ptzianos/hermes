package org.hermes

import android.app.Application
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import org.hermes.daos.EventDao
import org.hermes.entities.Event
import org.hermes.utils.AtomicLiveBoolean
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class MetadataRepository @Inject constructor(
    private val db: HermesRoomDatabase,
    private val application: Application
) {

    enum class MessageType {
        IOTA_RECEIVED,
        ETH_RECEIVED,
        PACKETS_BROADCAST,
        PACKETS_CONFIRMED,
        START_BACKGROUND_SERVICE,
        STOP_BACKGROUND_SERVICE,
        FLIP_BACKGROUND_SERVICE,
        NOTIFY_FAILED_BROADCAST,
    }

    val eventBus: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            val msg = inputMessage.obj
            if (msg == null || msg !is Pair<*, *> || msg.first !is MetadataRepository.MessageType) return
            val message = msg as Pair<MetadataRepository.MessageType, *>
            when (message.first) {
                MessageType.PACKETS_BROADCAST -> {
                    if (message.second == null || message.second !is Int) return
                    packetsBroadcastNum.value = (packetsBroadcastNum.value ?: 0) + message.second as Int
                }
                MessageType.PACKETS_CONFIRMED -> {
                    if (message.second == null || message.second !is Int) return
                    packetsConfirmedNum.value = (packetsConfirmedNum.value ?: 0) + message.second as Int
                }
                MessageType.START_BACKGROUND_SERVICE -> ledgerServiceBroadcasting.setAndNotify(true)
                MessageType.STOP_BACKGROUND_SERVICE -> ledgerServiceBroadcasting.setAndNotify(false)
                MessageType.FLIP_BACKGROUND_SERVICE -> if (ledgerServiceBroadcasting.get()) ledgerServiceBroadcasting.setAndNotify(false)
                                                    else ledgerServiceBroadcasting.setAndNotify(true)
                MessageType.NOTIFY_FAILED_BROADCAST -> failedBroadcastNum.value = (failedBroadcastNum.value ?: 0) + 1
                else -> Log.e(loggingTag, "Someone sent an unknown packet to the Metadata event handler")
            }
        }
    }

    private fun <T> initLiveData(value: T): MutableLiveData<T> {
        return MutableLiveData<T>().apply { this.value = value }
    }

    private val loggingTag = "MetadataRepository"

    val eventDao: EventDao = db.eventDao()
    private var ledgerServiceBootstrapped: Boolean = false
    var ledgerServiceBroadcasting: AtomicLiveBoolean = AtomicLiveBoolean(false)
    private var sensorList: LinkedList<LedgerService.Sensor> = LinkedList()
    var sensorListData: MutableLiveData<List<LedgerService.Sensor>> = initLiveData(sensorList)
    val ledgerServiceUptime: MutableLiveData<Int> = initLiveData(0)
    val packetsBroadcastNum: MutableLiveData<Int> = initLiveData(0)
    val packetsConfirmedNum: MutableLiveData<Int> = initLiveData(0)
    val rootIOTAAddress: MutableLiveData<String> = initLiveData("")
    val failedBroadcastNum: MutableLiveData<Int> = initLiveData(0)

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
}