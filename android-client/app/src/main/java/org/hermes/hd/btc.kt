package org.hermes.hd

import org.bouncycastle.util.encoders.Hex

import org.hermes.utils.extend
import org.hermes.utils.toByteArray

enum class BTCNetwork(val prefix: String) {
    PUBLIC_MAIN_NET("0488b21e"),
    PRIVATE_MAIN_NET("0488ade4"),
    PUBLIC_TEST_NET("043587cf"),
    PRIVATE_TEST_NET("04358394")
}

object BTCKeyEncoder: KeyEncoder<ExPrivKey>() {
    // TODO: Find a way to remove the options map
    override fun encodePrivateKey(key: ExPrivKey, options: Map<String, Any>): String = Base58.toBase58String(
        Hex.decode(BTCNetwork.PRIVATE_MAIN_NET.prefix) +
                key.depth.toByte() +
                (key.parent?.fingerprint ?: Hex.decode("00000000")) +
                key.index.toByteArray().extend(3) +
                key.chainCode +
                key.value.toByteArray(),
            appendChecksum = true
        )
}