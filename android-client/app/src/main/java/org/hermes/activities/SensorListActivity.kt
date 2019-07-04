package org.hermes.activities

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.RecyclerView
import dagger.Module
import dagger.android.AndroidInjection
import javax.inject.Inject

import org.hermes.BaseActivity
import org.hermes.HermesClientApp
import org.hermes.R
import org.hermes.adapters.SensorListViewAdapter
import org.hermes.entities.Sensor
import org.hermes.viewmodels.SensorListViewModel


class SensorListActivity : BaseActivity() {

    @Module
    abstract class DaggerModule

    private val loggingTag = "SensorListActivity"

    @Inject
    lateinit var viewModel: SensorListViewModel

    @Inject
    lateinit var hermesApplication: HermesClientApp

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensor_list)

        val recyclerView: RecyclerView = findViewById(R.id.sensorFragment)

        viewModel.sensors.observe(this, Observer<List<Sensor>?> {
            (recyclerView.adapter as SensorListViewAdapter).submitList(it)
        })
    }
}
