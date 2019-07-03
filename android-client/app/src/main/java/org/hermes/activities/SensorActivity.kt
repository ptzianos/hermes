package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import com.google.android.material.snackbar.Snackbar
import dagger.Module
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_sensor.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

import org.hermes.*
import org.hermes.utils.addBackButton


class SensorActivity : BaseActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "SensorActivity"

    @Inject
    lateinit var sensorRepository: SensorRepository

    @Inject
    lateinit var marketRepository: MarketRepository

    @Inject
    lateinit var hermesApplication: HermesClientApp

    private lateinit var sensorUUID: TextView
    private lateinit var sensorID: TextView
    private lateinit var sensorType: TextView
    private lateinit var sensorUnit: TextView
    private lateinit var sensorWhat: TextView
    private lateinit var sensorDevice: TextView
    private lateinit var sensorActivateButton: Button
    private lateinit var sensorAdvertiseButton: Button
    private lateinit var sensorRootAddress: TextView
    private lateinit var sensorLatestAddress: TextView
    private lateinit var sensorInfoLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

        sensorInfoLayout = findViewById(R.id.sensorInfoLayout)
        sensorUUID = findViewById(R.id.sensorUUID)
        sensorID = findViewById(R.id.sensorID)
        sensorType = findViewById(R.id.sensorType)
        sensorUnit = findViewById(R.id.sensorUnit)
        sensorWhat = findViewById(R.id.sensorWhat)
        sensorDevice = findViewById(R.id.sensorDevice)
        sensorActivateButton = findViewById(R.id.sensorActivate)
        sensorAdvertiseButton = findViewById(R.id.sensorAdvertiseButton)

        sensorRootAddress = findViewById(R.id.rootAddress)
        sensorLatestAddress = findViewById(R.id.latestAddress)

        val extras = intent.extras
        if (extras != null) {
            val sensorId = extras.getString("sensorId", "")
            if (sensorId.isNullOrBlank()) {
                Toast.makeText(this, "No sensor provided", Toast.LENGTH_LONG)
                    .show()
                startActivity(Intent(this, SensorListActivity::class.java))
            } else {
                sensorRepository.fetchSensor(sensorId) { bind(it) }
            }
        }

        addBackButton(findViewById(R.id.toolbar), resources) {
            val intent = Intent(this, DrawerActivity::class.java)
            intent.putExtra("tile", DrawerActivity.Tile.SENSOR_LIST.i)
            startActivity(intent)
        }
    }

    private fun bind(sensor: LedgerService.Sensor) {
        sensorUUID.text = sensor.uuid
        sensorID.text = sensor.dataId
        sensorType.text = sensor.mtype
        sensorUnit.text = sensor.unit
        sensorDevice.text = sensor.device
        val activateText = "Activate"
        val deActivateText = "Deactivate"
        val green = ResourcesCompat.getColor(resources, R.color.green, null)
        val red = ResourcesCompat.getColor(resources, R.color.red, null)
        val grey = ResourcesCompat.getColor(resources, R.color.secondaryTextColor, null)

        sensorRepository.registry[sensor.uuid]?.active?.getLiveData()?.observe(this, Observer<Boolean> {
            sensorActivateButton.text = if (it) {
                sensorActivateButton.setBackgroundColor(green)
                deActivateText
            } else {
                sensorActivateButton.setBackgroundColor(red)
                activateText
            }
        })
        sensorRepository.latestAddresses[sensor.uuid]?.observe(this, Observer<String> {
            latestAddress.text = it ?: ""
        })
        sensorRepository.rootAddresses[sensor.uuid]?.observe(this, Observer<String> {
            rootAddress.text = it ?: ""
            sensorAdvertiseButton.setBackgroundColor(if (it == null || it == "") grey else red)
        })
        sensorActivateButton.setOnClickListener {
            sensorRepository.eventBus.sendMessage(sensorRepository.eventBus.obtainMessage().apply {
                obj = Pair(SensorRepository.MessageType.FLIP_SENSOR, sensor)
            })
        }
        sensorAdvertiseButton.setOnClickListener {
            if (sensorRootAddress.text == null || sensorRootAddress.text.isBlank())
                Snackbar.make(sensorInfoLayout, "Can not advertise sensor before streaming starts", Snackbar.LENGTH_LONG).show()
            else {
                CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
                    marketRepository.postOrPingAd(sensor, rootAddress = sensorRootAddress.text.toString(),
                        toastHandler = hermesApplication.toastHandler,
                        callback = {})
                }
            }
        }
    }
}