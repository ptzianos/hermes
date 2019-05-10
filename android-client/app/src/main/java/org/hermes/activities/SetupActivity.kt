package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import dagger.android.AndroidInjection
import dagger.Module
import javax.inject.Inject

import org.hermes.BaseActivity
import org.hermes.HermesRepository
import org.hermes.R


class SetupActivity : BaseActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "SetupActivity"

    @Inject
    lateinit var repository: HermesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup)
        initCallbacks()
    }

    override fun onResume() {
        super.onResume()
        if (repository.credentialsSet()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
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

    private fun goToDashBoard() {
        startActivity(Intent(this, DrawerActivity::class.java))
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
        if (!errorsInForm && repository.generateCredentials(pinSetupInput.text.toString())) {
            repository.unseal(pinSetupInput.text.toString())
            goToDashBoard()
        }
    }
}
