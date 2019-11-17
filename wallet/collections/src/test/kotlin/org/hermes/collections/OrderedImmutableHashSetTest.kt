package org.hermes.collections

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class OrderedImmutableHashSetTest {

    @Test
    fun get() {
        val oihs = OrderedImmutableHashSet(listOf(0, 2, 4, 8))
        assertEquals(2, oihs[1])
    }

    @Test
    fun indexOf() {
        val oihs = OrderedImmutableHashSet(listOf(0, 2, 4, 8))
        assertEquals(2, oihs.indexOf(4))
        assertEquals(-1, oihs.indexOf(1))
    }
}
