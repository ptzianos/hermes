package org.hermes.extensions

import java.math.BigInteger

import org.hermes.ternary.TryteArray

fun ByteArray.extendOrReduceTo(digits: Int, byteToAdd: Byte = 0.toByte(),
                               chipFromEnd: Boolean = false,
                               padStart: Boolean = false): ByteArray = when {
    this.size < digits -> if (padStart)    ByteArray(digits - this.size) { byteToAdd } + this
    else             this + ByteArray(digits - this.size) { byteToAdd }
    this.size > digits -> if (chipFromEnd) this.sliceArray(0 until digits)
    else             this.sliceArray(this.size - digits until this.size)
    else -> this
}

fun ByteArray.toBigInt(positive: Boolean = false): BigInteger = when {
    positive -> BigInteger(1, this)
    else ->     BigInteger(this)
}

fun ByteArray.toTryteArray(): TryteArray = TryteArray(this)
