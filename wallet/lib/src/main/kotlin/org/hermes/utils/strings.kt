package org.hermes.utils

import android.util.Log

import java.lang.StringBuilder

import org.hermes.crypto.SecP256K1PrivKey

import org.iota.jota.utils.TrytesConverter

fun String.toTrytes(): String = TrytesConverter.asciiToTrytes(this)

fun String.stripPaddingOfTrytes(): String = this.trimEnd('9')

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

fun String.endsWithAnyOf(vararg s: String): Boolean {
    for (suffix in s)
        if (this.endsWith(suffix))
            return true
    return false
}

fun String.startsWithAnyOf(collection: Collection<String>): Boolean {
    for (prefix in collection)
        if (this.startsWith(prefix))
            return true
    return false
}
