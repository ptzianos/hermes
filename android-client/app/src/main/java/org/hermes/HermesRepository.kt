package org.hermes

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import java.security.KeyPair
import javax.inject.Inject
import javax.inject.Singleton

import org.hermes.crypto.PasswordHasher
import org.hermes.iota.Seed
import org.hermes.service.LedgerService


@Singleton
class HermesRepository @Inject constructor(val application: Application,
                                           val db: HermesRoomDatabase,
                                           val sharedPref: SharedPreferences) {

    private val loggingTag = "HermesRepository"

    private var seed: Seed? = null
    private var keypair: KeyPair? = null
    private var unsealed: Boolean = false
    private var ledgerServiceRunning: Boolean = false

    fun generateCredentials(pin: String): Boolean {
        val hashedPin = PasswordHasher.hashPassword(pin.toCharArray()).toString()
        seed = Seed.new()
        val success = sharedPref.edit()
            .putString(application.getString(R.string.auth_hashed_pin), hashedPin)
            .putString(application.getString(R.string.auth_seed), seed.toString())
            .commit()
        // TODO: Throw a more visible error over here
        if (!success) Log.e(loggingTag, "There was some error while trying to store the user's hashed pin")
        else Log.i(loggingTag, "Committed successfully the hashed pin and the seed of the user")
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
        return storedPin == hashedPin.toString()
    }

    /**
     * Returns true if the necessary credentials of the application are available,
     * false otherwise.
     */
    fun credentialsSet(): Boolean {
        val seedEmpty = sharedPref.getString(application.getString(R.string.auth_seed), "")
                                  .isNullOrBlank()
        val hashedPinEmpty = sharedPref.getString(application.getString(R.string.auth_hashed_pin), "")
                                       .isNullOrBlank()
        if (seedEmpty != hashedPinEmpty) {
            Log.e(loggingTag, "Application is in an incorrect state. Either seed or pin not available!")
        }
        return !(seedEmpty && hashedPinEmpty)
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
            val seedString = sharedPref.getString(application.getString(R.string.auth_seed), seed.toString())
            // TODO: notify the called that something failed over here
            seedString ?: return
            seed = Seed(seedString.toCharArray())
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
