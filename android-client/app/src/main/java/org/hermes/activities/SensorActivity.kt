package org.hermes.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import dagger.Module
import dagger.android.AndroidInjection
import org.hermes.*
import org.hermes.utils.addBackButton
import javax.inject.Inject


class SensorActivity : BaseActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "SensorActivity"

    @Inject
    lateinit var metadataRepository: MetadataRepository

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
                metadataRepository.fetchSensor(sensorId) { bind(it) }
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
        sensorActivateButton.text = when(sensor.active.get()) {
            true -> {
                sensorActivateButton.setBackgroundColor(green)
                deActivateText
            }
            false -> {
                sensorActivateButton.setBackgroundColor(red)
                activateText
            }
        }
        sensorActivateButton.setOnClickListener {
            when (sensorActivateButton.text) {
                activateText -> {
                    sensor.active.compareAndSet(false, true)
                    sensorActivateButton.text = deActivateText
                    sensorActivateButton.setBackgroundColor(green)
                    metadataRepository.refreshSensorList()
                }
                deActivateText -> {
                    sensor.active.compareAndSet(true, false)
                    sensorActivateButton.text = activateText
                    sensorActivateButton.setBackgroundColor(red)
                    metadataRepository.refreshSensorList()
                }
            }
        }
    }
}