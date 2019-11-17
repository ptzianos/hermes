package org.hermes.bip32

import java.lang.Exception
import java.security.PrivateKey

interface BIP32Key : PrivateKey {
    class InvalidIndex : Exception()

    class NoSibling : Exception()

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
