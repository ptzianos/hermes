package org.hermes.utils

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Test


class StringsTests {
    @Test
    fun `string splitting in chunks`() {
        assertArrayEquals(
            arrayOf("aa", "bb", "cc", "dd"),
            "aabbccdd".splitInChunks(2)
        )
    }
}