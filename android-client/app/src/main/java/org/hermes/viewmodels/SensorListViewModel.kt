package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import javax.inject.Inject

import org.hermes.MetadataRepository


class SensorListViewModel @Inject constructor(application: Application,
                                              metadataRepository: MetadataRepository): AndroidViewModel(application) {

    val sensors = metadataRepository.getSensorLiveData()
}
