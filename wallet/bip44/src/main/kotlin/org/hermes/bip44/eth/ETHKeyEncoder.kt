package org.hermes.bip44.eth

import org.bouncycastle.util.encoders.Hex
import org.hermes.bip32.ExPrivKey
import org.hermes.bip32.ExPubKey
import org.hermes.bip32.KeyEncoder
import org.hermes.crypto.Keccak

object ETHKeyEncoder : KeyEncoder<ExPrivKey, ExPubKey>() {
    override fun encodePrivateKey(key: ExPrivKey, options: Map<String, Any>): String =
        // Drop the 0x00 byte at the beginning
        Hex.toHexString(key.value.toByteArray().drop(1).toByteArray())

    override fun encodePublicKey(key: ExPubKey, options: Map<String, Any>): String =
        // Drop the 0x04 byte at the beginning
        Hex.toHexString(key.bcPoint.getEncoded(false).drop(1).toByteArray())

    override fun address(key: ExPubKey, options: Map<String, Any>): String =
        Hex.toHexString(Keccak.hash(key.bcPoint.getEncoded(false).drop(1).toByteArray()))
            .drop(24)
}
