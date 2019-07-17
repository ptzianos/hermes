package org.hermes.utils

import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex

fun ByteArray.toHexStr(): String = Hex.toHexString(this)

fun ByteArray.toBase64Str(): String = Base64.toBase64String(this)

//fun ByteArray.toBase58Str(checksum: Boolean = false): String {
//
//}

fun ByteArray.countLeft(predicate: (Byte) -> Boolean): Int {
    var count = 0
    var i = 0
    while (i < this.size && predicate(this[i])) {
        count++
        i++
    }
    return count
}