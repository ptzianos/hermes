package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
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
    lateinit var metadataRepository: MetadataRepository

    @Inject
    lateinit var marketRepository: MarketRepository

    @Inject
    lateinit var cryptoRepository: CryptoRepository

    private val setupLoaderMessage: TextView by lazy { findViewById<TextView>(R.id.setupLoaderMessage) }

    private val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(inputMessage: Message) {
            setupLoaderMessage.text = inputMessage.obj as String
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_load)
        startPipeline(intent?.extras?.getString("pin") as String)
    }

    private fun startPipeline(pin: String) {
        CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
            handler
                .obtainMessage(0, "Generating seed and key pair. Please don't close app")
                .apply { sendToTarget() }
            cryptoRepository.generateCredentials(pin)
            cryptoRepository.unseal(pin)
            handler
                .obtainMessage(0, "Registering agent with the marketplace. Please don't close the app")
                .apply { sendToTarget() }
            if (!marketRepository.registerUser()) {
                handler
                    .obtainMessage(0, "There was an error. Please retry later to setup and make sure you " +
                            "have an active Internet connection")
                    .apply { sendToTarget() }
                rollbackCredentials()
                delay(5 * 1000)
                goToSetupPage()
            } else {
                handler
                    .obtainMessage(0, "Done with the registration. Have fun!")
                    .apply { sendToTarget() }
                delay(1000)
                metadataRepository.startLedgerService()
                goToDashboard()
            }
        }
    }

    private fun rollbackCredentials() = cryptoRepository.clearCredentials()

    private fun goToSetupPage() {
        startActivity(Intent(this, SetupActivity::class.java))
    }

    private fun goToDashboard() {
        startActivity(Intent(this, DrawerActivity::class.java))
    }
}
