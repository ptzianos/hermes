package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import javax.inject.Inject

import org.hermes.repositories.SensorRepository


class SensorListViewModel @Inject constructor(application: Application,
                                              sensorRepository: SensorRepository
): AndroidViewModel(application) {

    val sensors = sensorRepository.sensorListData
}
