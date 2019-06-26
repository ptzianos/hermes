package org.hermes.crypto

import java.math.BigInteger
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.bouncycastle.util.encoders.Hex


class SecP256K1PubKey(val x: BigInteger, val y: BigInteger) {

    companion object {
        fun fromBitcoinPubKey(pubKey: String): SecP256K1PubKey {
            if (!pubKey.startsWith("04")) throw Exception("Not a Bitcoin pub key hex")
            return SecP256K1PubKey(
                x = BigInteger(Hex.decode(pubKey.drop(2).dropLast(64))),
                y = BigInteger(Hex.decode(pubKey.drop(2 + 64))))
        }

        fun fromPrivateKey(privateKey: SecP256K1PrivKey): SecP256K1PubKey {
            val publicKeyBCECPoint: ECPoint = FixedPointCombMultiplier()
                .multiply(
                    Secp256K1Curve.X9ECParameters.g,
                    privateKey.value)
                .normalize()
            publicKeyBCECPoint.getEncoded(false)
            return SecP256K1PubKey(publicKeyBCECPoint)
        }
    }

    // Store public key in a form that is compatible with the standard Java libraries
    val publicKeyECPoint: java.security.spec.ECPoint = java.security.spec.ECPoint(x, y)

    // This field stores the public key in a standard encrypted form:
    // one byte at the beginning (0x04), the affine x coordinate as a big endian integer byte array
    // and the affine y coordinate as a big endian integer byte array.
    val encoded: ByteArray

    init {
        val xB = x.toByteArray()
        val yB = y.toByteArray()
        val xOffset = if (xB[0] == 0.toByte()) 1 else 0
        val yOffset = if (yB[0] == 0.toByte()) 1 else 0
        encoded = ByteArray(x.toByteArray().size + y.toByteArray().size + 1 - xOffset - yOffset)
        encoded[0] = 4.toByte()
        System.arraycopy(xB, 0 + xOffset, encoded, 1, xB.size - xOffset)
        System.arraycopy(yB, 0 + yOffset, encoded, 1 + xB.size - xOffset, yB.size - yOffset)
    }

    // The encoded field as a Hex string
    val encodedHex = Hex.toHexString(encoded)

    constructor(publicKeyECPoint: ECPoint): this(
        publicKeyECPoint.affineXCoord.toBigInteger(),
        publicKeyECPoint.affineYCoord.toBigInteger())
}
