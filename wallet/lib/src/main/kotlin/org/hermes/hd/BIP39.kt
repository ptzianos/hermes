package org.hermes.hd

import java.nio.ByteBuffer
import java.security.SecureRandom
import java.util.*
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import kotlin.math.pow
import org.bouncycastle.util.encoders.Hex
import org.hermes.crypto.CryptoAlgorithms

import org.hermes.crypto.SHA256
import org.hermes.utils.*

/**
 * An implementation of the algorithm described in BIP 39 for generating a mnemonic sentence.
 * @see{https://github.com/bitcoin/bips/blob/master/bip-0039.mediawiki}
 */
class Mnemonic(val entropy: ByteArray, val network: Network, checksum: Byte,
               words: Array<String>, passphrase: String = "") {

    enum class Entropy(val i: Int) {
        SIZE_128(128),
        SIZE_160(160),
        SIZE_192(192),
        SIZE_224(224),
        SIZE_256(256),
    }

    enum class Network(val b: ByteArray) {
        MAINNET(Hex.decode("0488ADE4")),
        TESTNET(Hex.decode("04358394"))
    }

    class InvalidWord: Exception()
    class InvalidChecksum: Exception()

    companion object {
        fun new(size: Entropy, passphrase: String = ""): Mnemonic {
            val cs = size.i / 32
            val ms = size.i + cs / 11
            val entropy = SecureRandom
                .getInstance("SHA1PRNG")
                .generateSeed(size.i)
            val checksum = SHA256.impl
                .digest(entropy).slice(0 until cs)
                .toByteArray()
            val finalEntropy = entropy + checksum
            val extractWord = fun(i: Int): String {
                val byteArray = finalEntropy
                    .slice((i * 11) until ((i + 1) * 11))
                    .toByteArray()
                val exp = ByteBuffer.wrap(byteArray).asIntBuffer()[0]
                val index = (2.0.pow(exp) - 1).toInt()
                return EnglishWordlist.items[index]
            }
            return Mnemonic(entropy, Network.MAINNET, checksum[0], Array(ms) { extractWord(it) })
        }

        /**
         * Extracts the mnemonic from a list of words.
         *
         * The process is as follows:
         * 1. Get the indices of all the words.
         * 2. Convert the indices to bytes and then concatenate them in a bitset
         * 3. Split the entropy from the checksum and convert them back into byte arrays
         */
        @Throws(InvalidWord::class)
        fun fromWordList(words: Array<String>, passphrase: String = ""): Mnemonic {
            words.forEach { if (!EnglishWordlist.index.containsKey(it)) throw InvalidWord() }
            val entropyAndChecksumBitSize = words.size * 11
            val checksumBitSize = entropyAndChecksumBitSize % 32
            val entropyBitSize = entropyAndChecksumBitSize - checksumBitSize

            val bitSet = BitSet(entropyAndChecksumBitSize)
            var bitSetIndex = 0
            for (word in words) {
                val wordIndex = EnglishWordlist.index[word] ?: continue
                bitSet.copyFromInt(wordIndex, bitSetIndex, 11)
                bitSetIndex += 11
            }
            val entropy = bitSet.asByteArray(0, entropyBitSize)
            val checksum = bitSet.asByteArray(entropyBitSize, entropyAndChecksumBitSize)[0]
            return Mnemonic(entropy, Network.MAINNET, checksum, words, passphrase)
        }
    }

    val seed: ByteArray

    init {
        words.forEach { if (!EnglishWordlist.index.containsKey(it)) throw InvalidWord() }

        // Calculate checksum of the entropy
        val expectedChecksum = SHA256.impl.digest(entropy)
        val expectedChecksumBitNum = (words.size * 11) / 33
        val expectedChecksumBits: Byte = BitSet(expectedChecksumBitNum)
            .apply { copyFromByte(expectedChecksum[0], 0, expectedChecksumBitNum, bitOffset = 8 - expectedChecksumBitNum) }
            .asByteArray(0, expectedChecksumBitNum)[0]
        if (checksum != expectedChecksumBits) throw InvalidChecksum()

        // Get seed
        val keySpec = PBEKeySpec(
            words.joinToString(" ").toCharArray(),
            "mnemonic$passphrase".toByteArray(), 2048, 512)
        val factory = SecretKeyFactory.getInstance(CryptoAlgorithms.PBKDF2_HMC_SHA512)
        seed = factory.generateSecret(keySpec).encoded
    }
}