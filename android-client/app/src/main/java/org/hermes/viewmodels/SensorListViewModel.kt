package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import javax.inject.Inject

import org.hermes.HermesRepository


class SensorListViewModel @Inject constructor(application: Application,
                                              hermesRepository: HermesRepository): AndroidViewModel(application) {

    val sensors = hermesRepository.getSensorLiveData()
}
