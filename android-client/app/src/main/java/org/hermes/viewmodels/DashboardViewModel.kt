package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

import org.hermes.HermesRepository


class DashboardViewModel @Inject constructor(application: Application,
                                             hermesRepository: HermesRepository
): AndroidViewModel(application) {
    // TODO: Move this to the HermesRepository when the wallet functionality has been finished
    val iotaReceived = MutableLiveData<Int>().apply {
        value = 0
    }

    val uptime = hermesRepository.getLedgerServiceUptime()

    val activeSensors = hermesRepository.getActiveSensorNumLiveData()

    val packetsBroadcast = hermesRepository.getPacketsBroadcast()

    val activeService = hermesRepository.ledgerServiceRunningLiveData
}
