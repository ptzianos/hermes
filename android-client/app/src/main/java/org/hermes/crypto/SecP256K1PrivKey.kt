package org.hermes.crypto

import java.math.BigInteger
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.SecureRandom
import java.util.*
import kotlinx.io.IOException
import kotlinx.io.StringWriter
import org.bouncycastle.asn1.ASN1Object
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.asn1.x509.AlgorithmIdentifier
import org.bouncycastle.asn1.x9.X9ObjectIdentifiers
import org.bouncycastle.crypto.Digest
import org.bouncycastle.crypto.digests.SHA256Digest
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.signers.DSAKCalculator
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.jcajce.provider.digest.Keccak
import org.bouncycastle.math.ec.ECMultiplier
import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.bouncycastle.util.encoders.Hex
import org.bouncycastle.util.io.pem.PemObject
import org.bouncycastle.util.io.pem.PemWriter

import org.hermes.hd.Base58
import org.hermes.utils.toByteArray


open class SecP256K1PrivKey : PrivateKey {

    companion object {
        fun random(): SecP256K1PrivKey {
            return SecP256K1PrivKey(key = BigInteger(256, SecureRandom.getInstance("SHA1PRNG")))
        }

        fun validateWIFCheckSum(wifStr: String): Boolean {
            val extendedCheckSummedBytes = Base58.decode(wifStr)
            val extendedChecksum = extendedCheckSummedBytes.dropLast(4).toByteArray()
            val digest = MessageDigest.getInstance("SHA-256")
            val twiceHashed = digest.digest(digest.digest(extendedChecksum))
            return Arrays.equals(
                twiceHashed.take(4).toByteArray(),
                extendedCheckSummedBytes.takeLast(4).toByteArray()
            )
        }
    }

    constructor(key: BigInteger) {
        val N = Secp256K1Curve.X9ECParameters.n.bitLength().toBigInteger()
        value = if (key.bitLength().toBigInteger() > N) key.mod(N) else key
    }

    constructor(hexStr: String) {
        value = BigInteger(Hex.decode(if (hexStr.startsWith("0x")) hexStr.drop(2) else hexStr))
    }

    // Make sure that the value of the key is not larger than the order of the group
    val value: BigInteger

    val point: ECPoint
        get() = FixedPointCombMultiplier().multiply(Secp256K1Curve.ecDomainParameters.g, value).normalize()

    /**
     * Signs a message according to RFC 6979 with a deterministic K.
     * The message is not check-summed, whatever bytes are fed to the function will be the ones
     * that will be signed.
     * Modified version of: @see <a href="https://github.com/bcgit/bc-java/blob/master/core/src/main/java/org/bouncycastle/crypto/signers/ECDSASigner.java">ECDSASigner.java</a>
     */
    fun rawSign(message: ByteArray): Triple<BigInteger, BigInteger, BigInteger> {
        val ec: ECDomainParameters = Secp256K1Curve.ecDomainParameters
        val n: BigInteger = ec.n
        val log2n = n.bitLength()
        val messageBitLength = message.size * 8
        val G = ec.g
        // Convert message to integer
        var e = BigInteger(1, message)
        if (log2n < messageBitLength)
            e = e.shiftRight(messageBitLength - log2n)
        val digest: Digest = SHA256Digest()
        val kCalculator: DSAKCalculator = HMacDSAKCalculator(digest).apply {
            init(n, value, message)
        }
        val basePointMultiplier: ECMultiplier = FixedPointCombMultiplier()
        var r: BigInteger
        var rY: BigInteger
        var s: BigInteger
        var k: BigInteger
        do { // generate s
            do { // generate r
                k = kCalculator.nextK()
                val p: ECPoint = basePointMultiplier.multiply(G, k).normalize()
                r = p.affineXCoord.toBigInteger().mod(n)
                rY = p.affineYCoord.toBigInteger().mod(n)
            }
            while (r.equals(BigInteger.ZERO))
            s = k.modInverse(n).multiply(e.add(value.multiply(r))).mod(n)
        }
        while (s.equals(BigInteger.ZERO))
        return Triple(r, s, rY)
    }

    /**
     * Produces a deterministic signature of the checksum based on RFC-6979.
     * Default checksum algorithm is SHA-256.
     */
    fun sign(message: ByteArray, digest: MessageDigest = MessageDigest.getInstance("SHA-256")): ECDSASignature {
        val checksum = digest.digest(message)
        val (r, s, rY) = rawSign(checksum)
        return ECDSASignature(r, s, rY)
    }

    private tailrec fun recDigest(msg: ByteArray, round: Int, digest: MessageDigest = MessageDigest.getInstance("SHA-256")): ByteArray {
        val d = digest.digest(msg)
        return when(round) {
            1 -> d
            else -> recDigest(d, round - 1, digest)
        }
    }

    /**
     * Base method for signing messages for various different blockchains, that loosely follow
     * the Electrum style signatures.
     */
    private fun chainSign(prefix: String, message: String, hashRounds: Int = 1,
                          digest: MessageDigest): ECDSASignature {
        val prefixLength = prefix.length
        val messageLength = message.length
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
                message.toByteArray()

        val hashed = recDigest(msgBytes, hashRounds, digest)
        val (r, s, rY) = rawSign(hashed)
        return ECDSASignature(r, s, rY)
    }

    fun electrumSign(message: String): String =
        chainSign(
            prefix = "Bitcoin Signed Message:\n",
            message = message,
            hashRounds = 2,
            digest = MessageDigest.getInstance("SHA-256")
        )
        .toBTCFormat()

    fun ethSign(message: String): String =
        chainSign(prefix = "Ethereum Signed Message:\n", message = message, digest = Keccak.Digest256())
            .toETHFormat()

    override fun getAlgorithm(): String = "EC"

    override fun getEncoded(): ByteArray? {
        val params = Secp256K1Curve.X962Parameters
        val orderBitLength: Int = Secp256K1Curve.n.bitLength()
        val keyStructure = org.bouncycastle.asn1.sec.ECPrivateKey(orderBitLength, value, params)
        val stringWriter = StringWriter()
        val pemWriter = PemWriter(stringWriter)
        return try {
            val info = PrivateKeyInfo(AlgorithmIdentifier(X9ObjectIdentifiers.id_ecPublicKey, params), keyStructure)
            val asn1Object = info.parsePrivateKey() as ASN1Object
            pemWriter.writeObject(PemObject ("EC PRIVATE KEY", asn1Object.getEncoded("DER")))
            pemWriter.flush()
            pemWriter.close()
            stringWriter.toString().toByteArray()
        } catch (e: IOException) {
            null
        }
    }

    /**
     * Export the private key using the Wallet Import Format.
     */
    override fun toString(): String = Base58.toBase58String(
        Hex.decode("80") + value.toByteArray(),
        appendChecksum = true
    )

    override fun getFormat(): String = "PKCS#8"

    fun asHex(): String = Hex.toHexString(value.toByteArray())
}
