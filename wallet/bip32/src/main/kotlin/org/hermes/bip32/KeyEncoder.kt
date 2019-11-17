package org.hermes.bip32

abstract class KeyEncoder<in PrK : BIP32Key, in PuK : BIP32PubKey> {
    abstract fun encodePrivateKey(key: PrK, options: Map<String, Any>): String

    abstract fun encodePublicKey(key: PuK, options: Map<String, Any>): String

    abstract fun address(key: PuK, options: Map<String, Any>): String
}
