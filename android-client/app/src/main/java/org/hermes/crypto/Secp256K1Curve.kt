package org.hermes.crypto

import org.bouncycastle.asn1.x9.X962Parameters
import java.math.BigInteger
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec
import java.security.spec.ECPoint
import java.security.spec.EllipticCurve
import org.bouncycastle.asn1.x9.X9ECParameters
import org.bouncycastle.crypto.ec.CustomNamedCurves
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.math.ec.FixedPointUtil
import org.bouncycastle.util.encoders.Hex


/**
 * Object that contains all the important bits and pieces for generating Keypairs
 * using the SecP256K1 ECC curve.
 * Some code has been modified from these repos:
 * @see <a href="https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/crypto/DeterministicKey.java">DeterministicKey.java</a>
 * @see <a href="https://github.com/web3j/web3j/blob/master/crypto/src/main/java/org/web3j/crypto/ECDSASignature.java">ECDSASignature.java</a>
 * @see <a href="https://www.javatips.net/api/updatefx-master/api/src/main/java/com/vinumeris/updatefx/Crypto.java">Crypto.java</a>
 */
object Secp256K1Curve {

    val X9ECParameters: X9ECParameters = CustomNamedCurves.getByName("secp256k1")
    val ecDomainParameters: ECDomainParameters = ECDomainParameters(
        X9ECParameters.curve, X9ECParameters.g, X9ECParameters.n, X9ECParameters.h)
    val halfCurveOrder: BigInteger = X9ECParameters.n.shiftRight(1)
    val X962Parameters: X962Parameters = X962Parameters(X9ECParameters)

    init {
        // Tell Bouncy Castle to pre-compute data that's needed during secp256k1 calculations.
        FixedPointUtil.precompute(X9ECParameters.g)
    }

    /**
     * ECParameterSpec according to the official description of the SECP256K1 Curve from:
     * @see <a href="https://en.bitcoin.it/wiki/Secp256k1">
     * <p>For the time being this cannot be used to generate a keypair in Android because
     * neither BoringSSL not OpenSSL support the generation of this curve even though it
     * can be used with any library that supports ECC.</p>
     * <p>Also, the BouncyCastle provider has been deprecated in the latest versions of the
     * AndroidAPI in favor of the AndroidOpenSSL provider which is built around BoringSSL.
     * There does not seem to be any willingness to support this curve any time soon as
     * evidenced by this issue:
     * @see <a href="https://github.com/google/conscrypt/issues/572">
     * Should the curve become available from one of the standard Providers available in
     * Android, this parameter specification will be useful to produce keys in a more standard
     * way.</p>
     */
    val a = BigInteger.ZERO
    val b = BigInteger.valueOf(7)
    val p = BigInteger(1, Hex.decode("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEFFFFFC2F"))
    val xOfG = BigInteger(1, Hex.decode("79be667ef9dcbbac55a06295ce870b07029bfcdb2dce28d959f2815b16f81798"))
    val yOfG = BigInteger(1, Hex.decode("483ada7726a3c4655da4fbfc0e1108a8fd17b448a68554199c47d08ffb10d4b8"))
    val G = ECPoint(xOfG, yOfG)
    val n = BigInteger(1, Hex.decode("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFEBAAEDCE6AF48A03BBFD25E8CD0364141"))
    val h = 1
    val ecField = ECFieldFp(p)
    val ellipticCurve = EllipticCurve(ecField, a, b)


    val ecParameterSpec = ECParameterSpec(ellipticCurve, G, n, h)
}