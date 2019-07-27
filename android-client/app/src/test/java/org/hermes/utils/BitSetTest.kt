package org.hermes.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.*
import kotlin.math.pow

internal class BitSetTest {

    @Test
    fun allZerosAsByteArray() {
        assertEquals(1, BitSet(8).asByteArray(0, 8).size)
        assertEquals(2, BitSet(2 * 8).asByteArray(0, 16).size)
        assertEquals(3, BitSet(3 * 8).asByteArray(0, 24).size)
        assertEquals(3, BitSet(2 * 8 + 1).asByteArray(0, 17).size)
        assertEquals(3, BitSet(2 * 8 + 5).asByteArray(0, 21).size)
        for (b in  BitSet(2 * 8).asByteArray(0, 16))
            assertEquals(0.toByte(), b)
    }

    @Test
    fun correctAlignedByteConversion() {
        val bitSet = BitSet(24)
        bitSet.flip(0)
        bitSet.flip(8)
        bitSet.flip(16)
        for (b in bitSet.asByteArray(0, 24))
            assertEquals(1.toByte(), b)
    }

    @Test
    fun correctConversionOfComplexNumbers() {
        val bitSet = BitSet(24)
        bitSet.flip(0)
        bitSet.flip(9)
        bitSet.flip(18)
        val expected = 2.0
        var exponent = 0.0
        for (b in bitSet.asByteArray(0, 24)) {
            assertEquals(expected.pow(exponent).toInt().toByte(), b)
            exponent += 1.0
        }
    }
}