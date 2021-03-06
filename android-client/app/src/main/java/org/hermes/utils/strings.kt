package org.hermes.utils

import android.util.Log
import java.lang.StringBuilder
import org.hermes.crypto.SecP256K1PrivKey
import org.iota.jota.utils.TrytesConverter

/**
 * Split a string into strings of maximum size equal to chunkSize.
 */
fun String.splitInChunks(chunkSize: Int): Array<String> {
    if (this.isEmpty())
        return arrayOf("")

    val head = fun(str: CharSequence) = when {
            str.isEmpty() -> ""
            str.length > chunkSize -> str.subSequence(0, chunkSize)
            else -> str
        }

    val tail = fun(str: CharSequence) = when {
        str.isEmpty() -> ""
        str.length > chunkSize -> str.subSequence(chunkSize, str.length)
        else -> ""
    }

    var arr = Array(0) { "" }
    var chunk = head(this)
    var rest = tail(this)

    do {
        arr = arr.plus(chunk.toString())
        chunk = head(rest)
        rest = tail(rest)
    } while (chunk.isNotEmpty())

    return arr
}

/**
 * Convert ASCII to trytes.
 */
fun String.toTrytes(): String = TrytesConverter.asciiToTrytes(this)

fun String.prepend(header: String): String = header + this

fun String.sign(header: String = "", privateKey: SecP256K1PrivKey,
                separator: String = ""): String {
    return try {
        StringBuilder()
            .append(header)
            .append(privateKey.sign(this@sign.toByteArray()).toHexedBinary())
            .append(separator)
            .append(this)
            .toString()
    } catch (e: Throwable) {
        Log.e("String","Could not find algorithm to sign the message $e")
        this
    }
}
