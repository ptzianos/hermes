package org.hermes.hd

import java.security.NoSuchAlgorithmException
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

import org.iota.jota.pow.SpongeFactory
import org.iota.jota.utils.Checksum
import org.iota.jota.utils.Converter
import org.iota.jota.utils.Signing

import org.hermes.crypto.CryptoAlgorithms
import org.hermes.iota.Seed
import org.hermes.iota.TryteArray
import org.hermes.utils.extendOrReduceTo
import org.hermes.utils.toByteArray
import org.hermes.utils.toTritArray
import org.hermes.utils.toTryteArray


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
            val newKey = PrivKey(I.sliceArray(0 until 32).toTryteArray().toTritIntArray(), childIndex)
            return Pair(I.sliceArray(42 until 74), newKey)
        }

        /**
         *
         */
        fun PrivKey(seed: IntArray, childIndex: Long): IntArray {
            val signing = Signing(SpongeFactory.create(SpongeFactory.Mode.KERL))
            return signing.key(seed, childIndex.toInt(), Seed.DEFAULT_SEED_SECURITY)
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
                74
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
            BIP32Key.pathOfChild(path, index),
            this,
            newChainCode,
            newKey.toTritArray().toTryteArray()
        )
    }

    constructor(path: String, parent: BIP32Key?, chainCode: ByteArray, value: ByteArray) :
            this(path, parent, chainCode, value.toTryteArray().sliceArray(0 until 81))

    override val public: BIP32PubKey by lazy {
        val pubKeyTrits = Signing(SpongeFactory.create(SpongeFactory.Mode.KERL)).digests(value.toTritIntArray())
        IOTAExPubKey(parent?.public, chainCode, path, pubKeyTrits.toTritArray().toTryteArray())
    }

    override fun getAlgorithm(): String = "Kerl"

    override fun getEncoded(): ByteArray = value.toByteArray()

    override fun getFormat(): String = "ASN.1"
}

class IOTAExPubKey(
    override val parent: BIP32PubKey?,
    override val chainCode: ByteArray,
    override val path: String,
    val value: TryteArray
) : BIP32PubKey {

    /**
     * The address without any checksum
     */
    val addressTrits: IntArray by lazy {
        val signing = Signing(SpongeFactory.create(SpongeFactory.Mode.KERL))
        signing.address(value.toTritIntArray())
    }

    /**
     * Returns an IOTA compatible address with the checksum appended to the trits
     */
    override val address: String by lazy { Checksum.addChecksum(Converter.trytes(addressTrits)) }

    override fun child(index: Long): BIP32PubKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAlgorithm(): String = "Kerl"

    override fun getEncoded(): ByteArray = value.toByteArray()

    // TODO: fix this
    override fun getFormat(): String = "ASN.1"
}
