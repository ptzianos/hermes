package org.hermes.bip32

import java.math.BigInteger

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.hermes.crypto.CryptoAlgorithms
import org.hermes.crypto.SecP256K1PrivKey
import org.hermes.crypto.Secp256K1Curve
import org.hermes.extensions.extendOrReduceTo
import org.hermes.extensions.toBigInt
import org.hermes.extensions.toByteArray

class ExPrivKey(
    override val path: String,
    override val parent: BIP32Key?,
    override val chainCode: ByteArray,
    key: BigInteger,
    val encoder: KeyEncoder<ExPrivKey, ExPubKey> = BTCKeyEncoder
) : BIP32Key, SecP256K1PrivKey(key) {

    init {
        BIP32.verify(path)
    }

    companion object {
        fun CKDPriv(
            index: Long,
            key: ByteArray,
            public: ByteArray,
            chainCode: ByteArray
        ): Pair<ByteArray, BigInteger> {
            if ((index < 0) or (index >= BIP32.MAX_KEY_INDEX))
                throw BIP32Key.InvalidIndex()

            val n = Secp256K1Curve.X9ECParameters.n
            val digest = Mac.getInstance(CryptoAlgorithms.HMAC_SHA512).apply { init(SecretKeySpec(chainCode, CryptoAlgorithms.HMAC_SHA512)) }
            val I = when (index >= BIP32.HARDENED_KEY_OFFSET) {
                true -> digest.doFinal(
                    key.extendOrReduceTo(33, padStart = true) +
                            index.toByteArray().extendOrReduceTo(4, padStart = true)
                )
                else -> digest.doFinal(
                    public + index.toByteArray().extendOrReduceTo(4, padStart = true)
                )
            }
            return Pair(
                I.sliceArray(32 until 64),
                (I.sliceArray(0 until 32).toBigInt(positive = true) + BigInteger(key)).mod(n)
            )
        }
    }

    override fun child(index: Long): BIP32Key {
        val (chainCode, key) = CKDPriv(
            index,
            value.toByteArray(),
            public.encoded,
            chainCode
        )
        return ExPrivKey(BIP32.pathOfChild(path, index), this, chainCode, key)
    }

    override val public: ExPubKey by lazy {
        ExPubKey(
            parent?.public,
            chainCode,
            path,
            this,
            encoder
        )
    }

    override fun toString(): String = encoder.encodePrivateKey(this, hashMapOf())
}
