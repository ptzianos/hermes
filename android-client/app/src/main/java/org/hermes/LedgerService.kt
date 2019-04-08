package org.hermes

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Messenger
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.*

import org.hermes.activities.LoginActivity
import org.hermes.entities.Event
import org.hermes.iota.Seed
import org.hermes.ledgers.IOTAConnector
import java.util.*


class LedgerService : Service() {

    enum class MessageType(val type: Int) {
        REGISTER_CLIENT(1), UNREGISTER_CLIENT(2), SEND_SAMPLE(3)
    }

    /**
     * Class for clients to access.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with
     * IPC.
     */
    inner class LocalBinder: Binder() {
        fun getService(): LedgerService {
            return this@LedgerService
        }
    }

    private val loggingTag = "LedgerService"
    private val db: Lazy<HermesRoomDatabase> = lazy { HermesRoomDatabase.getDatabase(applicationContext) }
    private val repository = lazy { HermesRepository.getInstance(application) }
    private var mNotificationManager: NotificationManager? = null
    private val mBinder: LocalBinder? = null
    private var mClients = ArrayList<Messenger>()
//    private val mMessenger = Messenger(IncomingHandler())
    private val PRNG = SecureRandom.getInstanceStrong()
    private val channelId = PRNG.nextInt().toString()
    private var foregroundNotificationId: Int = 15970
    private var iotaConnector: IOTAConnector? = null

    override fun onCreate() {
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(loggingTag, "Received start id $startId: $intent")
        Log.i(loggingTag, "Showing notification for Hermes service foregrounding")
        startForeground(foregroundNotificationId, buildNotification())
        try {
            // Initialize connection
            // TODO: This should be provided as configuration and not be hard-coded
            iotaConnector = IOTAConnector(protocol = "https", uri = "nodes.thetangle.org",
                port = "443", seed = repository.value.getSeed() as Seed, db = db.value
            )
            // Start coroutine to broadcast data
            CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch { broadcastData() }
        } catch (e: java.lang.Exception) {
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
//        bindService(Intent(Binding.this,
//            MessengerService.class), mConnection, Context.BIND_AUTO_CREATE)
//        mIsBound = true;
//        mCallbackText.setText("Binding.");
        return mBinder
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

    private suspend fun generateRandomEvents() {
        for (i: Int in 0 until 100) {
            Log.i(loggingTag, "Generating event $i")
            val event = Event(action = "random", resource = "iota", extraInfo = "whatevs")
            db.value.eventDao().insertAll(event)
            delay(1000)
        }
    }

    private suspend fun broadcastData() {
        for (i: Int in 0 until 100) {
            if (iotaConnector != null) {
                Log.i(loggingTag, "Generating data sample $i")
                iotaConnector?.sendData(
                    Metric20("pavlos.android.random", Random().nextInt() % 30)
                        .setData(Metric20.TagKey.MTYPE, "int")
                        .setData(Metric20.TagKey.UNIT, "random_source"),
                    blockUntilConfirmation = true, asyncConfirmation = true)
            }
            delay(5 * 1000)
        }
    }

}
