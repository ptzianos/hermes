package org.hermes.iota

import java.math.BigInteger

import org.hermes.utils.toTryteArray

import org.iota.jota.utils.Converter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class BalancedTrinaryTests {

    @Nested
    inner class Trytes {
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

    @Nested
    inner class TritArrays {
        @Test
        fun `tryte array to int array`() {
            assertEquals(12.toByte(), Converter.bytes(intArrayOf(0, 1, 1))[0])
            assertArrayEquals(intArrayOf(0, 1, 1), 12.toTryteArray().toTritArray().toTritIntArray())
        }
    }

    @Nested
    inner class TryteArrays {
        @Test
        fun conc() {
            val array1 = TritArray(Triple(Trit(-1), Trit(0), Trit(1)))
            val array2 = TritArray(Triple(Trit(-1), Trit(0), Trit(1)))
            var elems = 0
            for (t in array1.conc(array2)) {
                if (elems < array1.size) {
                    assertEquals(t.i, array1[elems].i)
                } else {
                    assertEquals(t.i, array2[elems - array1.size].i)
                }
                elems++
            }
            assertEquals(elems, array1.size + array2.size)
        }

        @Test
        fun `zip same arrays`() {
            val array1 = TritArray(Triple(Trit(-1), Trit(0), Trit(1)))
            val array2 = TritArray(Triple(Trit(-1), Trit(0), Trit(1)))
            var elems = 0
            for ((t1, t2) in array1.zip(array2)) {
                assertEquals(t1.i, array1[elems].i)
                assertEquals(t1.i, t2.i)
                elems++
            }
            assertEquals(elems, Math.max(array1.size, array2.size))
        }

        @Test
        fun powers() {
            assertEquals(
                100,
                Tryte.ten
                    .toTritArray()
                    .toPowerOf(2)
                    .toDecimal())
        }

        @Test
        fun `convert byte array to tryte array`() {
            assertEquals(200, BigInteger("200").toTryteArray().toDecimal())
            assertEquals(209, BigInteger("209").toTryteArray().toDecimal())
            assertEquals(-209, BigInteger("-209").toTryteArray().toDecimal())
            assertEquals(
                "ZMPTWNVOYSUPQVRSORMOYPUZS",
                "Hello World!".toByteArray().toTryteArray().toString()
            )
            assertEquals(
                "XTVYZX",
                "bla".toByteArray().toTryteArray().toString()
            )
        }
    }

}