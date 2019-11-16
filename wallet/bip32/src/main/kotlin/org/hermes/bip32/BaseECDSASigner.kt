package org.hermes.bip32

import java.security.MessageDigest

import org.hermes.crypto.ECDSASignature
import org.hermes.crypto.SecP256K1PrivKey
import org.hermes.extensions.toByteArray

abstract class BaseECDSASigner: Signer<SecP256K1PrivKey> {

    tailrec fun recDigest(msg: ByteArray, round: Int, digest: MessageDigest = MessageDigest.getInstance("SHA-256")): ByteArray {
        val d = digest.digest(msg)
        return when(round) {
            1 -> d
            else -> recDigest(d, round - 1, digest)
        }
    }

    /**
     * Base method for signing messages for various different blockchains,
     * that loosely follow the Electrum style signatures.
     */
    fun chainSign(
        key: SecP256K1PrivKey, prefix: String, data: ByteArray, hashRounds: Int = 1, digest: MessageDigest
    ): ECDSASignature {
        val prefixLength = prefix.length
        val messageLength = data.size
        val messageLengthExtraByte = when {
            messageLength < 253 -> ByteArray(0)
            messageLength < 65536 -> 253.toByteArray()
            messageLength < 4294967296 -> 254.toByteArray()
            else -> 255.toByteArray()
        }

        val msgBytes = prefixLength.toByteArray() +
                prefix.toByteArray() +
                messageLengthExtraByte +
                messageLength.toByteArray().reversedArray() +
                data

        val hashed = recDigest(msgBytes, hashRounds, digest)
        val (r, s, rY) = key.rawSign(hashed)
        return ECDSASignature(r, s, rY)
    }
}