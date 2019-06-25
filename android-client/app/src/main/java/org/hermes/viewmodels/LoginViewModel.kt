package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject

import org.hermes.CryptoRepository


class LoginViewModel @Inject constructor(application: Application,
                                         private val cryptoRepository: CryptoRepository)
    : AndroidViewModel(application) {

    private val _isFormValid = MutableLiveData<Boolean>()

    val isFormValid: LiveData<Boolean>
        get() = _isFormValid

    var pin: String? = null
        set(value) {
            field = value
            _isFormValid.postValue(value != null && !value.isBlank() && cryptoRepository.unseal(value))
        }
}