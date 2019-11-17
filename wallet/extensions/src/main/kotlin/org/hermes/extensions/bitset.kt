package org.hermes.extensions

import java.util.BitSet

/**
 * Copies bits from the byte representation of the integer into the bit set.
 * Java is big endian while bit sets are little endian. This means that if
 * you wish to copy some bits from the integer 11 into the bit set, then you
 * would get the following results:
 * 2 bits -> 11
 * 3 bits -> 110
 * 4 bits -> 1101
 * 5 bits -> 11010
 * The integer 11 is represented internally in Java as 00001011.
 *
 * @param _int the integer from which bits will be copied
 * @param start designates from which bit of the integer the copying will begin
 * @param bits number of bits to be copied
 * @param bitOffset from which bit of the bitset will the copying begin
 */
fun BitSet.copyFromInt(_int: Int, start: Int, bits: Int, bitOffset: Int = 0): BitSet {
    for (i in 0 until bits) {
        if (_int and (1 shl (bitOffset + i)) > 0)
            flip(start + i)
    }
    return this
}

fun BitSet.copyFromByte(byte: Byte, start: Int, bits: Int, bitOffset: Int = 0) =
    copyFromInt(byte.toInt(), start, bits, bitOffset)

fun BitSet.toString(from: Int, to: Int): String =
    (from until to)
        .map { if (this[it]) 1 else 0 }
        .joinToString("") { it.toString() }

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
