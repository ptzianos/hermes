package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import dagger.Module
import dagger.android.AndroidInjection
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.hermes.*


class SetupLoadActivity : BaseActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "SetupLoadActivity"

    @Inject
    lateinit var repository: HermesRepository

    @Inject
    lateinit var marketRepository: MarketRepository

    @Inject
    lateinit var cryptoRepository: CryptoRepository

    private val setupLoaderMessage: TextView by lazy { findViewById<TextView>(R.id.setupLoaderMessage) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_load)
        startPipeline(intent?.extras?.getString("pin") as String)
    }

    private fun startPipeline(pin: String) {
        CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
            generateCredentials(pin)
            setupLoaderMessage.text = "Registering agent with the marketplace. Please don't close the app"
            if (marketRepository.registerUser()) {
                showErrorMessage()
                rollbackCredentials()
                delay(1000)
                goToSetupPage()
            } else {
                setupLoaderMessage.text = "Done with the registration. Have fun!"
                delay(1000)
                goToDashboard()
            }
        }
    }

    private fun generateCredentials(pin: String) {
        setupLoaderMessage.text = "Generating seed and key pair. Please don't close app"
        cryptoRepository.generateCredentials(pin)
        cryptoRepository.unseal(pin)
    }

    private fun rollbackCredentials() = cryptoRepository.clearCredentials()

    private fun showErrorMessage() {
        setupLoaderMessage.text = "There was an error. Please retry later to setup " +
                "and make sure you have an active Internet connection"
    }

    private fun goToSetupPage() {
        startActivity(Intent(this, SetupActivity::class.java))
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DrawerActivity::class.java))
    }
}
