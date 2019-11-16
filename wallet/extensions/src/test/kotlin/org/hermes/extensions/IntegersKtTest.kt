package org.hermes.extensions

import org.bouncycastle.util.encoders.Hex

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class IntegersKtTest {

    @Test
    fun IntToByteArray() {
        val h1 = Hex.toHexString(2.toByteArray())
        assertEquals("02", h1)
        val h2 = Hex.toHexString(255.toByteArray())
        assertEquals("ff", h2)
        val h3 = Hex.toHexString(256.toByteArray())
        assertEquals("0100", h3)
        val h4 = Hex.toHexString(257.toByteArray())
        assertEquals("0101", h4)
        val h5 = Hex.toHexString(630.toByteArray())
        assertEquals("0276", h5)
    }

}