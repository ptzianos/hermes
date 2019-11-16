package org.hermes.extensions

fun Int.toByteArray(): ByteArray {
    return when {
        this < 256 -> byteArrayOf(this.toByte())
        else -> (this / 256).toByteArray() + (this % 256).toByteArray()
    }
}
