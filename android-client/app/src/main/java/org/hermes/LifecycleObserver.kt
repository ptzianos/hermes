package org.hermes

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import org.hermes.activities.LoginActivity
import org.hermes.activities.SetupActivity
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class HermesLifeCycleObserver @Inject constructor(val repository: HermesRepository) {

    private val loggingTag = "HermesLifeCycleObserver"
    private var onScreenActivity: String? = null

    fun resume(activity: AppCompatActivity) {
        Log.d(loggingTag, "resuming: ${activity.javaClass.name}")
        if (onScreenActivity == null) {
            Log.d(loggingTag, "Starting application")
            if (repository.credentialsSet() && activity.javaClass != LoginActivity::class.java) {
                activity.startActivity(Intent(activity, LoginActivity::class.java))
            } else if (!repository.credentialsSet() && activity.javaClass != SetupActivity::class.java) {
                activity.startActivity(Intent(activity, SetupActivity::class.java))
            }
        }
        onScreenActivity = activity.javaClass.name
    }

    fun pause(activity: AppCompatActivity) {
        Log.d(loggingTag, "pausing: ${activity.javaClass.name}")
    }

    fun stop(activity: AppCompatActivity) {
        Log.d(loggingTag, "stopping: ${activity.javaClass.name}")
        if (activity.javaClass.name == onScreenActivity) {
            Log.d(loggingTag, "Exiting application")
            onScreenActivity = null
            repository.seal()
        }
    }

    fun destroy(activity: AppCompatActivity) {
        Log.d(loggingTag, "destroying: ${activity.javaClass.name}")
    }
}