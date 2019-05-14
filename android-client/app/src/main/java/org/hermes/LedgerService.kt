package org.hermes

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import dagger.Module
import dagger.android.AndroidInjection
import java.security.KeyPair
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import kotlin.concurrent.withLock
import kotlinx.coroutines.*

import org.hermes.activities.LoginActivity
import org.hermes.iota.Seed
import org.hermes.ledgers.IOTAConnector


class LedgerService : Service() {

    @Module
    abstract class DaggerModule

    data class Sensor(val dataId: String, val unit: String, val mtype: String, val what: String?, val device: String?) {
        private var start = 0
        private var end = -1
        private val lock = ReentrantLock()
        // TODO: Make this configurable
        private val sampleSize = 10
        private var buffer = Array<Metric20?>(sampleSize) { null }
        lateinit var uuid: String
        var active: AtomicBoolean = AtomicBoolean(false)

        /**
         * Put a new sample in the buffer.
         * The buffer is a ring buffer so if the client exceeds the available number of
         * samples, the oldest sample will be overwritten.
         */
        fun putSample(metric: Metric20) = lock.withLock {
            end += 1
            start = (start + (end / sampleSize)) % sampleSize
            end %= sampleSize
            buffer[end] = metric
        }

        /**
         * Return a buffer with all the samples in the correct order and clear the original buffer
         */
        fun flushData(): Array<Metric20?> = lock.withLock {
            val sampleNum = if (start != 0) sampleSize else end
            if (sampleNum <= 0) {
                Array(0) { null }
            } else {
                val tempBuffer = Array<Metric20?>(sampleNum) { null }
                for (j in 0 until sampleNum) {
                    tempBuffer[j] = buffer[(start + j) % sampleSize]
                    buffer[(start + j) % sampleSize] = null
                }
                start = 0
                end = -1
                tempBuffer
            }
        }
    }

    private val binder = object : IHermesService.Stub() {

        override fun register(dataId: String?, unit: String?, mtype: String?, what: String?, device: String?): String =
            when {
                dataId == null -> ErrorCode.NO_DATA_ID.errorStr
                unit == null -> ErrorCode.NO_UNIT.errorStr
                mtype == null -> ErrorCode.NO_TYPE.errorStr
                else -> {
                    var uuid = reverseSensorRegistry.getOrDefault(Sensor(dataId, unit, mtype, what, device), "")
                    if (uuid.isEmpty()) {
                        uuid = UUID.randomUUID().toString()
                        val newSensor = Sensor(dataId, unit, mtype, what, device).apply { this.uuid = uuid }
                        sensorRegistry[uuid] = newSensor
                        repository.addSensor(newSensor)
                        reverseSensorRegistry[newSensor] = uuid
                        Log.i(loggingTag, "A new sensor has registered with the application with uuid $uuid")
                    }
                    uuid
                }
            }

        override fun deregister(uuid: String?): String =
            when (uuid) {
                null -> ErrorCode.NO_UUID.errorStr
                else -> {
                    val sensor = sensorRegistry.remove(uuid)
                    if (sensor != null) repository.removeSensor(sensor)
                    ""
                }
            }

        override fun sendDataDouble(uuid: String?, value: Double, http_method: String?,
                                    http_code: String?, result: String?, stat: String?, direction: String?,
                                    file: String?, line: Int, env: String?): String =
            when {
                uuid == null -> ErrorCode.NO_UUID.errorStr
                !sensorRegistry.containsKey(uuid) -> ErrorCode.NOT_REGISTERED.errorStr
                !repository.unsealed() -> ErrorCode.SEALED.errorStr
                else -> {
                    Log.d(loggingTag, "Got a new sample from client with uuid $uuid")
                    // TODO: Add a check to ensure that the data are in the expected form
                    val client = sensorRegistry[uuid] as Sensor
                    val newSample = Metric20(client.dataId, value)
                        .setData(Metric20.TagKey.MTYPE, client.mtype)
                        .setData(Metric20.TagKey.UNIT, client.unit)
                    client.putSample(newSample)
                    ""
                }
            }

        override fun sendDataString(uuid: String?, value: String?, http_method: String?,
                                    http_code: String?, result: String?, stat: String?, direction: String?,
                                    file: String?, line: Int, env: String?): String  =
            when {
                uuid == null -> ErrorCode.NO_UUID.errorStr
                !sensorRegistry.containsKey(uuid) -> ErrorCode.NOT_REGISTERED.errorStr
                !repository.unsealed() -> ErrorCode.SEALED.errorStr
                else -> {
                    Log.d(loggingTag, "Got a new sample from client with uuid $uuid")
                    // TODO: Add a check to ensure that the data are in the expected form
                    val client = sensorRegistry[uuid] as Sensor
                    val newSample = Metric20(client.dataId, value.toString())
                        .setData(Metric20.TagKey.MTYPE, client.mtype)
                        .setData(Metric20.TagKey.UNIT, client.unit)
                    client.putSample(newSample)
                    ""
                }
            }
    }

    var iHermesService: IHermesService? = null

