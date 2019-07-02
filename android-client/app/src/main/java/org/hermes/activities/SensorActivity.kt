package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import dagger.Module
import dagger.android.AndroidInjection
import kotlinx.android.synthetic.main.activity_sensor.*
import javax.inject.Inject

import org.hermes.*
import org.hermes.utils.addBackButton

class SensorActivity : BaseActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "SensorActivity"

    @Inject
    lateinit var sensorRepository: SensorRepository

    private lateinit var sensorUUID: TextView
    private lateinit var sensorID: TextView
    private lateinit var sensorType: TextView
    private lateinit var sensorUnit: TextView
    private lateinit var sensorWhat: TextView
    private lateinit var sensorDevice: TextView
    private lateinit var sensorActivateButton: Button
    private lateinit var sensorRootAddress: TextView
    private lateinit var sensorLatestAddress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor)

        sensorUUID = findViewById(R.id.sensorUUID)
        sensorID = findViewById(R.id.sensorID)
        sensorType = findViewById(R.id.sensorType)
        sensorUnit = findViewById(R.id.sensorUnit)
        sensorWhat = findViewById(R.id.sensorWhat)
        sensorDevice = findViewById(R.id.sensorDevice)
        sensorActivateButton = findViewById(R.id.sensorActivate)

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
        })
        sensorActivateButton.setOnClickListener {
            sensorRepository.eventBus.sendMessage(sensorRepository.eventBus.obtainMessage().apply {
                obj = Pair(SensorRepository.MessageType.FLIP_SENSOR, sensor)
            })
        }
    }
}