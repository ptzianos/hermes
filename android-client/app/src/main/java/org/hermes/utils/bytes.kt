package org.hermes.utils

import java.lang.IndexOutOfBoundsException
import java.math.BigInteger
import java.util.*

import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex

import org.hermes.hd.Base58
import org.hermes.iota.TryteArray

fun ByteArray.toHexStr(): String = Hex.toHexString(this)

fun ByteArray.toBase64Str(): String = Base64.toBase64String(this)

fun ByteArray.toBase58Str(appendChecksum: Boolean = false): String = Base58.toBase58String(this, appendChecksum)

fun ByteArray.toBigInt(positive: Boolean = false): BigInteger = when {
    positive -> BigInteger(1, this)
    else ->     BigInteger(this)
}

fun ByteArray.extend(digits: Int, byteToAdd: Byte = 0.toByte()): ByteArray = when {
    this.size < digits -> this + ByteArray(digits - this.size) { byteToAdd }
    else -> this
}

fun ByteArray.extendOrReduceTo(digits: Int, byteToAdd: Byte = 0.toByte(),
                               chipFromEnd: Boolean = false,
                               padStart: Boolean = false): ByteArray = when {
    this.size < digits -> if (padStart)    ByteArray(digits - this.size) { byteToAdd } + this
                          else             this + ByteArray(digits - this.size) { byteToAdd }
    this.size > digits -> if (chipFromEnd) this.sliceArray(0 until digits)
                          else             this.sliceArray(this.size - digits until this.size)
    else -> this
}

fun ByteArray.countLeft(predicate: (Byte) -> Boolean): Int {
    var count = 0
    var i = 0
    while (i < this.size && predicate(this[i])) {
        count++
        i++
    }
    return count
}

/**
 * Converts the bits to a byte array.
 *
 * If the number of bits is not enough to create a byte
 * it will be padded with zeros.
 */
@Throws(IndexOutOfBoundsException::class)
fun BitSet.asByteArray(from: Int, to: Int): ByteArray {
    val byteArraySize = (to - from) / 8 + (if ((to - from) % 8 != 0) 1 else 0)
    return ByteArray(byteArraySize) {
        var res = 0
        for (j in 0 until 8) {
            if ((it * 8) + j + from > to) break
            if (this[(it * 8) + j + from]) res = res or (1 shl (7 - j))
        }
        res.toByte()
    }
}

fun ByteArray.ensureMinSize(minSize: Int, byteToFill: Byte): ByteArray {
    return if (size < minSize) plus(ByteArray(minSize - size) { byteToFill }) else this
}

fun BitSet.copyFromInt(int: Int, start: Int, bits: Int, bitOffset: Int = 0) {
    for (i in 0 until bits) {
        val test = 1 shl (bits + bitOffset - 1 - i)
        if (int and (1 shl (bits + bitOffset - 1 - i)) > 0)
            flip(start + i)
    }
}

fun BitSet.copyFromByte(byte: Byte, start: Int, bits: Int, bitOffset: Int = 0) =
    copyFromInt(byte.toInt(), start, bits, bitOffset)

fun BitSet.asString(from: Int, to: Int): String {
    return (from until to)
        .map { if (this[it]) 1 else 0 }
        .joinToString { it.toString() }
}

fun ByteArray.toTryteArray(): TryteArray = TryteArray(this)
