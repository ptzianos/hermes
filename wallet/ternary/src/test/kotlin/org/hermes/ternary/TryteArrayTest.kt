package org.hermes.ternary

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import java.math.BigInteger

internal class TryteArrayTest {

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