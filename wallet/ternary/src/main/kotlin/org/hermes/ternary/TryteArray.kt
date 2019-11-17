package org.hermes.ternary

import java.math.BigInteger
import java.util.LinkedList
import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions.pow
import org.hermes.extensions.toByteArray
import org.iota.jota.utils.TrytesConverter

/**
 * A class representing an array of trytes.
 */
class TryteArray {

    private val trytes: Array<Tryte>

    val size: Int
        get() = trytes.size

    companion object {
        /**
         * Return an array of random trytes.
         */
        fun random(size: Int): TryteArray {
            return TryteArray(*(1..size)
                .map { Tryte.random() }
                .toTypedArray()
            )
        }
    }

    /**
     * Constructs a TryteArray from the characters in the character array.
     *
     * Every character must belong to the balanced trinary alphabet.
     */
    constructor(charArray: CharArray) {
        trytes = Array(charArray.size) { i -> Tryte(charArray[i]) }
    }

    constructor(byteArray: ByteArray) {
        val temp = LinkedList<Tryte>()
        val fourTeen = BigInteger("14")
        var b = BigInteger(byteArray)
        while (b != BigInteger.ZERO) {
            val dividerAndRemainder = b.divideAndRemainder(fourTeen)
            temp.add(Tryte(dividerAndRemainder[1].toInt()))
            b = dividerAndRemainder[0]
        }
        trytes = temp.toTypedArray()
    }

    constructor(vararg trytes: Tryte) {
        this.trytes = Array(trytes.size) { i -> trytes[i] }
    }

    fun toCharArray(): CharArray = trytes
        .map { it.char }
        .toCharArray()

    fun toTritArray(): TritArray = trytes
        .map { tryte -> tryte.toTritArray() }
        .reduce { tritArray1, tritArray2 -> tritArray1.conc(tritArray2) }

    fun sliceArray(indices: IntRange): TryteArray = TryteArray(*trytes.sliceArray(indices))

    override fun toString(): String = toCharArray().joinToString("")

    fun plus(tryte: Tryte): TryteArray = TryteArray(*trytes.clone().plus(tryte))

    operator fun plus(otherArray: TryteArray): TryteArray =
        (this.toTritArray() + otherArray.toTritArray()).toTryteArray()

    /**
     * Convert the tryte array into an integer.
     *
     * Handle with care, because the conversion process could cause an overflow.
     */
    fun toDecimal(): Int = trytes
        .mapIndexed { i, tryte -> pow(14, i) * tryte.decimalValue }
        .reduce { acc, i -> acc + i }

    /**
     * Converts the tryte array into a BigInteger.
     */
    fun toBigDecimal(): BigInteger = trytes
        .mapIndexed { i, tryte -> (pow(14L, i) * tryte.decimalValue).toBigInteger() }
        .reduce { acc, i -> acc + i }

    /**
     * Returns this as an array of integers with trits
     */
    fun toTritIntArray(): IntArray = trytes
        .flatMap { listOf(it.trits.first.i, it.trits.second.i, it.trits.third.i) }
        .toIntArray()

    fun toByteArray(): ByteArray = toBigDecimal().toByteArray()
}

fun String.toTrytes(): String = TrytesConverter.asciiToTrytes(this)

fun String.stripPaddingOfTrytes(): String = this.trimEnd('9')

fun Long.toTryteArray(): TryteArray = TryteArray(this.toByteArray())

fun Int.toTryteArray(): TryteArray = TryteArray(this.toByteArray())

fun BigInteger.toTryteArray(): TryteArray = TryteArray(this.toByteArray())

fun ByteArray.toTryteArray(): TryteArray = TryteArray(this)
