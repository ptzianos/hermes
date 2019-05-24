package org.hermes.utils

import androidx.lifecycle.MutableLiveData

object Integers {
    fun max(i1: Int, i2: Int, vararg ints: Int): Int {
        var max = when {
            i1 > i2 -> i1
            else -> i2
        }
        for (i: Int in ints) {
            if (i > max) {
                max = i
            }
        }
        return max
    }
}

fun Int.toMutableLiveData() = MutableLiveData<Int>().apply { value = 0 }
