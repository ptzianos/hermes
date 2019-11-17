package org.hermes.collections

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class ImmutableHashMapTest {

    @Test
    fun put() {
        val ihm = ImmutableHashMap<String, Int> {
            val ref = it
            (0..5).onEach { ref[it.toString()] = it }
        }
        assertTrue(ihm.containsKey("1"))
        assertTrue(ihm.containsKey("2"))
        ihm["6"] = 6
        assertFalse(ihm.containsKey("6"))
    }
}
