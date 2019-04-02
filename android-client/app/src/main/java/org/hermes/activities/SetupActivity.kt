package org.hermes.activities

import android.os.Bundle
import android.content.Context
import android.content.Intent
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import android.widget.EditText

import org.hermes.R


class SetupActivity : AppCompatActivity() {

    val loggingTag = "SetupActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        val sharedPref = getSharedPreferences(getString(R.string.auth_preference_key), Context.MODE_PRIVATE)
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

    private fun checkSetupForm() {
        var errorsInForm = false
        val pinSetupInput = findViewById<EditText>(R.id.setup_pin_input)
        if (pinSetupInput.text.isNullOrBlank()) {
            errorsInForm = true
            pinSetupInput.error = "This is not supposed to be empty"
        }
        val pinSetupInputVerify = findViewById<EditText>(R.id.setup_verify_pin_input)
        if (pinSetupInputVerify.text.isNullOrBlank()) {
            errorsInForm = true
            pinSetupInputVerify.error = "This is not supposed to be empty"
        }
        if (errorsInForm) {
            initForm()
        }
    }
}
