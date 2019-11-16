package org.hermes.bip32

import java.lang.Exception
import java.security.PublicKey

import org.hermes.crypto.RIPEMD
import org.hermes.crypto.SHA256

interface BIP32PubKey: PublicKey {

    class NoPubKey: Exception()

    val path: String

    val index: Long
        get() = BIP32Key.currentIndex(path)

    val parent: BIP32PubKey?

    val chainCode: ByteArray

    val depth: Int
        get() = path.split("/").size - 1

    val identifier: ByteArray
        get() = RIPEMD.hash(SHA256.hash(encoded))

    val fingerprint: ByteArray
        get() = identifier.sliceArray(0 until 4)

    fun child(index: Long): BIP32PubKey

    val address: String
}
