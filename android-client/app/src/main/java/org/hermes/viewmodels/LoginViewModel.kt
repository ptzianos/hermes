package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

import org.hermes.HermesRepository


class LoginViewModel(application: Application): AndroidViewModel(application) {

    private val repository = lazy { HermesRepository.getInstance(application) }

    private val _isFormValid = MutableLiveData<Boolean>()

    val isFormValid: LiveData<Boolean>
        get() = _isFormValid

    var pin: String? = null
        set(value) {
            field = value
            _isFormValid.postValue(value != null && !value.isBlank() && repository.value.checkPIN(value))
        }
}