package org.hermes.utils

import java.math.BigInteger

import org.hermes.iota.Trit
import org.hermes.iota.TritArray
import org.hermes.iota.TryteArray

fun Int.toByteArray(): ByteArray {
    return when {
        this < 256 -> byteArrayOf(this.toByte())
        else -> (this / 256).toByteArray() + (this % 256).toByteArray()
    }
}

fun IntArray.toTritArray(): TritArray = TritArray(this.size) { Trit(this[it]) }

fun Int.toTryteArray(): TryteArray = TryteArray(this.toByteArray())

fun BigInteger.toTryteArray(): TryteArray = TryteArray(this.toByteArray())
