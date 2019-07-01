package org.hermes.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.util.concurrent.atomic.AtomicBoolean


class AtomicLiveBoolean(b: Boolean): AtomicBoolean(b) {
    private var observer: MutableLiveData<Boolean>? = null

    fun setAndNotify(newValue: Boolean) {
        super.set(newValue)
        observer?.setValue(get())
    }

    fun getLiveData(): LiveData<Boolean> {
        if (observer == null)
            observer = MutableLiveData<Boolean>().apply { value = get() }
        return observer!!
    }
}