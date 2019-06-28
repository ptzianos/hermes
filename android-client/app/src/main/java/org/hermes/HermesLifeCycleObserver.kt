package org.hermes

import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import javax.inject.Inject
import javax.inject.Singleton

import org.hermes.activities.LoginActivity
import org.hermes.activities.SetupActivity
import org.hermes.activities.SetupLoadActivity


@Singleton
class HermesLifeCycleObserver @Inject constructor(
    private val cryptoRepository: CryptoRepository,
    private val marketRepository: MarketRepository) {

    private val loggingTag = "HermesLifeCycleObserver"
    private var onScreenActivity: AppCompatActivity? = null
    private var redirect: AppCompatActivity? = null
    private var currentState: State? = null

    fun resume(activity: AppCompatActivity) {
        Log.d(loggingTag, "resuming: ${activity.javaClass.name}")
        evaluateState()
        val activityName = activity.javaClass.name
        val initActivity = activityName == SetupActivity::class.java.name
                || activityName == SetupLoadActivity::class.java.name
        val intent = when {
            currentState == State.UNINITIALIZED &&  activityName != SetupActivity::class.java.name ->
                Intent(activity, SetupActivity::class.java)
            currentState == State.SEEDED || currentState == State.REGISTERED ->
                // Need to keep setting up the app, but the pin has not been loaded
                if (activityName != LoginActivity::class.java.name && cryptoRepository.sealed())
                    Intent(activity, LoginActivity::class.java).apply{ putExtra("redirect", redirect?.javaClass?.name) }
                // Pin has been loaded but user is not directed to the SetupLoad activity
                else if (activityName != SetupLoadActivity::class.java.name && cryptoRepository.unsealed())
                    Intent(activity, SetupLoadActivity::class.java)
                else null
            currentState == State.FIRST_TOKEN_ACQUIRED && (onScreenActivity == null || initActivity) -> Intent(activity, LoginActivity::class.java)
            else -> null
        }
        if (intent != null) activity.startActivity(intent)
        onScreenActivity = activity
        Log.d(loggingTag, "On screen activity set to $activityName")
    }

    fun pause(activity: AppCompatActivity) {
        Log.d(loggingTag, "pausing: ${activity.javaClass.name}")
    }

    fun stop(activity: AppCompatActivity) {
        Log.d(loggingTag, "stopping: ${activity.javaClass.name}")
        if (activity == onScreenActivity) {
            Log.d(loggingTag, "Setting on screen activity to null")
            redirect = activity
            onScreenActivity = null
        }
    }

    fun destroy(activity: AppCompatActivity) = Log.d(loggingTag, "destroying: ${activity.javaClass.name}")

    private fun evaluateState() {
        currentState = when {
            marketRepository.tokenAcquired() -> State.FIRST_TOKEN_ACQUIRED
            marketRepository.registered() -> State.REGISTERED
            cryptoRepository.credentialsGenerated() -> State.SEEDED
            else -> State.UNINITIALIZED
        }
    }

    fun getCurrentState(): State? = currentState
}