package org.hermes.client

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import org.hermes.crypto.PasswordHasher

/**
 * A login screen that offers to access the app via pin.
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val hashedPinKey = getString(R.string.auth_hashed_pin)
        val sharedPref = getSharedPreferences(getString(R.string.auth_hashed_pin), Context.MODE_PRIVATE)
        if (sharedPref.getString(hashedPinKey, "").isNullOrBlank()) {
            goToSetupPage()
        } else {
            initCallbacks()
            initForm()
        }
    }

    private fun initForm() {
        val pinInput = findViewById<EditText>(R.id.login_pin_input)
        pinInput.requestFocus()
    }

    private fun initCallbacks() {
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
        val hashedPinKey = getString(R.string.auth_hashed_pin)
        val sharedPref = getSharedPreferences(getString(R.string.auth_preference_key), Context.MODE_PRIVATE)
        val storedHashedPin: String? = sharedPref.getString(hashedPinKey, "")
        if (storedHashedPin.isNullOrBlank()) {
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
