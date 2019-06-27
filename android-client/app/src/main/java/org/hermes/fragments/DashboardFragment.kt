package org.hermes.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.Observer
import dagger.Module
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

import org.hermes.HermesClientApp
import org.hermes.MetadataRepository
import org.hermes.R
import org.hermes.viewmodels.DashboardViewModel


class DashboardFragment @Inject constructor() : Fragment() {

    @Module
    abstract class DaggerModule

    @Inject
    lateinit var application: HermesClientApp

    @Inject
    lateinit var dashboardViewModel: DashboardViewModel

    @Inject
    lateinit var metadataRepository: MetadataRepository

    private lateinit var mView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mView = inflater.inflate(R.layout.dashboard_fragment, container, false)
        return mView
    }

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dashboardViewModel.activeSensors.observe(this, Observer<Int> {
            mView.findViewById<TextView>(R.id.activeSensorNum).text = it?.toString() ?: "0"
        })
        dashboardViewModel.iotaReceived.observe(this, Observer<Int> {
            mView.findViewById<TextView>(R.id.iotaReceived).text = it?.toString() ?: "0"
        })
        dashboardViewModel.packetsBroadcast.observe(this, Observer<Int> {
            mView.findViewById<TextView>(R.id.dataPacketNum).text = it?.toString() ?: "0"
        })
        dashboardViewModel.uptime.observe(this, Observer<Int> {
            mView.findViewById<TextView>(R.id.uptimeNum).text = "${it?.toString() ?: "0"} minutes"
        })
        dashboardViewModel.rootAddress.observe(this, Observer<String> {
            mView.findViewById<TextView>(R.id.rootAddress).text = it
        })
        val activateText = "Start Broadcasting"
        val deActivateText = "Stop Broadcasting"
        val green = ResourcesCompat.getColor(resources, R.color.green, null)
        val red = ResourcesCompat.getColor(resources, R.color.red, null)
        val button = mView.findViewById<Button>(R.id.serviceActivationButton)
        dashboardViewModel.activeService.observe(this, Observer<Boolean> {
            when (it) {
                true -> {
                    button.setBackgroundColor(green)
                    button.text = deActivateText
                }
                else -> {
                    button.setBackgroundColor(red)
                    button.text = activateText
                }
            }

        })
        button.setOnClickListener {
            metadataRepository.ledgerServiceRunning.set(dashboardViewModel.activeService.value == null || !dashboardViewModel.activeService.value!!)
            dashboardViewModel.activeService.value = metadataRepository.ledgerServiceRunning.get()
        }
    }
}
