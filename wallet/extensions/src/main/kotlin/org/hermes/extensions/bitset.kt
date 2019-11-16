package org.hermes.extensions

import java.util.*

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

/**
 * Converts the bits to a byte array.
 *
 * If the number of bits is not enough to create a byte
 * it will be padded with zeros. Sums the bits of the bitset,
 * converts to a byte array and finally pads the array to ensure
 * correct number of expected bit.
 */
@Throws(IndexOutOfBoundsException::class)
fun BitSet.toByteArray(from: Int, to: Int): ByteArray {
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
