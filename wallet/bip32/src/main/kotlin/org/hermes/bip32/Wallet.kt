package org.hermes.bip32

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.hermes.crypto.CryptoAlgorithms
import org.hermes.extensions.toBigInt

open class Wallet(seed: ByteArray, private val keyRegistry: KeyRegistry = InMemoryKeyRegistry()) {

    open operator fun get(path: String): BIP32Key {
        BIP32.verify(path)
        val normalizedPath = BIP32.normalizeToStr(path)
        val cachedKey = keyRegistry.get(normalizedPath)
        if (cachedKey != null) return cachedKey
        var key: BIP32Key = master
        var partialPath = master.path
        for (index in BIP32.normalize(path).drop(1)) {
            partialPath = BIP32.normalizeToStr(BIP32.pathOfChild(partialPath, index))
            val intermediateKey = keyRegistry.get(partialPath)
            if (intermediateKey == null) {
                key = key.child(index)
                keyRegistry.put(key.path, key)
            } else {
                key = intermediateKey
            }
        }
        return key
    }

    /**
     * Master node will always be a standard Extended Private Key. The other nodes might be of
     * different kinds depending on the network they will be used for.
     */
    val master: ExPrivKey

    init {
        val I = Mac.getInstance(CryptoAlgorithms.HMAC_SHA512)
            .apply { init(SecretKeySpec("Bitcoin seed".toByteArray(), CryptoAlgorithms.HMAC_SHA512)) }
            .doFinal(seed)

        master = ExPrivKey(
            path = "m",
            parent = null,
            chainCode = I.sliceArray(32 until 64),
            key = I.sliceArray(0 until 32).toBigInt(positive = true)
        )

        keyRegistry.put("m", master)
    }
}
