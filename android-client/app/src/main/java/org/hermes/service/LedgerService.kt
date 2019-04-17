package org.hermes.service

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
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.concurrent.withLock
import kotlinx.coroutines.*
import org.hermes.*

import org.hermes.activities.LoginActivity
import org.hermes.iota.Seed
import org.hermes.ledgers.IOTAConnector


class LedgerService : Service() {

    @Module
    abstract class DaggerModule

    data class Client(val dataId: String, val unit: String, val mtype: String, val what: String?, val device: String?) {
        private var i = 0
        private val lock = ReentrantLock()
        // TODO: Make this configurable
        private val sampleSize = 10
        private var buffer = Array<Metric20?>(sampleSize) { null }

        /**
         * Put a new sample in the buffer.
         * The buffer is a ring buffer so if the client exceeds the available number of
         * samples, the oldest sample will be overwritten.
         */
        fun putSample(metric: Metric20) = lock.withLock {
            i = (i + 1) % sampleSize
            buffer[i] = metric
        }

        /**
         * Return a clean buffer with all the samples
         */
        fun flushData(): Array<Metric20?> = lock.withLock {
            val tempBuffer = Array<Metric20?>(sampleSize) { null }
            for (j in 0 until sampleSize) {
                tempBuffer[j] = buffer[(i + j) % sampleSize]
                buffer[i] = null
            }
            return tempBuffer
        }
    }

    private val binder = object : IHermesService.Stub() {

        override fun register(dataId: String?, unit: String?, mtype: String?, what: String?, device: String?): String =
            when {
                dataId == null -> ErrorCode.NO_DATA_ID.errorStr
                unit == null -> ErrorCode.NO_UNIT.errorStr
                mtype == null -> ErrorCode.NO_TYPE.errorStr
                clientRegistry.containsValue(Client(dataId, unit, mtype, what, device)) ->
                    ErrorCode.ALREADY_REGISTERED.errorStr
                else -> {
                    val uuid = UUID.randomUUID().toString()
                    clientRegistry[uuid] = Client(dataId, unit, mtype, what, device)
                    uuid
                }
            }

        override fun deregister(uuid: String?): String =
            when (uuid) {
                null -> ErrorCode.NO_UUID.errorStr
                else -> {
                    clientRegistry.remove(uuid)
                    ""
                }
            }

        override fun sendDataDouble(uuid: String?, value: Double, http_method: String?,
                                    http_code: String?, result: String?, stat: String?, direction: String?,
                                    file: String?, line: Int, env: String?): String =
            when {
                uuid == null -> ErrorCode.NO_UUID.errorStr
                !clientRegistry.containsKey(uuid) -> ErrorCode.NOT_REGISTERED.errorStr
                else -> {
                    Log.d(loggingTag, "Got a new sample from client with uuid $uuid")
                    // TODO: Add a check to ensure that the data are in the expected form
                    val client = clientRegistry[uuid] as Client
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
                !clientRegistry.containsKey(uuid) -> ErrorCode.NOT_REGISTERED.errorStr
                else -> {
                    Log.d(loggingTag, "Got a new sample from client with uuid $uuid")
                    // TODO: Add a check to ensure that the data are in the expected form
                    val client = clientRegistry[uuid] as Client
                    val newSample = Metric20(client.dataId, value.toString())
                        .setData(Metric20.TagKey.MTYPE, client.mtype)
                        .setData(Metric20.TagKey.UNIT, client.unit)
                    client.putSample(newSample)
                    ""
                }
            }
    }

    var iHermesService: IHermesService? = null

    val mConnectionToSelf = object : ServiceConnection {

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
    private val PRNG = SecureRandom.getInstanceStrong()
    private val channelId = PRNG.nextInt().toString()
    private var foregroundNotificationId: Int = 15970
    private var iotaConnector: IOTAConnector? = null
    private val clientRegistry = ConcurrentHashMap<String, Client>()

    @Inject
    lateinit var db: HermesRoomDatabase

    @Inject
    lateinit var repository: HermesRepository

    @Inject
    lateinit var app: HermesClientApp

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
            // Initialize connection
            // TODO: This should be provided as configuration and not be hard-coded
            iotaConnector = IOTAConnector(repository.getSeed() as Seed, app)
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
        super.onDestroy();
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
            .setSmallIcon(R.drawable.notification_tile_bg)
            .setWhen(System.currentTimeMillis())  // the time stamp
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
        for (i: Int in 0 until 10) {
            Log.d(loggingTag, "Generating a data sample for the Hermes Service")
            iHermesService?.sendDataDouble(uuid, (Random().nextInt() % 30).toDouble(), null, null,
                null, null, null, null, -1, null)
            delay(5 * 1000)
        }
    }

    private suspend fun broadcastData() {
        while (true) {
            Log.d(loggingTag, "Hermes service looking at the registered client data")
            for ((uuid, client) in clientRegistry) {
                Log.d(loggingTag, "Broadcasting data of client with id $uuid")
                if (iotaConnector != null) iotaConnector?.sendData(
                    *client.flushData(),
                    blockUntilConfirmation = true,
                    asyncConfirmation = true
                )
                else Log.d(loggingTag, "There is no connector to use to broadcast the data")
            }
            delay(10 * 1000)
        }
    }
}
