package org.hermes.crypto

import java.math.BigInteger
import java.security.PublicKey

import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.bouncycastle.util.encoders.Hex


open class SecP256K1PubKey(val x: BigInteger, val y: BigInteger): PublicKey {

    companion object {
        fun fromBitcoinPubKey(pubKey: String): SecP256K1PubKey {
            if (!pubKey.startsWith("04")) throw Exception("Not a Bitcoin pub key hex")
            return SecP256K1PubKey(
                x = BigInteger(Hex.decode(pubKey.drop(2).dropLast(64))),
                y = BigInteger(Hex.decode(pubKey.drop(2 + 64))))
        }
    }

    // Store public key in a form that is compatible with the standard Java libraries
    val point: java.security.spec.ECPoint = java.security.spec.ECPoint(x, y)

    // Store public key in a form that is compatible with the Bouncy Castle internal data structures
    val bcPoint: ECPoint = Secp256K1Curve.ecDomainParameters.curve.createPoint(x, y)

    // The encoded field as a Hex string
    val encodedHex: String by lazy { Hex.toHexString(encoded) }

    constructor(publicKeyECPoint: ECPoint): this(
        publicKeyECPoint.affineXCoord.toBigInteger(),
        publicKeyECPoint.affineYCoord.toBigInteger())

    constructor(privateKey: SecP256K1PrivKey):
        this(
            FixedPointCombMultiplier()
            .multiply(
                Secp256K1Curve.X9ECParameters.g,
                privateKey.value)
            .normalize()
        )

    override fun getAlgorithm(): String = "SECP256K1"

    /**
     * Encodes the public key in a standard encoded form:
     * one byte at the beginning (0x04), the affine x coordinate as
     * a big endian integer byte array and the affine y coordinate
     * as a big endian integer byte array.
     */
    override fun getEncoded(): ByteArray {
        val xB = x.toByteArray()
        val yB = y.toByteArray()
        val xOffset = if (xB[0] == 0.toByte()) 1 else 0
        val yOffset = if (yB[0] == 0.toByte()) 1 else 0
        val encodedByteArray = ByteArray(x.toByteArray().size + y.toByteArray().size + 1 - xOffset - yOffset)
        encodedByteArray[0] = 4.toByte()
        System.arraycopy(xB, 0 + xOffset, encodedByteArray, 1, xB.size - xOffset)
        System.arraycopy(yB, 0 + yOffset, encodedByteArray, 1 + xB.size - xOffset, yB.size - yOffset)
        return encodedByteArray
    }

    override fun getFormat(): String = "ASN.1"
}
