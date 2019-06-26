package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

import org.hermes.MetadataRepository


class DashboardViewModel @Inject constructor(application: Application,
                                             metadataRepository: MetadataRepository
): AndroidViewModel(application) {
    // TODO: Move this to the MetadataRepository when the wallet functionality has been finished
    val iotaReceived = MutableLiveData<Int>().apply {
        value = 0
    }

    val uptime = metadataRepository.getLedgerServiceUptime()

    val activeSensors = metadataRepository.getActiveSensorNumLiveData()

    val packetsBroadcast = metadataRepository.getPacketsBroadcast()

    val activeService = metadataRepository.ledgerServiceRunningLiveData

    val rootAddress = metadataRepository.rootIOTAAddress
}
