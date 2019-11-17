package org.hermes.bip44.iota

import java.security.NoSuchAlgorithmException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import org.hermes.bip32.BIP32
import org.hermes.bip32.BIP32Key
import org.hermes.bip32.BIP32PubKey
import org.hermes.crypto.CryptoAlgorithms
import org.hermes.extensions.extendOrReduceTo
import org.hermes.extensions.toByteArray
import org.hermes.ternary.TryteArray
import org.hermes.ternary.toTritArray
import org.hermes.ternary.toTryteArray
import org.iota.jota.pow.SpongeFactory
import org.iota.jota.utils.Signing

class IOTAExPrivKey(
    override val path: String,
    override val parent: BIP32Key?,
    override val chainCode: ByteArray,
    val value: TryteArray
) : BIP32Key {

    companion object {

        /**
         * Produces a child key for the IOTA network.
         *
         * Produces a child key, from a parent, private or public, depending
         * on whether or not the the child is hardened.
         * The chaincode is combined with the key and then fed into a PBKDF2
         * function that will stretch it to the appropriate length. This is
         * necessary because the key might be from a non-IOTA parent and hence
         * will not be at the proper size.
         * Once the key has been produced it is split into two chunks, a
         * 32-byte chaincode and a 42-byte IOTA seed. The IOTA seed is then
         * used in combination with the index to produce a private key for the
         * child. This way the seed for a chain of keys can be exported and
         * fed to the standard Trinity wallet and allow that wallet to recreate
         * this chain of keys and thus track the addresses used.
         */
        fun CKDpriv(
            childIndex: Long,
            key: ByteArray,
            publicKey: ByteArray,
            chainCode: ByteArray
        ): Pair<ByteArray, IntArray> {
            val I = BIP32Seed(childIndex, key, publicKey, chainCode)
            val seed = I.sliceArray(0 until 41)
                .toTryteArray()
                .toTritIntArray()
                .sliceArray(0 until Seed.MAX_SEED_LENGTH * 3)
            val newKey = PrivKey(seed, childIndex)
            return Pair(I.sliceArray(42 until 74), newKey)
        }

        fun PrivKey(seed: IntArray, childIndex: Long): IntArray {
            val signing = Signing(SpongeFactory.create(SpongeFactory.Mode.KERL))
            // Indices can be reused because the hardened key seed will be different than the non-hardened one.
            // This is done because IOTA can not handle indices that are too large.
            val index = when (childIndex >= BIP32.HARDENED_KEY_OFFSET) {
                true -> childIndex - BIP32.HARDENED_KEY_OFFSET
                else -> childIndex
            }.toInt()
            return signing.key(seed, index, Seed.DEFAULT_SEED_SECURITY)
        }

        /**
         * Produces a seed in a way that resembles the BIP32 spec. The parent key can be
         * either another IOTAExPrivKey or any BIP32 key.
         */
        fun BIP32Seed(
            childIndex: Long,
            key: ByteArray,
            publicKey: ByteArray,
            chainCode: ByteArray
        ): ByteArray {
            if ((childIndex < 0) or (childIndex >= BIP32.MAX_KEY_INDEX))
                throw BIP32Key.InvalidIndex()

            val spec = PBEKeySpec(
                when (childIndex >= BIP32.HARDENED_KEY_OFFSET) {
                    true -> key + childIndex.toByteArray().extendOrReduceTo(4, padStart = true)
                    else -> publicKey + childIndex.toByteArray().extendOrReduceTo(4, padStart = true)
                }
                    .map { it.toChar() }
                    .toCharArray(),
                chainCode,
                10000,
                74 * 8
            )
            return try {
                SecretKeyFactory.getInstance(CryptoAlgorithms.PBKDF2_HMC_SHA512)
            } catch (e: NoSuchAlgorithmException) {
                // TODO: Do some proper exception handling here
                throw e
            }.generateSecret(spec).encoded
        }
    }

    override fun child(index: Long): BIP32Key {
        val (newChainCode, newKey) = CKDpriv(index, value.toByteArray(), public.encoded, chainCode)

        return IOTAExPrivKey(
            BIP32.pathOfChild(path, index),
            this,
            newChainCode,
            newKey.toTritArray().toTryteArray()
        )
    }

    override val public: BIP32PubKey by lazy {
        val pubKeyTrits = Signing(SpongeFactory.create(SpongeFactory.Mode.KERL)).digests(value.toTritIntArray())
        IOTAExPubKey(parent?.public, chainCode, path, pubKeyTrits.toTritArray().toTryteArray())
    }

    override fun getAlgorithm(): String = "Kerl"

    override fun getEncoded(): ByteArray = value.toByteArray()

    override fun getFormat(): String = "ASN.1"
}
