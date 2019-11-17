package org.hermes.bip32

import org.bouncycastle.util.encoders.Hex
import org.hermes.encoders.Base58
import org.hermes.extensions.extendOrReduceTo
import org.hermes.extensions.toByteArray

object BTCKeyEncoder : KeyEncoder<ExPrivKey, ExPubKey>() {
    // TODO: Find a way to remove the options map
    override fun encodePrivateKey(key: ExPrivKey, options: Map<String, Any>): String =
        Base58.toBase58String(
            Hex.decode(BTCNet.PRIVATE_MAIN_NET.prefix) +
                    key.depth.toByteArray() +
                    (key.parent?.public?.fingerprint ?: Hex.decode("00000000")) +
                    key.index.toByteArray().extendOrReduceTo(4, padStart = true) +
                    key.chainCode +
                    key.value.toByteArray().extendOrReduceTo(33, padStart = true),
            appendChecksum = true
        )

    override fun encodePublicKey(key: ExPubKey, options: Map<String, Any>): String =
        Base58.toBase58String(
            Hex.decode(BTCNet.PUBLIC_MAIN_NET.prefix) +
                    key.depth.toByteArray() +
                    (key.parent?.fingerprint ?: Hex.decode("00000000")) +
                    key.index.toByteArray().extendOrReduceTo(4, padStart = true) +
                    key.chainCode +
                    key.encoded,
            appendChecksum = true
        )

    override fun address(key: ExPubKey, options: Map<String, Any>): String =
        Base58.toBase58String(ByteArray(1) + key.identifier, appendChecksum = true)
}
