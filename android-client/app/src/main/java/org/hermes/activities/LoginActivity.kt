package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import dagger.Module
import dagger.android.AndroidInjection
import javax.inject.Inject
import kotlinx.android.synthetic.main.activity_login.*

import org.hermes.*
import org.hermes.utils.afterTextChanged
import org.hermes.viewmodels.LoginViewModel


/**
 * A login screen that offers to access the app via pin.
 */
class LoginActivity: BaseActivity() {

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
        viewModel = hermesApplication.daggerHermesComponent.getLoginViewModel()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        initForm()
        login_pin_input.afterTextChanged { text -> viewModel.pin = text?.toString() ?: "" }
        viewModel.isFormValid.observe(this, Observer { valid ->
            if (valid) {
                login_pin_input.error = null
                startActivity(Intent(this, DrawerActivity::class.java))
            }
        })
    }

    override fun onResume() {
        super.onResume()
        initForm()
    }

    private fun initForm() {
        viewModel.pin = null
        login_pin_input.text = null
        login_pin_input.requestFocus()
    }
}
