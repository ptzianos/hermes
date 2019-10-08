package org.hermes.iota

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test


class BalancedTrinaryTests {

    @Nested
    inner class Trytes {
        @Test
        fun `from char works`() {
            val tritArray = Tryte('A').asTritArray().trimToTryteSize()
            assertEquals(tritArray.size(), 3)
            assertEquals(tritArray[0].i, 0)
            assertEquals(tritArray[1].i, -1)
            assertEquals(tritArray[2].i, -1)
        }

        @Test
        fun `from lowercase char works`() {
            assertEquals(-12, Tryte('a').decimalValue)
        }

        @Test
        fun `from random char throws error`() {
            assertThrows(InvalidTryte::class.java) { Tryte('1'); }
        }

        @Test
        fun addition() {
            assertEquals(Tryte('A').decimalValue, -12)
            assertEquals(Tryte('Q').decimalValue, 4)
            assertEquals((Tryte('A') + Tryte('Q')).decimalValue, -8)
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
                if (elems < array1.size()) {
                    assertEquals(t.i, array1[elems].i)
                } else {
                    assertEquals(t.i, array2[elems - array1.size()].i)
                }
                elems++
            }
            assertEquals(elems, array1.size() + array2.size())
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
            assertEquals(elems, Math.max(array1.size(), array2.size()))
        }

        @Test
        fun powers() {
            assertEquals(
                100,
                Tryte.decimal10()
                    .toPowerOf(2)
                    .toDecimal())
        }

        @Test
        fun `convert bytes to tryte array`() {
//            "bla".forEach {
//                println(it.toByte())
//                println(it.toByte() / 27)
//                println(it.toByte() % 27)
//            }

            "bla".flatMap { listOf(
                BalancedTernary.TRYTE_ALPHABET[it.toByte() % 27],
                BalancedTernary.TRYTE_ALPHABET[it.toByte() / 27]
            ) }.forEach {
                println(it)
            }

            assertEquals(
                "QC9DPC",
                TryteArray("bla".map { it.toByte() }.toByteArray()).toString()
            )
        }
    }

}