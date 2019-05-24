package org.hermes.utils

import androidx.lifecycle.MutableLiveData
import java.util.*

fun <E: Any> LinkedList<E>.toMutableLiveData(): MutableLiveData<List<E>> =
    MutableLiveData<List<E>>().let {
        it.value = this
        it
    }
