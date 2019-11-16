package org.hermes.crypto

import java.lang.Exception
import java.math.BigInteger

import org.bouncycastle.util.encoders.Hex

/**
 * An ECDSA Signature.
 * Code modified from: org.web3j.crypto.ECDSASignature
 */
class ECDSASignature(val r: BigInteger, val s: BigInteger, val rY: BigInteger? = null) {

    class NoEvenBit: Exception()

    val TWO = BigInteger(byteArrayOf(2.toByte()))
    val vb: Byte?
    val rb: ByteArray
    val sb: ByteArray
    val canonicalSb: ByteArray

    init {
        val zeroByte = 0.toByte()
        val normalizeByteArray = fun(ar: ByteArray): ByteArray {
            val offset = if (ar[0] == zeroByte && ar.size > 32) 1 else 0
            return ByteArray(32) { i ->
                when {
                    i < ar.size - offset -> ar[i + offset]
                    else -> 0.toByte()
                }
            }
        }
        rb = normalizeByteArray(r.toByteArray())
        sb = normalizeByteArray(s.toByteArray())
        canonicalSb = if (s.multiply(TWO) < Secp256K1Curve.n) sb
                      else normalizeByteArray(Secp256K1Curve.X9ECParameters.n.subtract(s).toByteArray())
        if (rY == null) vb = null
        else {
            val yMod2 = rY.mod(TWO).toInt()
            val lessThanHalf = if (s.multiply(TWO) < Secp256K1Curve.n) 0 else 1
            vb = (27 + yMod2.xor(lessThanHalf)).toByte()
        }
    }

    /**
     * @return true if the S component is "low", that means it is below
     * {@link Secp256K1Curve#halfCurveOrder}. See
     * <a href="https://github.com/bitcoin/bips/blob/master/bip-0062.mediawiki#Low_S_values_in_signatures">
     * BIP62</a>.
     */
    fun isCanonical(): Boolean = s.compareTo(Secp256K1Curve.halfCurveOrder) <= 0

    /**
     * Puts both integers in one continuous byte array
     */
    fun toBinaryFormat(): ByteArray {
        val completeArray = ByteArray(64)
        System.arraycopy(r.toByteArray(), 0, completeArray, 0, 32)
        System.arraycopy(s.toByteArray(), 0, completeArray, 32, 32)
        return completeArray
    }

    /**
     * Returns the ECDSA signature as a hexed string
     */
    fun toHexedBinary(): String = Hex.toHexString(toBinaryFormat())
    //DER encoding
    //
    //For reference, here is how to encode signatures correctly in DER format.
    //
    //0x30 [total-length] 0x02 [R-length] [R] 0x02 [S-length] [S] [sighash-type]
    //
    //    total-length: 1-byte length descriptor of everything that follows, excluding the sighash byte.
    //    R-length: 1-byte length descriptor of the R value that follows.
    //    R: arbitrary-length big-endian encoded R value. It cannot start with any 0x00 bytes, unless the first byte that follows is 0x80 or higher, in which case a single 0x00 is required.
    //    S-length: 1-byte length descriptor of the S value that follows.
    //    S: arbitrary-length big-endian encoded S value. The same rules apply as for R.
    //    sighash-type: 1-byte hashtype flag (only 0x01, 0x02, 0x03, 0x81, 0x82 and 0x83 are allowed).
    //
    //This is already enforced by the reference client as of version 0.8.0 (only as relay policy, not as a consensus rule).
    //
    //This rule, combined with the low S requirement above, results in S-length being at most 32 (and R-length at most 33), and the total signature size being at most 72 bytes (and on average 71.494 bytes).
}