package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

import org.hermes.HermesRepository
import org.hermes.R

/**
 * A login screen that offers to access the app via pin.
 */
class LoginActivity : AppCompatActivity() {

    private val repository by lazy { HermesRepository.getInstance(application) }
    private val loggingTag = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (!repository.credentialsSet()) {
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

    private fun goToEventPage() {
        startActivity(Intent(this, EventActivity::class.java))
    }

    private fun authenticate() {
        Log.i(loggingTag, "Checking user's PIN")

        val pin = findViewById<EditText>(R.id.login_pin_input).toString()
        if (repository.checkPIN(pin)) {
            repository.unlockCredentials(pin)
            goToEventPage()
        } else {
            showError()
        }
    }
}
