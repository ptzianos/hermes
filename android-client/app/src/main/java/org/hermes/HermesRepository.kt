package org.hermes

import android.app.*
import android.content.Context
import android.content.Intent
import android.util.Log
import java.security.KeyPair

import org.hermes.crypto.PasswordHasher
import org.hermes.iota.Seed


class HermesRepository(private val application: Application) {

    private val loggingTag = "HermesRepository"

    val db = HermesRoomDatabase.getDatabase(application)
    private var seed: Seed? = null
    private var keypair: KeyPair? = null
    private var unsealed: Boolean = false
    private var ledgerService: LedgerService? = null
    private var ledgerServiceRunning: Boolean = false

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

    fun generateCredentials(pin: String): Boolean {
        val sharedPref = application.getSharedPreferences(application.getString(R.string.auth_preference_key),
                                                          Context.MODE_PRIVATE)
        val hashedPin = PasswordHasher.hashPassword(pin.toCharArray())
        val seed = Seed.new()
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

    fun unsealed(): Boolean {
        return unsealed
    }

    /**
     * Decrypt the credentials using the user's PIN
     */
    fun unlockCredentials(pin: String) {
        if (!unsealed) {
            Log.i(loggingTag, "Unlocking credentials of the application")
            startLedgerService()
            unsealed = true
            // TODO: Implement unsealing of encrypted credentials
        }
    }

    /**
     * Start the LedgerService if it's not running already
     */
    private fun startLedgerService() {
        if (!ledgerServiceRunning) {
            Log.i(loggingTag,"Ledger service is not running. Starting it now")
            ledgerService = LedgerService()
            val intent = Intent(application.applicationContext, LedgerService::class.java)
            application.startForegroundService(intent)
            ledgerServiceRunning = true
        } else {
            Log.i(loggingTag, "Ledger service is already running")
        }
    }

    fun getSeed(): Seed? {
        return seed
    }
}
