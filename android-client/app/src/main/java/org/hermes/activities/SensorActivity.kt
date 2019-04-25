package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import dagger.Module
import dagger.android.AndroidInjection
import javax.inject.Inject

import org.hermes.BaseActivity
import org.hermes.HermesRepository
import org.hermes.LedgerService
import org.hermes.R


class SensorActivity : BaseActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "SensorActivity"

    @Inject
    lateinit var repository: HermesRepository

    private lateinit var sensorUUID: TextView
    private lateinit var sensorID: TextView
    private lateinit var sensorType: TextView
    private lateinit var sensorUnit: TextView
    private lateinit var sensorWhat: TextView
    private lateinit var sensorDevice: TextView
    private lateinit var sensorActivateButton: Button

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

        val extras = intent.extras
        if (extras != null) {
            val sensorId = extras.getString("sensorId", "")
            if (sensorId.isNullOrBlank()) {
                Toast.makeText(this, "No sensor provided", Toast.LENGTH_LONG)
                    .show()
                startActivity(Intent(this, SensorListActivity::class.java))
            } else {
                repository.fetchSensor(sensorId) { bind(it) }
            }
        }
    }

    private fun bind(sensor: LedgerService.Sensor) {
        sensorUUID.text = sensor.uuid
        sensorID.text = sensor.dataId
        sensorType.text = sensor.mtype
        sensorUnit.text = sensor.unit
        sensorDevice.text = sensor.device
        if (sensor.active) {
            sensorActivateButton.text = "Deactivate"
        } else {
            sensorActivateButton.text = "Activate"
        }
    }
}