package org.hermes.hd

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class BIP44WalletTest {

    @Test
    fun getWallet() {
        val words = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about".split(' ')
        val wallet = BIP44Wallet(seed = Mnemonic.fromWordList(words.toTypedArray()).seed)
        wallet.getWalletForCoin("BTC", 0, 0)
        wallet.getWalletForCoin("ETH", 0, 0)
        val iotaWallet = wallet.getWalletForCoin("IOTA", 0, 0)
        assertTrue(wallet.masterWallet["m"] is ExPrivKey)
        assertTrue(wallet.masterWallet["m/44'"] is ExPrivKey)
        assertTrue(wallet.masterWallet["m/44'/${SLIP44.bySymbol["BTC"]!!.walletIndex}/0'/0"] is ExPrivKey)
        assertTrue(wallet.masterWallet["m/44'/${SLIP44.bySymbol["ETH"]!!.walletIndex}/0'/0"] is ExPrivKey)
        assertTrue(wallet.masterWallet["m/44'/${SLIP44.bySymbol["IOTA"]!!.walletIndex}/0'/0"] is IOTAExPrivKey)
        assertTrue(iotaWallet[0] is IOTAExPrivKey)
        assertArrayEquals(
            (iotaWallet[0] as IOTAExPrivKey).value.toByteArray(),
            (wallet.masterWallet["m/44'/${SLIP44.bySymbol["IOTA"]!!.walletIndex}/0'/0/0"] as IOTAExPrivKey).value.toByteArray()
        )
    }
}