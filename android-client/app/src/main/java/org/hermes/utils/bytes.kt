package org.hermes.utils

import java.lang.IndexOutOfBoundsException
import java.util.*
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex

fun ByteArray.toHexStr(): String = Hex.toHexString(this)

fun ByteArray.toBase64Str(): String = Base64.toBase64String(this)

fun ByteArray.countLeft(predicate: (Byte) -> Boolean): Int {
    var count = 0
    var i = 0
    while (i < this.size && predicate(this[i])) {
        count++
        i++
    }
    return count
}

fun BitSet.asByteArray(from: Int, to: Int): ByteArray {
    val extractByte = fun(start: Int): Byte {
        var byte = 0
        for (i in 0 until 8) {
            if (this[start + i]) {
                byte = byte xor (1 shl i)
            }
        }
        return byte.toByte()
    }
    if (from > to || from < 0) throw IndexOutOfBoundsException()
    val byteArraySize = (to - from) / 8 + (if ((to - from) % 8 != 0) 1 else 0)
    return ByteArray(byteArraySize) { extractByte(it * 8) }
}