    private val mConnectionToSelf = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            iHermesService = IHermesService.Stub.asInterface(service)
            Log.i(loggingTag,
                "Connection has been established with self. Beginning data sampling from built-in sensors")
            CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch { generateData() }
        }

        override fun onServiceDisconnected(className: ComponentName) {
            Log.e(loggingTag, "Service has unexpectedly disconnected")
            iHermesService = null
        }
    }


    private val loggingTag = "LedgerService"

    private var mNotificationManager: NotificationManager? = null
    private val channelId = SecureRandom.getInstance("SHA1PRNG").nextInt().toString()
    private val foregroundNotificationId: Int = 15970
    private val sensorRegistry = ConcurrentHashMap<String, Sensor>()
    private val reverseSensorRegistry = HashMap<Sensor, String>()
    private var broadcastStart: Long? = null

    @Inject
    lateinit var db: HermesRoomDatabase

    @Inject
    lateinit var repository: HermesRepository

    @Inject
    lateinit var app: HermesClientApp

    private val iotaConnector by lazy {
        IOTAConnector(repository.getSeed() as Seed, repository.getKeyPair() as KeyPair, app)
    }

    override fun onCreate() {
        AndroidInjection.inject(this)
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(loggingTag, "Received start id $startId: $intent")
        Log.d(loggingTag, "Showing notification for Hermes service foregrounding")
        startForeground(foregroundNotificationId, buildNotification())
        try {
            // Start co-routine to broadcast data
            CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch { broadcastData() }
            CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
                Log.d(loggingTag, "Creating intent for Ledger service connection")
                val iLedgerIntent = Intent(this@LedgerService, LedgerService::class.java)
                iLedgerIntent.action = LedgerService::class.java.name
                baseContext.bindService(iLedgerIntent, mConnectionToSelf, Context.BIND_AUTO_CREATE)
            }
        } catch (e: java.lang.Exception) {
            Log.e(loggingTag, "There was an exception while trying to start the service $e")
            stopSelf()
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(loggingTag, "Destroying Ledger Service")
        stopForeground(true)

        // Cancel the persistent notification.
        mNotificationManager?.cancel(R.string.remote_service_started)

        // Tell the user we stopped.
        Toast.makeText(this, R.string.remote_service_stopped, Toast.LENGTH_SHORT).show()

        Log.i(loggingTag, "Terminating the background executors")
        BACKGROUND.shutdown()
        Log.i(loggingTag, "Waiting for the background executor to finish everything")
        BACKGROUND.awaitTermination(1, TimeUnit.SECONDS)
        Log.i(loggingTag, "All background threads have finished")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    /**
     *  Create a notification channel before sending a notification.
     *
     *  This is important for Android API version >= 26
     */
    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(loggingTag, "Creating notification channel for background service")
            val name = getString(R.string.remote_service_channel_name)
            val descriptionText = getString(R.string.remote_service_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            mNotificationManager?.createNotificationChannel(channel)
        } else {
        }
    }

    /**
     * Show a notification that this service is running.
     *
     * This is important because if the service does not show a notification, the OS will not allow
     * it to keep running once the activities are not running.
     * Re-creating an existing notification channel does not produce any errors or side-effects
     */
    private fun buildNotification(): Notification {
        // Launch the Hermes activity when the user presses the notification
        val contentIntent: PendingIntent = PendingIntent.getActivity(this, 0,
            Intent(this, LoginActivity::class.java), 0)

        // Set the info for the views that show in the notification panel.
        return NotificationCompat.Builder(this, channelId)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(getText(R.string.remote_service_label))
            .setContentText(getText(R.string.remote_service_started))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()
    }

    private suspend fun generateData() {
        val uuid = iHermesService?.register("android.random", "int", "random_source", null, null)
        if (uuid == null && uuid != ErrorCode.ALREADY_REGISTERED.errorStr
            && uuid != ErrorCode.NO_DATA_ID.errorStr && uuid != ErrorCode.NO_TYPE.errorStr
            && uuid != ErrorCode.NO_UNIT.errorStr)
        {
            Log.e(loggingTag, "There was an error while trying to connect to the service")
            return
        }
        Log.i(loggingTag, "Starting generating data with uuid $uuid")
        for (i: Int in 0 until 100) {
            Log.d(loggingTag, "Generating a data sample for the Hermes Service")
            iHermesService?.sendDataDouble(uuid, (Random().nextInt() % 30).toDouble(), null, null,
                null, null, null, null, -1, null)
            delay(5 * 1000)
        }
    }

    private suspend fun broadcastData() {
        while (true) {
            if (!repository.ledgerServiceRunning.get()) {
                Log.d(loggingTag, "Ledger service skipping broadcasting data for now")
                broadcastStart = null
            } else {
                Log.d(loggingTag, "Hermes service looking at the registered client data")
                if (broadcastStart == null) {
                    broadcastStart = System.currentTimeMillis()
                    repository.ledgerServiceUptime.postValue(0)
                } else {
                    repository.ledgerServiceUptime.postValue(((System.currentTimeMillis() - broadcastStart as Long) / (60 * 1000)).toInt())
                }
                for ((uuid, client) in sensorRegistry.filter { it.value.active.get() }) {
                    Log.d(loggingTag, "Broadcasting data of client with id $uuid")
                    client.flushData().apply {
                        if (isNotEmpty()) iotaConnector.sendData(
                            *this, clientUUID = uuid,
                            blockUntilConfirmation = true, asyncConfirmation = true,
                            packetCounter = repository.packetBroadcastNum
                        )
                    }
                }
            }
            delay(20 * 1000)
        }
    }
}
