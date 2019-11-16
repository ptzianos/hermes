package org.hermes.bip32

import java.lang.Exception

import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions.pow

import org.hermes.extensions.endsWithAnyOf

object BIP32 {

    class InvalidPath: Exception()

    val HARDENED_BIT = pow(2.toLong(), 31)
    val HARDENED_KEY_OFFSET = pow(2.toLong(), 31)
    val MAX_KEY_INDEX = pow(2.toLong(), 32)

    /**
     * Verifies a BIP32 compliant key path.
     */
    fun verify(path: String) {
        if (!Regex("^m(/[0-9]+['H]?)*\$").matches(path))
            throw InvalidPath()
        for (fragment in path.split("/").drop(1)) {
            val cleanFragment = when (fragment.endsWithAnyOf("'", "H")) {
                true -> fragment.dropLast(1).toLong() + HARDENED_KEY_OFFSET
                else -> fragment.toLong()
            }
            if ((cleanFragment >= MAX_KEY_INDEX) or (cleanFragment < 0))
                throw InvalidPath()
        }
    }

    /**
     * Converts a path of BIP32 path into a list of indices.
     */
    fun normalize(path: String): Iterable<Long> = path
        .split("/")
        .map { when {
            it == "m" -> 0
            it.endsWithAnyOf("'", "H") -> it.dropLast(1).toLong() + HARDENED_KEY_OFFSET
            else -> it.toLong()
        } }

    fun normalizeToStr(path: String): String = when (path) {
        "m" -> path
        else -> "m/${normalize(path).drop(1).joinToString("/")}"
    }

    fun verifyAndNormalize(path: String): Iterable<Long> {
        verify(path)
        return normalize(path)
    }

    fun pathOfChild(currentPath: String, childIndex: Long): String = "${normalizeToStr(currentPath)}/$childIndex"
}
