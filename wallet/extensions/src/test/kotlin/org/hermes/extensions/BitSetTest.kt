package org.hermes.extensions

import java.util.BitSet
import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BitSetTest {

    @Test
    fun testEntireBitSetToString() {
        val bs = BitSet(4).apply {
            flip(1)
            flip(3)
        }
        assertEquals("0101", bs.toString(0, 4))
    }

    @Test
    fun testPartialBitSetToString() {
        val bs = BitSet(4).apply {
            flip(1)
            flip(3)
        }
        assertEquals("101", bs.toString(1, 4))
    }

    @Test
    fun testPartialCopyFromInt() {
        assertEquals("11", BitSet(4).copyFromInt(11, 0, 2).toString(0, 2))
        assertEquals("110", BitSet(4).copyFromInt(11, 0, 3).toString(0, 3))
    }

    @Test
    fun testCopyFromInt() {
        assertEquals("1101", BitSet(4).copyFromInt(11, 0, 4).toString(0, 4))
    }

    @Test
    fun testToByteArray() {
        // Copy all the bits of the integer into the bitset, then convert it to byte array and get the same result
        assertEquals(Hex.toHexString(11.toByteArray()), Hex.toHexString(BitSet(4).copyFromInt(11, 0, 8).toByteArray()))
    }

    @Test
    fun testCopyFromByte() {
        // Example taken from a BIP39 test vector.
        // The first byte of the checksum is 9d. We need to extract the six left most bytes of this.
        assertEquals(
            "9c",
            Hex.toHexString(
                BitSet(6).copyFromByte(
                    Hex.decode("9d")[0],
                    0,
                    6,
                    bitOffset = 2,
                    preserveEndianness = true
                ).toByteArray(0, 6)
            )
        )
    }

    @Test
    fun testCopyFromIntPreserveEndianness() {
        // Example taken from a BIP39 test vector.
        // The index of a word is 3. 11 bits will be copied into the BitSet.
        assertEquals(
            "00000000011",
            BitSet(11).copyFromInt(3, 0, 11, 0, preserveEndianness = true).toString(0, 11)
        )
    }
}
