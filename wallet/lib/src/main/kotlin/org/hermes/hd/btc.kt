package org.hermes.hd

import java.security.MessageDigest

import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex

import org.hermes.crypto.NoEvenBit
import org.hermes.crypto.SecP256K1PrivKey
import org.hermes.utils.extendOrReduceTo
import org.hermes.utils.toByteArray

enum class BTCKey(val prefix: String) {
    PUBLIC_MAIN_NET("0488b21e"),
    PRIVATE_MAIN_NET("0488ade4"),
    PUBLIC_TEST_NET("043587cf"),
    PRIVATE_TEST_NET("04358394")
}

object BTCKeyEncoder: KeyEncoder<ExPrivKey, ExPubKey>() {
    // TODO: Find a way to remove the options map
    override fun encodePrivateKey(key: ExPrivKey, options: Map<String, Any>): String =
        Base58.toBase58String(
            Hex.decode(BTCKey.PRIVATE_MAIN_NET.prefix) +
                    key.depth.toByteArray() +
                    (key.parent?.public?.fingerprint ?: Hex.decode("00000000")) +
                    key.index.toByteArray().extendOrReduceTo(4, padStart = true) +
                    key.chainCode +
                    key.value.toByteArray().extendOrReduceTo(33, padStart = true),
            appendChecksum = true
        )

    override fun encodePublicKey(key: ExPubKey, options: Map<String, Any>): String =
        Base58.toBase58String(
            Hex.decode(BTCKey.PUBLIC_MAIN_NET.prefix) +
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


object BTCSigner: BaseECDSASigner() {

    override fun sign(key: SecP256K1PrivKey, data: ByteArray): ByteArray {
        val signature = chainSign(
            key = key,
            prefix = "Bitcoin Signed Message:\n",
            data = data,
            hashRounds = 2,
            digest = MessageDigest.getInstance("SHA-256")
        )

        if (signature.vb == null) throw NoEvenBit()
        val completeArray = ByteArray(65)
        completeArray[0] = signature.vb
        System.arraycopy(signature.rb, 0, completeArray, 1, 32)
        System.arraycopy(signature.canonicalSb, 0, completeArray, 33, 32)
        return Base64.encode(completeArray)
    }
}
