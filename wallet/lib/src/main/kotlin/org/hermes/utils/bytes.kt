package org.hermes.utils

import java.lang.IndexOutOfBoundsException
import java.math.BigInteger
import java.util.*

import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions.pow
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

fun ByteArray.extend(digits: Int, byteToAdd: Byte = 0.toByte(), padStart: Boolean = false): ByteArray = when {
    this.size < digits -> if (padStart) ByteArray(digits - this.size) { byteToAdd } + this
                          else          this + ByteArray(digits - this.size) { byteToAdd }
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

fun alignToByteSize(from: Int, to: Int): Int = (to - from) / 8 + (if ((to - from) % 8 != 0) 1 else 0)

/**
 * Converts the bits to a byte array.
 *
 * If the number of bits is not enough to create a byte
 * it will be padded with zeros. Sums the bits of the bitset,
 * converts to a byte array and finally pads the array to ensure
 * correct number of expected bit.
 */
@Throws(IndexOutOfBoundsException::class)
fun BitSet.toByteArray(from: Int, to: Int): ByteArray
//        =
//    sliceAndAlign(from, to)
//        .zip((alignToByteSize(from, to) * 8) - 1 downTo 0)
//        .map { p: Pair<Int, Int> -> p.first * pow(2, p.second) }
//        .reduce { acc: Int, e: Int -> acc + e }
//        .toByteArray()
//        .extend(alignToByteSize(from, to), padStart = true)
{
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

/**
 * Takes a slice of bits.
 *
 * If out of bounds it will add 0's. Treats bitset as big endian.
 */
fun BitSet.slice(from: Int, to: Int): List<Int> =
    (from until to)
        .map { if (get(it)) 1 else 0 }
        .toList()

/**
 * Takes a slice of bits and then adds 0's to align it as bytes.
 */
fun BitSet.sliceAndAlign(from: Int, to: Int): List<Int> {
    val expectedSize = alignToByteSize(0, to - from)
    return slice(from, to) + LinkedList<Int>().apply { for (i in 0 until expectedSize - (to - from)) add(0) }
}

fun <R> BitSet.mapIndexed(from: Int, to: Int, transform: (index: Int, bit: Int) -> R): List<R> =
    LinkedList<Int>().apply {
        (to - 1 downTo from).forEach { add(if (this@mapIndexed.get(it)) 1 else 0) }
    }.mapIndexed(transform)

fun <R> BitSet.mapIndexed(transform: (index: Int, bit: Int) -> R): List<R> {
    val ll = LinkedList<R>()
    for (i in 0 until size())
        ll.add(transform(i, if (get(i)) 1 else 0))
    return ll
}

fun ByteArray.ensureMinSize(minSize: Int, byteToFill: Byte): ByteArray = when {
    size < minSize -> plus(ByteArray(minSize - size) { byteToFill })
    else -> this
}

fun BitSet.copyFromInt(int: Int, start: Int, bits: Int, bitOffset: Int = 0) {
    for (i in 0 until bits) {
        if (int and (1 shl (bits + bitOffset - 1 - i)) > 0)
            flip(start + i)
    }
}

fun BitSet.copyFromByte(byte: Byte, start: Int, bits: Int, bitOffset: Int = 0) =
    copyFromInt(byte.toInt(), start, bits, bitOffset)

fun BitSet.toString(from: Int, to: Int): String =
    (from until to)
        .map { if (this[it]) 1 else 0 }
        .joinToString { it.toString() }

fun ByteArray.toTryteArray(): TryteArray = TryteArray(this)
