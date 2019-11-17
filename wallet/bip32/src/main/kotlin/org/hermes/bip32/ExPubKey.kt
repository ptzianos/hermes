package org.hermes.bip32

import java.math.BigInteger
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.hermes.crypto.CryptoAlgorithms
import org.hermes.crypto.SecP256K1PrivKey
import org.hermes.crypto.SecP256K1PubKey
import org.hermes.crypto.Secp256K1Curve
import org.hermes.extensions.toBigInt
import org.hermes.extensions.toByteArray

class ExPubKey(

    override val parent: BIP32PubKey?,
    override val chainCode: ByteArray,
    override val path: String,
    x: BigInteger,
    y: BigInteger,
    val encoder: KeyEncoder<ExPrivKey, ExPubKey> = BTCKeyEncoder
) : SecP256K1PubKey(x, y), BIP32PubKey {

    override fun getEncoded(): ByteArray = bcPoint.getEncoded(true)

    constructor(
        parent: BIP32PubKey?,
        chainCode: ByteArray,
        path: String,
        publicKeyECPoint: ECPoint,
        encoder: KeyEncoder<ExPrivKey, ExPubKey> = BTCKeyEncoder
    ) :
            this(
                parent, chainCode, path,
                publicKeyECPoint.affineXCoord.toBigInteger(),
                publicKeyECPoint.affineYCoord.toBigInteger(),
                encoder
            )

    constructor(parent: BIP32PubKey?, chainCode: ByteArray, path: String, rawKey: BigInteger) :
            this(
                parent, chainCode, path,
                FixedPointCombMultiplier()
                    .multiply(Secp256K1Curve.X9ECParameters.g, rawKey)
                    .normalize()
            )

    constructor(
        parent: BIP32PubKey?,
        chainCode: ByteArray,
        path: String,
        privateKey: SecP256K1PrivKey,
        encoder: KeyEncoder<ExPrivKey, ExPubKey> = BTCKeyEncoder
    ) :
            this(parent, chainCode, path,
                FixedPointCombMultiplier()
                    .multiply(Secp256K1Curve.X9ECParameters.g, privateKey.value)
                    .normalize(),
                encoder
            )

    override fun toString(): String = encoder.encodePublicKey(this, hashMapOf())

    override fun child(index: Long): BIP32PubKey {
        if ((index < 0) or (index >= BIP32.MAX_KEY_INDEX))
            throw BIP32Key.InvalidIndex()

        if (index >= BIP32.HARDENED_KEY_OFFSET)
            throw BIP32PubKey.NoPubKey()

        val I = Mac.getInstance(CryptoAlgorithms.HMAC_SHA512)
            .apply { init(SecretKeySpec(chainCode, CryptoAlgorithms.HMAC_SHA512)) }
            .doFinal(ByteArray(1) + encoded + index.toByteArray())

        return ExPubKey(
            this,
            I.sliceArray(32 until 64),
            BIP32.pathOfChild(path, index),
            I.sliceArray(0 until 32).toBigInt()
        )
    }

    override val address: String = encoder.address(this, hashMapOf())
}
