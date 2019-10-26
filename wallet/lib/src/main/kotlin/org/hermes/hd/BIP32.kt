package org.hermes.hd

import java.lang.Exception
import java.math.BigInteger
import java.security.PrivateKey
import java.security.PublicKey

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import org.bouncycastle.math.ec.ECPoint
import org.bouncycastle.math.ec.FixedPointCombMultiplier
import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions.pow

import org.hermes.crypto.CryptoAlgorithms.HMAC_SHA512
import org.hermes.crypto.*
import org.hermes.hd.BIP32.HARDENED_KEY_OFFSET
import org.hermes.hd.BIP32.normalizeToStr
import org.hermes.utils.endsWithAnyOf
import org.hermes.utils.extendOrReduceTo
import org.hermes.utils.toBigInt
import org.hermes.utils.toByteArray


object BIP32 {

    class InvalidPath: Exception()

    val HARDENED_BIT = pow(2.toLong(), 31)
    val HARDENED_KEY_OFFSET = pow(2.toLong(), 31)
    val MAX_KEY_INDEX = pow(2.toLong(), 32)

    /**
     * Verifies a BIP32 compliant key path.
     */
    fun verify(path: String) {
        if (!Regex("^m(/[0-9]+['H]?)*\$").matches(path))
            throw InvalidPath()
        for (fragment in path.split("/").drop(1)) {
            val cleanFragment = when (fragment.endsWithAnyOf("'", "H")) {
                true -> fragment.dropLast(1).toLong() + HARDENED_KEY_OFFSET
                else -> fragment.toLong()
            }
            if ((cleanFragment >= MAX_KEY_INDEX) or (cleanFragment < 0))
                throw InvalidPath()
        }
    }

    /**
     * Converts a path of BIP32 path into a list of indices.
     */
    fun normalize(path: String): Iterable<Long> = path
        .split("/")
        .map { when {
            it == "m" -> 0
            it.endsWithAnyOf("'", "H") -> it.dropLast(1).toLong() + HARDENED_KEY_OFFSET
            else -> it.toLong()
        } }

    fun normalizeToStr(path: String): String = when (path) {
        "m" -> path
        else -> "m/${normalize(path).drop(1).joinToString("/")}"
    }

    fun verifyAndNormalize(path: String): Iterable<Long> {
        verify(path)
        return normalize(path)
    }

    fun pathOfChild(currentPath: String, childIndex: Long): String = "${normalizeToStr(currentPath)}/$childIndex"
}

interface BIP32Key: PrivateKey {
    class InvalidIndex: Exception()

    class NoSibling: Exception()

    companion object {
        /**
         * Returns the index of the current key.
         */
        fun currentIndex(path: String): Long {
            val currentIndex = path.split("/").last()
            return when {
                currentIndex.endsWith("'") -> currentIndex.replace("'", "").toLong() + BIP32.HARDENED_KEY_OFFSET
                currentIndex.endsWith("H") -> currentIndex.replace("H", "").toLong() + BIP32.HARDENED_KEY_OFFSET
                currentIndex == "m" -> 0
                else -> currentIndex.toLong()
            }
        }
    }

    val path: String

    val index: Long
        get() = currentIndex(path)

    val parent: BIP32Key?

    val chainCode: ByteArray

    val hardened: Boolean
        get() = index >= BIP32.HARDENED_KEY_OFFSET

    val depth: Int
        get() = path.split("/").size - 1

    val public: BIP32PubKey

    fun child(index: Long = 0): BIP32Key

    /**
     * Returns the BIP32Key key that belongs in the same chain and has the next index.
     * @throws NoSibling when it's the master node or the index is greater than 2^32 - 1.
     */
    fun sibling(): BIP32Key = when {
        index >= BIP32.MAX_KEY_INDEX - 1 -> throw NoSibling()
        else -> parent?.child(index + 1) ?: throw NoSibling()
    }
}

interface BIP32PubKey: PublicKey {

    class NoPubKey: Exception()

    val path: String

    val index: Long
        get() = BIP32Key.currentIndex(path)

    val parent: BIP32PubKey?

    val chainCode: ByteArray

    val depth: Int
        get() = path.split("/").size - 1

    val identifier: ByteArray
        get() = RIPEMD.hash(SHA256.hash(encoded))

    val fingerprint: ByteArray
        get() = identifier.sliceArray(0 until 4)

    fun child(index: Long): BIP32PubKey

    val address: String
}

