package org.hermes.utils

import jota.utils.TrytesConverter

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
fun String.toTrytes(): String {
    return TrytesConverter.asciiToTrytes(this)
}
