package org.hermes.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


class SetupViewModel(application: Application): AndroidViewModel(application) {

    private val _isFormValid = MutableLiveData<Boolean>()

    val isFormValid: LiveData<Boolean>
        get() = _isFormValid

    var pin: String? = null
        set(value) {
            field = value
            validateForm()
        }

    var pinRepeat: String? = null
        set(value) {
            field = value
            validateForm()
        }

    private fun validateForm() {
        if (pin != null && pin == pinRepeat) {
            // Add a check to ensure the length of the pin is not too small
            _isFormValid.postValue(true)
        } else {
            _isFormValid.postValue(false)
        }
    }
}