package org.hermes.client

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log;
import android.widget.Button
import android.widget.EditText
import org.hermes.crypto.PasswordHasher

/**
 * A login screen that offers to access the app via pin.
 */
class LoginActivity : AppCompatActivity() {

    companion object {
        const val loggingTag = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val hashedPinKey = getString(R.string.auth_hashed_pin)
        val sharedPref = getSharedPreferences(getString(R.string.auth_hashed_pin), Context.MODE_PRIVATE)
        Log.d(loggingTag, "Creating activity")
        if (sharedPref.getString(hashedPinKey, "").isNullOrBlank()) {
            Log.d(loggingTag, "Login activity redirecting to Setup activity")
            goToSetupPage()
        } else {
            initCallbacks()
            initForm()
        }
    }

    private fun initForm() {
        Log.d(loggingTag, "Initializing forms")
        val pinInput = findViewById<EditText>(R.id.login_pin_input)
        pinInput.requestFocus()
    }

    private fun initCallbacks() {
        Log.d(loggingTag,"Initializing callbacks")
        val loginButton = findViewById<Button>(R.id.login_button)
        loginButton.setOnClickListener { authenticate()}
    }

    private fun showError() {
        val loginInput = findViewById<EditText>(R.id.login_pin_input)
        loginInput.error = "Incorrect PIN"
    }

    private fun goToSetupPage() {
        startActivity(Intent(this, SetupActivity::class.java))
    }

    private fun authenticate() {
        Log.i(loggingTag, "Checking user's PIN")
        val hashedPinKey = getString(R.string.auth_hashed_pin)
        val sharedPref = getSharedPreferences(getString(R.string.auth_preference_key), Context.MODE_PRIVATE)
        val storedHashedPin: String? = sharedPref.getString(hashedPinKey, "")
        if (storedHashedPin.isNullOrBlank()) {
            Log.e(loggingTag, "There was an error while retrieving stored PIN. Redirecting to Setup activity")
            goToSetupPage()
        }

        val pinFromForm = PasswordHasher.hashPassword(
                findViewById<EditText>(R.id.login_pin_input)
                        .toString()
                        .toCharArray()
        )
        if ((storedHashedPin as String).toByteArray().contentEquals(pinFromForm.hash)) {
            // TODO: Start session here
        } else {
            showError()
        }
    }
}