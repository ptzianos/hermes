package org.hermes.activities

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import org.hermes.crypto.PasswordHasher
import org.hermes.HermesRepository
import org.hermes.iota.Seed
import org.hermes.R


class SetupActivity : AppCompatActivity() {

    val loggingTag = "SetupActivity"
    var repository: HermesRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        val sharedPref = getSharedPreferences(getString(R.string.auth_preference_key), Context.MODE_PRIVATE)
        repository = HermesRepository.getInstance(application)
        if (sharedPref.contains("hashedPin")) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            initCallbacks()
            initForm()
        }
    }

    private fun initForm() {
        val pinSetupInput = findViewById<EditText>(R.id.setup_pin_input)
        pinSetupInput.setText("")
        val pinSetupInputVerify = findViewById<EditText>(R.id.setup_verify_pin_input)
        pinSetupInputVerify.setText("")
    }

    private fun initCallbacks() {
        val setupButton = findViewById<Button>(R.id.setup_button)
        setupButton.setOnClickListener { checkSetupForm() }
    }

    private fun storePin(pin: String) {
        Log.i(loggingTag, "Setting up newly created user's PIN")
        val hashedPinKey = getString(R.string.auth_hashed_pin)
        // TODO: this needs to be encrypted with the pin
        val seedKey = getString(R.string.auth_seed)
        val sharedPref = getSharedPreferences(getString(R.string.auth_preference_key), Context.MODE_PRIVATE)
        val hashedPin = PasswordHasher.hashPassword(pin.toCharArray())
        val seed = Seed.new()
        val success = sharedPref.edit()
            .putString(hashedPinKey, hashedPin.toString())
            .putString(seedKey, seed.toString())
            .commit()
        if (!success) {
            Log.e(loggingTag, "There was come error while trying to store the user's hashed pin")
        } else {
            Log.i(loggingTag, "Committed successfully the hashed pin and the seed of the user")
        }
        repository?.handoverSeed(seed)
    }

    private fun goToEventPage() {
        startActivity(Intent(this, EventActivity::class.java))
    }

    private fun checkSetupForm() {
        var errorsInForm = false
        val pinSetupInput = findViewById<EditText>(R.id.setup_pin_input)
        if (pinSetupInput.text.isNullOrBlank()) {
            errorsInForm = true
            pinSetupInput.error = "This is not supposed to be empty"
        } else if (!pinSetupInput.text.map { c -> c.isDigit() }.reduce { b1, b2 -> b1 && b2 }) {
            errorsInForm = true
            pinSetupInput.error = "PIN must be all digits"
        }
        val pinSetupInputVerify = findViewById<EditText>(R.id.setup_verify_pin_input)
        if (pinSetupInputVerify.text.isNullOrBlank()) {
            errorsInForm = true
            pinSetupInputVerify.error = "This is not supposed to be empty"
        } else if (!pinSetupInput.text.isNullOrBlank() && pinSetupInput.toString().equals(pinSetupInputVerify.toString())) {
            errorsInForm = true
            pinSetupInputVerify.error = "The two fields must be equal"
        }
        if (!errorsInForm) {
            storePin(pinSetupInput.toString())
            goToEventPage()
        }
    }
}
