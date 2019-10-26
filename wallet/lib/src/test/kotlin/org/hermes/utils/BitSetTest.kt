package org.hermes.utils

import java.util.*

import kotlin.math.pow

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class BitSetTest {

    @Test
    fun allZerosAsByteArray() {
        assertEquals(1, BitSet(8).toByteArray(0, 8).size)
        assertEquals(2, BitSet(2 * 8).toByteArray(0, 16).size)
        assertEquals(3, BitSet(3 * 8).toByteArray(0, 24).size)
        assertEquals(3, BitSet(2 * 8 + 1).toByteArray(0, 17).size)
        assertEquals(3, BitSet(2 * 8 + 5).toByteArray(0, 21).size)
        for (b in  BitSet(2 * 8).toByteArray(0, 16))
            assertEquals(0.toByte(), b)
    }

    @Test
    fun correctAlignedByteConversion() {
        val bitSet = BitSet(24)
        bitSet.flip(7)
        bitSet.flip(15)
        bitSet.flip(23)
        for (b in bitSet.toByteArray(0, 24))
            assertEquals(1.toByte(), b)
    }

    @Test
    fun correctConversionOfComplexNumbers() {
        val bitSet = BitSet(24)
        bitSet.flip(7)
        bitSet.flip(14)
        bitSet.flip(21)
        val expected = 2.0
        var exponent = 0.0
        for (b in bitSet.toByteArray(0, 24)) {
            assertEquals(expected.pow(exponent).toInt().toByte(), b)
            exponent += 1.0
        }
    }

    @Test
    fun correctConversionOfUnalignedNumbers() {
        val bitSet = BitSet(4)
        bitSet.flip(2)
        bitSet.flip(3)
        assertEquals(48.toByte(), bitSet.toByteArray(0, 4)[0])
    }

    @Test
    fun convertUnalignedBitSet() {
        val bs = BitSet(2 * 8 - 1).apply { flip(14) }.toByteArray(0, 16)
        assertEquals(2, bs.size)
        assertEquals("0002", bs.toHexStr())
    }
}