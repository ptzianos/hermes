package org.hermes.crypto

import java.security.MessageDigest

open class SHAHash(val impl: MessageDigest) {

    fun hash(b: ByteArray): ByteArray {
        return impl.digest(b)
    }

    fun hashTwice(b: ByteArray): ByteArray {
        return hash(hash(b))
    }
}