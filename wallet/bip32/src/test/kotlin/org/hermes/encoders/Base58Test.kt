package org.hermes.encoders

import org.bouncycastle.util.encoders.Hex

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * Test vectors based on @see{https://github.com/luke-jr/libbase58}
 */
internal class Base58Test {

    @Test
    fun toBase58String() {
        assertEquals(
            org.bitcoinj.core.Base58.encode(Hex.decode("707070")),
            Base58.toBase58String(Hex.decode("707070"))
        )

        assertEquals(
            "19DXstMaV43WpYg4ceREiiTv2UntmoiA9j",
            Base58.toBase58String(Hex.decode("005a1fc5dd9e6f03819fca94a2d89669469667f9a0"), appendChecksum = true)
        )
    }

    @Test
    fun fromBase58String() {
        assertEquals(
            "01",
            Hex.toHexString(Base58.decode("2"))
        )

        assertEquals(
            Hex.toHexString(org.bitcoinj.core.Base58.decode("emVZ")),
            Hex.toHexString(Base58.decode("emVZ"))
        )

        assertEquals(
            "005a1fc5dd9e6f03819fca94a2d89669469667f9a0",
            Hex.toHexString(Base58.decode("19DXstMaV43WpYg4ceREiiTv2UntmoiA9j", verifyChecksum = true))
        )
    }
}