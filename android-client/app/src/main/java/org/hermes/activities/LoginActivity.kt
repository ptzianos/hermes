package org.hermes.activities

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import dagger.android.AndroidInjection
import dagger.Module
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_login.*
import org.hermes.HermesClientApp

import org.hermes.HermesRepository
import org.hermes.R
import org.hermes.utils.afterTextChanged
import org.hermes.viewmodels.LoginViewModel


/**
 * A login screen that offers to access the app via pin.
 */
class LoginActivity: AppCompatActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "LoginActivity"

    lateinit var viewModel: LoginViewModel

    @Inject
    lateinit var repository: HermesRepository

    @Inject
    lateinit var hermesApplication: HermesClientApp

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        viewModel = hermesApplication.daggerHermesComponent.providesLoginViewModel()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (!repository.credentialsSet()) {
            Log.d(loggingTag, "Login activity redirecting to Setup activity")
            goToSetupPage()
        } else {
            initForm()
            login_pin_input.afterTextChanged { text -> viewModel.pin = text?.toString() ?: "" }
            viewModel.isFormValid.observe(this, Observer { valid ->
                if (valid) {
                    login_pin_input.error = null
                    repository.unlockCredentials(login_pin_input.text.toString())
                    startActivity(Intent(this, EventActivity::class.java))
                }
            })
        }
    }

    override fun onResume() {
        super.onResume()
        if (!repository.credentialsSet()) {
            Log.d(loggingTag, "Login activity redirecting to Setup activity")
            goToSetupPage()
        } else {
            initForm()
        }
    }

    private fun initForm() {
        viewModel.pin = null
        login_pin_input.text = null
        login_pin_input.requestFocus()
    }

    private fun goToSetupPage() {
        startActivity(Intent(this, SetupActivity::class.java))
    }
}
