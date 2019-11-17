package org.hermes.bip32

/**
 * Simplest possible implementation of a KeyRegistry that is backed by
 * an in-memory HashMap. When an application is shut down, all keys are
 * lost.
 */
class InMemoryKeyRegistry : KeyRegistry {
    private val registry = HashMap<String, BIP32Key>()

    override fun get(path: String): BIP32Key? = registry[path]

    override fun put(path: String, key: BIP32Key) {
        if (registry.containsKey(path))
            throw KeyRegistry.DuplicateKey()
        registry[path] = key
    }
}