class ExPrivKey(
    override val path: String,
    override val parent: BIP32Key?,
    override val chainCode: ByteArray,
    key: BigInteger,
    val encoder: KeyEncoder<ExPrivKey, ExPubKey> = BTCKeyEncoder
) : BIP32Key, SecP256K1PrivKey(key) {

    init { BIP32.verify(path) }

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
            val digest = Mac.getInstance(HMAC_SHA512).apply { init(SecretKeySpec(chainCode, HMAC_SHA512)) }
            val I = when (index >= HARDENED_KEY_OFFSET) {
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
        val (chainCode, key) = CKDPriv(index, value.toByteArray(), public.encoded, chainCode)
        return ExPrivKey(BIP32.pathOfChild(path, index), this, chainCode, key)
    }

    override val public: ExPubKey by lazy { ExPubKey(parent?.public, chainCode, path, this, encoder) }

    override fun toString(): String = encoder.encodePrivateKey(this, hashMapOf())
}

class ExPubKey(
    override val parent: BIP32PubKey?,
    override val chainCode: ByteArray,
    override val path: String,
    x :BigInteger,
    y: BigInteger,
    val encoder: KeyEncoder<ExPrivKey, ExPubKey> = BTCKeyEncoder
):
    SecP256K1PubKey(x, y), BIP32PubKey {

    override fun getEncoded(): ByteArray = bcPoint.getEncoded(true)

    constructor(parent: BIP32PubKey?, chainCode: ByteArray, path: String, publicKeyECPoint: ECPoint,
                encoder: KeyEncoder<ExPrivKey, ExPubKey> = BTCKeyEncoder):
        this(
            parent, chainCode, path,
            publicKeyECPoint.affineXCoord.toBigInteger(),
            publicKeyECPoint.affineYCoord.toBigInteger(),
            encoder
        )

    constructor(parent: BIP32PubKey?, chainCode: ByteArray, path: String, rawKey: BigInteger):
        this(
            parent, chainCode, path,
            FixedPointCombMultiplier()
                .multiply(Secp256K1Curve.X9ECParameters.g, rawKey)
                .normalize()
        )

    constructor(parent: BIP32PubKey?, chainCode: ByteArray, path: String, privateKey: SecP256K1PrivKey,
                encoder: KeyEncoder<ExPrivKey, ExPubKey> = BTCKeyEncoder):
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

        if (index >= HARDENED_KEY_OFFSET)
            throw BIP32PubKey.NoPubKey()

        val I = Mac.getInstance(HMAC_SHA512)
            .apply { init(SecretKeySpec(chainCode, HMAC_SHA512)) }
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

interface KeyRegistry {
    class DuplicateKey: Exception()

    /**
     * Return the key that corresponds to this path.
     *
     * If the key does not exist, a null will be returned
     */
    fun get(path: String): BIP32Key?

    /**
     * Map a key to a path.
     *
     * If the key is already mapped, an exception will be thrown.
     * Keys can not be overwritten!
     */
    fun put(path: String, key: BIP32Key)
}

abstract class KeyEncoder<in PrK : BIP32Key, in PuK: BIP32PubKey> {
    abstract fun encodePrivateKey(key: PrK, options: Map<String, Any>): String

    abstract fun encodePublicKey(key: PuK, options: Map<String, Any>): String

    abstract fun address(key: PuK, options: Map<String, Any>): String
}

interface Signer<in K : PrivateKey> {
    fun sign(key: K, data: ByteArray): ByteArray
}

/**
 * Simplest possible implementation of a KeyRegistry that is backed by
 * an in-memory HashMap. When an application is shut down, all keys are
 * lost.
 */
class InMemoryKeyRegistry: KeyRegistry {
    private val registry = HashMap<String, BIP32Key>()

    override fun get(path: String): BIP32Key? = registry[path]

    override fun put(path: String, key: BIP32Key) {
        if (registry.containsKey(path))
            throw KeyRegistry.DuplicateKey()
        registry[path] = key
    }
}

open class Wallet(seed: ByteArray, private val keyRegistry: KeyRegistry = InMemoryKeyRegistry()) {

    open operator fun get(path: String): BIP32Key {
        BIP32.verify(path)
        val normalizedPath = normalizeToStr(path)
        val cachedKey = keyRegistry.get(normalizedPath)
        if (cachedKey != null) return cachedKey
        var key: BIP32Key = master
        var partialPath = master.path
        for (index in BIP32.normalize(path).drop(1)) {
            partialPath = normalizeToStr(BIP32.pathOfChild(partialPath, index))
            val intermediateKey = keyRegistry.get(partialPath)
            if (intermediateKey == null) {
                key = key.child(index)
                keyRegistry.put(key.path, key)
            } else {
                key = intermediateKey
            }
        }
        return key
    }

    /**
     * Master node will always be a standard Extended Private Key. The other nodes might be of
     * different kinds depending on the network they will be used for.
     */
    val master: ExPrivKey

    init {
        val I = Mac.getInstance(HMAC_SHA512)
            .apply { init(SecretKeySpec("Bitcoin seed".toByteArray(), HMAC_SHA512)) }
            .doFinal(seed)

        master = ExPrivKey(
            path = "m",
            parent = null,
            chainCode = I.sliceArray(32 until 64),
            key = I.sliceArray(0 until 32).toBigInt(positive = true))

        keyRegistry.put("m", master)
    }
}