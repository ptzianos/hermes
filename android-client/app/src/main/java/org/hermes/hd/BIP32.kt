package org.hermes.hd

import java.lang.Exception
import java.math.BigInteger
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions.pow

import org.hermes.crypto.CryptoAlgorithms.HMAC_SHA512
import org.hermes.crypto.RIPEMD
import org.hermes.crypto.SHA256
import org.hermes.crypto.SecP256K1PrivKey
import org.hermes.crypto.Secp256K1Curve
import org.hermes.utils.toBigInt
import org.hermes.utils.toByteArray


object BIP32 {

    class InvalidPath: Exception()

    /**
     * Verifies a BIP32 compliant key path.
     */
    fun verify(path: String) {
        if (!Regex("^m(/[0-9]+'?)*\$").matches(path))
            throw InvalidPath()
        for (fragment in path.split("/").drop(1)) {
            if (fragment.contains("'")) {
                val cleanFragment = fragment.dropLast(1).toInt()
                if ((cleanFragment >= pow(2, 31)) or (cleanFragment < 0)) {
                    throw InvalidPath()
                }
            } else if (!fragment.contains("'")) {
                if ((fragment.toInt() >= pow(2, 32)) or (fragment.toInt() < 0)) {
                    throw InvalidPath()
                }
            }
        }
    }

    /**
     * Converts a path of BIP32 path into a list of indices.
     */
    fun normalize(path: String): Iterable<Int> = path
        .split("/")
        .map { when {
            it == "m" -> pow(2, 31)
            it.endsWith("'") -> it.dropLast(1).toInt()
            else -> it.toInt()
        } }
}

interface BIP32Key {
    class InvalidIndex: Exception()

    class NoSibling: Exception()

    companion object {
        /**
         * Returns the index of the current key.
         */
        fun currentIndex(path: String): Int {
            val currentIndex = path.split("/").last()
            return when {
                currentIndex.endsWith("'") -> currentIndex.replace("'", "").toInt() + pow(2, 31)
                currentIndex == "m" -> 0
                else -> currentIndex.toInt()
            }
        }

        fun pathOfChild(currentPath: String, childIndex: Int): String = "$currentPath/$childIndex"
    }

    val path: String

    val index: Int
        get() = currentIndex(path)

    val parent: BIP32Key?

    val chainCode: ByteArray

    val hardened: Boolean
        get() = index >= pow(2, 31)

    val depth: Int
        get() = path.split("/").size - 1

    val fingerprint: ByteArray
        get() = RIPEMD.hash(SHA256.hash(chainCode))

    // TODO: Fix me
    val public: String

    fun child(index: Int = 0): BIP32Key

    /**
     * Returns the BIP32Key key that belongs in the same chain and has the next index.
     * @throws NoSibling when it's the master node or the index is greater than 2^32 - 1.
     */
    fun sibling(): BIP32Key = when {
        index >= pow(2, 32) - 1 -> throw NoSibling()
        else -> parent?.child(index + 1) ?: throw NoSibling()
    }
}

class ExPrivKey(
    override val path: String,
    override val parent: ExPrivKey?,
    override val chainCode: ByteArray,
    key: BigInteger,
    val encoder: KeyEncoder<ExPrivKey> = BTCKeyEncoder
) : BIP32Key, SecP256K1PrivKey(key) {

    init { BIP32.verify(path) }

    override fun child(index: Int): BIP32Key {
        if ((index < 0) or (index >= pow(2, 32)))
            throw BIP32Key.InvalidIndex()

        val n = Secp256K1Curve.X9ECParameters.n
        val digest = Mac.getInstance(HMAC_SHA512).apply { init(SecretKeySpec(chainCode, HMAC_SHA512)) }
        val I = when (index >= pow(2, 31)) {
            true -> digest.doFinal(ByteArray(1) + value.toByteArray() + index.toByteArray())
            else -> digest.doFinal(ByteArray(1) + exPubKey.encoded + index.toByteArray())
        }
        return ExPrivKey(
            BIP32Key.pathOfChild(path, index),
            this,
            I.sliceArray(32 until 64),
            I.sliceArray(0 until 32).toBigInt() + value.mod(n)
        )
    }

    val exPubKey: ExPubKey by lazy { ExPubKey(index, parent?.exPubKey, chainCode, ) }

    override fun toString(): String = encoder.encodePrivateKey(this, hashMapOf())
}

// TODO: Implement me
class ExPubKey(val index: Int, val parent: ExPubKey?, val chainCode: ByteArray, x :BigInteger, y: BigInteger):
    SecP256K1PubKey(x, y) {

    override val encoded: ByteArray by lazy { bcPoint.getEncoded(true) }

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

abstract class KeyEncoder<in K : BIP32Key> {
    abstract fun encodePrivateKey(key: K, options: Map<String, Any>): String

// TODO: add this as soon as the public keys are finished
//    fun encodePublicKey(): String
}

interface Signer {
    fun sign(key: BIP32Key, data: ByteArray): ByteArray
}

/**
 * Simplest possible implementation of a KeyRegistry that is backed by
 * an in-memory HashMap. When an application is shut down, all keys are
 * lost.
 */
class InMemoryKeyRegistry: KeyRegistry {
    private val registry = HashMap<String, BIP32Key>()

    override fun get(path: String): BIP32Key? {
        return registry[path]
    }

    override fun put(path: String, key: BIP32Key) {
        if (registry.containsKey(path))
            throw KeyRegistry.DuplicateKey()
        registry[path] = key
    }
}

class Wallet(seed: ByteArray, private val keyRegistry: KeyRegistry = InMemoryKeyRegistry()) {

    operator fun get(path: String): BIP32Key {
        BIP32.verify(path)
        val cachedKey = keyRegistry.get(path)
        if (cachedKey != null) return cachedKey
        var key: BIP32Key = master
        for (fragment in BIP32.normalize(path).drop(1)) {
            key = key.child(fragment)
        }
        return key
    }

    /**
     * Master node will always be a standard Extended Private Key. The other nodes might be of
     * different kinds depending on the network they will be used for.
     */
    private val master: ExPrivKey

    init {
        val I = Mac.getInstance(HMAC_SHA512)
            .apply { init(SecretKeySpec("Bitcoin seed".toByteArray(), HMAC_SHA512)) }
            .doFinal(seed)

        master = ExPrivKey(
            path = "m",
            parent = null,
            chainCode = I.sliceArray(32 until 64),
            key = I.sliceArray(0 until 32).toBigInt(positive = true))
    }
}
