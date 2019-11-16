package org.hermes.ternary

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class TryteTest {

    @Test
    fun `from char works`() {
        val tritArray = Tryte('N').toTritArray().trimToTryteSize()
        assertEquals(tritArray.size, 3)
        assertEquals(tritArray[0].i, 1)
        assertEquals(tritArray[1].i, 0)
        assertEquals(tritArray[2].i, 0)
    }

    @Test
    fun `from lowercase char works`() {
        assertEquals(1, Tryte('n').decimalValue)
    }

    @Test
    fun `from random char throws error`() {
        assertThrows(InvalidTryte::class.java) { Tryte('1'); }
    }

    @Test
    fun addition() {
        assertEquals(1, Tryte('N').decimalValue)
        assertEquals(Tryte('Y').decimalValue, 12)
        assertEquals(13, (Tryte('N') + Tryte('Y')).decimalValue)
    }

}