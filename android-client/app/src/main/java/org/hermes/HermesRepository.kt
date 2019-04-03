package org.hermes

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.room.Room
import kotlin.coroutines.EmptyCoroutineContext
import kotlinx.coroutines.*

import org.hermes.entities.Event
import org.hermes.iota.Seed
import org.hermes.R
import org.hermes.crypto.PasswordHasher


class HermesRepository(private val application: Application) {

    private val loggingTag = "HermesRepository"

    private val db = Room.databaseBuilder(application, HermesRoomDatabase::class.java, "my-room-database")
                         .fallbackToDestructiveMigration()
                         .build()
    private var seed: Seed? = null

    private val sharedPref = application.getSharedPreferences(application.getString(R.string.auth_preference_key),
                                                              Context.MODE_PRIVATE)

    val eventDao = db.eventDao()

    companion object {
        @Volatile private var instance: HermesRepository? = null

        fun getInstance(application: Application) =
            instance ?: synchronized(this) {
                instance ?: HermesRepository(application).also { instance = it }
            }
    }

    /**
     * Insert some events in a background thread.
     *
     * Doing an insert in the UI thread would throw an exception.
     */
    fun insertEvent(vararg events: Event) {
        CoroutineScope(Dispatchers.IO).launch(EmptyCoroutineContext, CoroutineStart.DEFAULT) {
            db.eventDao().insertAll(*events)
        }
    }

    fun generateCredentials(pin: String): Boolean {
        val sharedPref = application.getSharedPreferences(application.getString(R.string.auth_preference_key),
                                                          Context.MODE_PRIVATE)
        val hashedPin = PasswordHasher.hashPassword(pin.toCharArray())
        val seed = Seed.new()
        // TODO: this needs to be encrypted with the PIN
        val success = sharedPref.edit()
            .putString(application.getString(R.string.auth_hashed_pin), hashedPin.toString())
            .putString(application.getString(R.string.auth_seed), seed.toString())
            .commit()
        if (!success) {
            Log.e(loggingTag, "There was come error while trying to store the user's hashed pin")
        } else {
            Log.i(loggingTag, "Committed successfully the hashed pin and the seed of the user")
        }
        this.seed = seed
        return success
    }

    /**
     * Returns true if the PIN matches the stored hash, false otherwise.
     * It does not check if there is a stored hash to compare against. The caller of the function
     * must perform these checks.
     */
    fun checkPIN(pin: String): Boolean {
        val hashedPin = PasswordHasher.hashPassword(pin.toCharArray())
        val storedPin = sharedPref.getString(application.getString(R.string.auth_hashed_pin), "")
        return storedPin != null && (storedPin as String).toByteArray().contentEquals(hashedPin.hash)
    }

    /**
     * Returns true if the necessary credentials of the application are available,
     * false otherwise.
     */
    fun credentialsSet(): Boolean {
        val seedAvailable = sharedPref.getString(application.getString(R.string.auth_seed), "")
                                      .isNullOrBlank()
        val hashedPinAvailable = sharedPref.getString(application.getString(R.string.auth_hashed_pin), "")
                                           .isNullOrBlank()
        if (seedAvailable != hashedPinAvailable) {
            Log.e(loggingTag, "Application is in an incorrect state. Either seed or pin not available!")
        }
        return seedAvailable && hashedPinAvailable
    }

    /**
     * Decrypt the credentials using the user's PIN
     */
    fun unlockCredentials(pin: String) {
        // TODO: Implement this
    }
}
