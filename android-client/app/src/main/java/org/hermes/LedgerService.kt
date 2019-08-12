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
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlinx.coroutines.*

import org.hermes.activities.LoginActivity
import org.hermes.entities.Sensor
import org.hermes.ledgers.IOTAConnector
import org.hermes.repositories.CryptoRepository
import org.hermes.repositories.MarketRepository
import org.hermes.repositories.MetadataRepository
import org.hermes.repositories.SensorRepository
import org.hermes.sensors.RandomSensor
import org.hermes.sensors.StressTestingSensor
import org.hermes.utils.applyIfNotEmpty
import org.hermes.utils.mapIfNotEmpty


class LedgerService : Service() {

    @Module
    abstract class DaggerModule

    private val binder = object : IHermesService.Stub() {

        override fun register(dataId: String?, unit: String?, mtype: String?, what: String?, device: String?): String =
            when {
                dataId == null -> ErrorCode.NO_DATA_ID.errorStr
                unit == null -> ErrorCode.NO_UNIT.errorStr
                mtype == null -> ErrorCode.NO_TYPE.errorStr
                dataId.startsWith(".") -> ErrorCode.INVALID_DATA_ID.errorStr
                else -> {
                    // Search the dao in the db
                    var uuid = sensorRepository.reverseRegistry.getOrDefault(dataId, "")
                    if (uuid.isEmpty()) {
                        uuid = UUID.randomUUID().toString()
                        val newSensor = Sensor(uuid, dataId, unit, mtype, what, device, "", "", 1000)
                        sensorRepository.eventBus.sendMessage(sensorRepository.eventBus.obtainMessage().apply {
                            obj = Pair(SensorRepository.MessageType.ADD_SENSOR, newSensor) })
                        Log.i(loggingTag, "A new sensor has registered with the application with uuid $uuid")
                    }
                    uuid
                }
            }

        override fun deregister(uuid: String?): String =
            when (uuid) {
                null -> ErrorCode.NO_UUID.errorStr
                else -> {
                    val sensor = sensorRepository.registry.remove(uuid)
                    if (sensor != null) sensorRepository.eventBus.sendMessage(
                        sensorRepository.eventBus.obtainMessage().apply {
                            obj = Pair(SensorRepository.MessageType.REMOVE_SENSOR, sensor) }
                    )
                    ""
                }
            }

        override fun sendDataDouble(uuid: String?, value: Double, http_method: String?,
                                    http_code: String?, result: String?, stat: String?, direction: String?,
                                    file: String?, line: Int, env: String?): String =
            when {
                uuid == null -> ErrorCode.NO_UUID.errorStr
                !sensorRepository.registry.containsKey(uuid) -> ErrorCode.NOT_REGISTERED.errorStr
                cryptoRepository.sealed() -> ErrorCode.SEALED.errorStr
                else -> {
                    Log.d(loggingTag, "Got a new sample from sensor with uuid $uuid")
                    val client = sensorRepository.registry[uuid] as Sensor
                    val newSample = Metric20(
                        "${cryptoRepository.pkHash.slice(0 until 10)}.${client.dataId}", value)
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
                !sensorRepository.registry.containsKey(uuid) -> ErrorCode.NOT_REGISTERED.errorStr
                cryptoRepository.sealed() -> ErrorCode.SEALED.errorStr
                else -> {
                    Log.d(loggingTag, "Got a new sample from sensor with uuid $uuid")
                    val client = sensorRepository.registry[uuid] as Sensor
                    val newSample = Metric20(
                        "${cryptoRepository.pkHash.slice(0 until 10)}.${client.dataId}", value.toString())
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
    private var broadcastStart: Long? = null

    @Inject
    lateinit var db: HermesRoomDatabase

    @Inject
    lateinit var cryptoRepository: CryptoRepository

    @Inject
    lateinit var metadataRepository: MetadataRepository

    @Inject
    lateinit var sensorRepository: SensorRepository

    @Inject
    lateinit var randomSensor: RandomSensor

    private val stressTestingSensors by lazy { Array(10) { StressTestingSensor(sensorRepository, it + 3) }}

    @Inject
    lateinit var app: HermesClientApp

    private val iotaConnector by lazy {
        IOTAConnector(cryptoRepository.IOTASeed(), cryptoRepository.privateKey()!!, app)
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

    private fun generateData() {
        randomSensor.beginScrappingData(this)
        stressTestingSensors.forEach { it.beginScrappingData(this) }
    }

    private suspend fun broadcastData() {
        while (true) {
            if (!metadataRepository.ledgerServiceBroadcasting.get()) {
                Log.d(loggingTag, "Ledger service skipping broadcasting data for now")
                broadcastStart = null
            } else {
                Log.d(loggingTag, "Hermes service looking at the registered client data")
                if (broadcastStart == null) {
                    broadcastStart = System.currentTimeMillis()
                    metadataRepository.ledgerServiceUptime.postValue(0)
                } else {
                    metadataRepository.ledgerServiceUptime.postValue(((System.currentTimeMillis() - broadcastStart as Long) / (60 * 1000)).toInt())
                }
                for ((uuid, client) in sensorRepository.registry.filter { it.value.active.get() }) {
                    client.flushData()
                        .mapIfNotEmpty {
                            iotaConnector.sendData(it, clientUUID = uuid,
                                blockUntilConfirmation = true, asyncConfirmation = true)}
                        .applyIfNotEmpty { client.returnSamples(it) }
                }
            }
            delay(10 * 1000)
        }
    }
}
