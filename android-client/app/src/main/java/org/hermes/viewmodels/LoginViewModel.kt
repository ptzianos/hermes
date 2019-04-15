package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

import org.hermes.HermesRepository


class LoginViewModel(application: Application): AndroidViewModel(application) {

    @Inject
    lateinit var hermesRepository: HermesRepository

    private val _isFormValid = MutableLiveData<Boolean>()

    val isFormValid: LiveData<Boolean>
        get() = _isFormValid

    var pin: String? = null
        set(value) {
            field = value
            _isFormValid.postValue(value != null && !value.isBlank() && hermesRepository.checkPIN(value))
        }
}