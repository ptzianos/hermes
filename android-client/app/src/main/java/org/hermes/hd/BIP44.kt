package org.hermes.hd

import org.hermes.utils.toTritArray
import java.lang.Exception

class BIP44Wallet(seed: ByteArray, keyRegistry: KeyRegistry = InMemoryKeyRegistry()) {

    class InvalidCoinType(): Exception()

    /**
     * ExtendedWallet is a class that extends the standard BIP32 compliant wallet so
     * that it becomes compliant with BIP44.
     *
     * Specifically, it injects different keys in some paths according to the specification
     * in SLIP44.
     * The ExtendedWallet is seeded once and can then be re-used for multiple different
     * currencies from the same application.
     */
    class ExtendedWallet(seed: ByteArray, private val keyRegistry: KeyRegistry = InMemoryKeyRegistry()):
        Wallet(seed, keyRegistry)
    {

        companion object {
            val BIP44Path = BIP32.normalizeToStr("m/44'") // "m/2147483692" // m/44'
        }

        /**
         * The get method will intercept all calls and will make sure to inject the correct keys
         * wherever necessary.
         *
         * The implementation is lazy, meaning that the keys will be injected only when requested.
         * It does not force the user to use just the BIP44 related paths.
         */
        override operator fun get(path: String): BIP32Key {
            BIP32.verify(path)
            val normalizedPath = BIP32.normalize(path).drop(1)
            val normalizedPathStr = BIP32.normalizeToStr(path)
            if (normalizedPathStr.startsWith(BIP44Path) and (normalizedPath.size > 1)) {
                val coinIndex = normalizedPath[1]
                val coinEntry = SLIP44.byWalletIndex[coinIndex] ?: throw InvalidCoinType()
                val childPath = BIP32.normalizeToStr("$BIP44Path/${coinEntry.walletIndex}")
                // If the special paths have not been set, then put them in place
                if ((keyRegistry.get(BIP44Path) == null) or (keyRegistry.get(childPath) == null)) {
                    val bip44Key = super.get(BIP44Path)
                    val childKey = if (coinEntry.symbol == "IOTA") {
                        // Special keys for IOTA
                        val (chainCode, key) = IOTAExPrivKey.CKDpriv(
                            coinEntry.walletIndex,
                            (bip44Key as ExPrivKey).value.toByteArray(),
                            bip44Key.public.encoded,
                            bip44Key.chainCode)
                        IOTAExPrivKey(childPath, bip44Key, chainCode, key.toTritArray().toTryteArray())
                    }
                    else {
                        // Standard ExPrivKeys
                        val encoder = when (coinEntry.symbol) {
                            "ETH" -> ETHKeyEncoder
                            else -> BTCKeyEncoder
                        }
                        val (chainCode, key) = ExPrivKey.CKDPriv(
                            coinEntry.walletIndex,
                            (bip44Key as ExPrivKey).value.toByteArray(),
                            (bip44Key as ExPrivKey).public.encoded,
                            bip44Key.chainCode)
                        ExPrivKey(childPath, bip44Key, chainCode, key, encoder)
                    }
                    keyRegistry.put(childPath, childKey)
                }
            }
            // One the keys have been put in place, proceed as normal
            return super.get(path)
        }
    }

    class Context(val coinType: String, val account: Int, val change: Int = 0, private val wallet: Wallet) {

        private val coinPath = BIP32.normalizeToStr("m/44'/${SLIP44.bySymbol[coinType]!!.walletIndex}")
        private val accountPath = "$account'/$change"
        private val internalPath = "$coinPath/$accountPath"

        operator fun get(index: Long): BIP32Key {
            if (index >= BIP32.HARDENED_KEY_OFFSET)
                throw BIP32Key.InvalidIndex()
            return wallet["$internalPath/$index"]
        }

        operator fun get(path: String): BIP32Key {
            return wallet["$internalPath/$path"]
        }

        init {
            if (!SLIP44.bySymbol.containsKey(coinType))
                throw InvalidCoinType()
        }
    }

    // Be careful accessing the wallet this way because you might cause some paths to be initialised.
    val masterWallet: Wallet = ExtendedWallet(seed, keyRegistry = keyRegistry)

    fun getWalletForCoin(coinType: String, account: Int, change: Int = 0): Context =
        Context(coinType, account, change, masterWallet).apply {
            // Use this to initialize the path
            get(0L)
        }
}