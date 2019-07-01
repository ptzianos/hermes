package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

import org.hermes.MetadataRepository


class DashboardViewModel @Inject constructor(application: Application,
                                             metadataRepository: MetadataRepository
): AndroidViewModel(application) {
    val iotaReceived = MutableLiveData<Int>().apply {
        value = 0
    }

    val uptime = metadataRepository.ledgerServiceUptime

    val activeSensors = metadataRepository.activeSensorNum

    val packetsBroadcast = metadataRepository.packetsBroadcastNum

    val packetsConfirmed = metadataRepository.packetsConfirmedNum

    val activeService = metadataRepository.ledgerServiceBroadcasting.getLiveData()

    val rootAddress = metadataRepository.rootIOTAAddress

    val failedBroadcastNum = metadataRepository.failedBroadcastNum
}
