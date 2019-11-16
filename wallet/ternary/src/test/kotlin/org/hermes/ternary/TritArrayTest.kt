package org.hermes.ternary

import org.iota.jota.utils.Converter

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class TritArrayTest {

    @Test
    fun `tryte array to int array`() {
        assertEquals(12.toByte(), Converter.bytes(intArrayOf(0, 1, 1))[0])
        assertArrayEquals(intArrayOf(0, 1, 1), 12.toTryteArray().toTritArray().toTritIntArray())
    }

}