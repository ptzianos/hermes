package org.hermes.bip32

import java.lang.Exception

interface KeyRegistry {
    class DuplicateKey : Exception()

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
