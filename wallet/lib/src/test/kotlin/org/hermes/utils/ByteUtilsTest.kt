package org.hermes.utils

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ByteUtilsTest {

    @Test
    fun countLeft() {
        var size = 10

        val test1 = ByteArray(size) { 0.toByte() }
        assertEquals(size, test1.countLeft { it == 0.toByte() })

        var count = -1
        val test2 = ByteArray(size) {
            count++
            if (count < size/2) 0.toByte()
            else 1.toByte()
        }
        assertEquals(size/2, test2.countLeft { it == 0.toByte() })
        assertEquals(0, test2.countLeft { it == 1.toByte() })
    }
}