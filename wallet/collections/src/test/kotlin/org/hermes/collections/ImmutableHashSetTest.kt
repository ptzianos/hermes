package org.hermes.collections

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ImmutableHashSetTest {

    @Test
    fun contains() {
        val ihs = ImmutableHashSet(listOf(1, 2, 3, 4))
        assertTrue(ihs.contains(1))
        assertTrue(ihs.contains(2))
        assertTrue(ihs.contains(3))
        assertTrue(ihs.contains(4))
        assertFalse(ihs.contains(5))
    }

    @Test
    fun size() {
        assertEquals(4, ImmutableHashSet(listOf(1, 2, 3, 4)).size())
    }
}
