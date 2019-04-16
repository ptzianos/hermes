package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

import org.hermes.HermesRepository


class LoginViewModel @Inject constructor(application: Application,
                                         private val hermesRepository: HermesRepository)
    : AndroidViewModel(application) {

    private val _isFormValid = MutableLiveData<Boolean>()

    val isFormValid: LiveData<Boolean>
        get() = _isFormValid

    var pin: String? = null
        set(value) {
            field = value
            _isFormValid.postValue(value != null && !value.isBlank() && hermesRepository.checkPIN(value))
        }
}