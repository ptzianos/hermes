package org.hermes.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicBoolean


class AtomicLiveBoolean(b: Boolean): AtomicBoolean(b) {
    private val observer: MutableLiveData<Boolean> = MutableLiveData<Boolean>().apply { value = b }

    fun setLive(newValue: Boolean) {
        super.set(newValue)
        observer.setValue(get())
    }

    fun getLiveData(): LiveData<Boolean> = observer
}