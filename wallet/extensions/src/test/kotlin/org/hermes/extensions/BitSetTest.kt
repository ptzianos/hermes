package org.hermes.extensions

import java.util.BitSet
import org.bouncycastle.util.encoders.Hex
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class BitSetTest {

    @Test
    fun BitSetToString() {
        val bs = BitSet(4).apply {
            flip(1)
            flip(3)
        }
        assertEquals("0101", bs.toString(0, 4))
        assertEquals("101", bs.toString(1, 4))
    }

    @Test
    fun BitSetCopyFromInt() {
        assertEquals("11", BitSet(4).copyFromInt(11, 0, 2).toString(0, 2))
        assertEquals("110", BitSet(4).copyFromInt(11, 0, 3).toString(0, 3))
        assertEquals("1101", BitSet(4).copyFromInt(11, 0, 4).toString(0, 4))
        // Copy all the bits of the integer into the bitset, then convert it to byte array and get the same result
        assertEquals(Hex.toHexString(11.toByteArray()), Hex.toHexString(BitSet(4).copyFromInt(11, 0, 8).toByteArray()))
        // Example taken from BIP39 test vector.
        // The first byte of the checksum is 9d. We need to extract the six left most bytes of this.
        assertEquals(-100, Hex.toHexString(BitSet(4).copyFromByte(Hex.decode("9d")[0], 0, 6, bitOffset = 1).toByteArray()))
    }
}
