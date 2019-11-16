package org.hermes.bip32

import java.security.PrivateKey

interface Signer<in K : PrivateKey> {
    fun sign(key: K, data: ByteArray): ByteArray
}
