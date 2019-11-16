package org.hermes.ternary

import java.util.*

import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions.pow


/**
 * A class that represents a sequence of trits. It does not necessarily mean that the trits
 * contained can be neatly split into trytes.
 *
 * It is the underlying class that is used when performing any mathematical operations using Trytes.
 */
class TritArray {

    private val tritArray: Array<Trit>

    companion object {
        fun empty(): TritArray = TritArray(emptyArray())

        fun emptyArray(): Array<Trit> = Array(0) { Trit(0) }

        fun zeroedArray(i: Int = 0): TritArray = TritArray(i) { Trit(0) }
    }

    constructor(size: Int) {
        tritArray = Array(size) { Trit(0) }
    }

    constructor(size: Int, f: (Int) -> Trit) {
        tritArray = Array(size, f)
    }

    constructor(tritArray: Array<Trit>) {
        this.tritArray = tritArray
    }

    constructor(trits: Triple<Trit, Trit, Trit>) {
        tritArray = emptyArray()
            .plus(trits.first)
            .plus(trits.second)
            .plus(trits.third)
    }

    fun conc(otherArray: TritArray): TritArray {
        return TritArray(size + otherArray.size) {
                i: Int ->
            when {
                i < size -> tritArray[i]
                else -> otherArray.tritArray[i - size]
            }
        }
    }

    /**
     * Combines two arrays into a list of pairs of trits.
     *
     * If one of the lists has more elements, the missing trits will be
     * replaced with 0s.
     */
    fun zip(otherArray: TritArray): List<Pair<Trit, Trit>> {
        val zipped = LinkedList<Pair<Trit, Trit>>()
        for (i: Int in 0 until Math.max(size, otherArray.size)) {
            when{
                i >= size -> zipped.add(Pair(Trit(0), otherArray.tritArray[i]))
                i >= otherArray.size -> zipped.add(Pair(tritArray[i], Trit(0)))
                else -> zipped.add(Pair(tritArray[i], otherArray.tritArray[i]))
            }
        }
        return zipped
    }

    val size: Int
        get() = tritArray.size

    operator fun get(i: Int): Trit = tritArray[i]

    operator fun set(i: Int, t: Trit) = tritArray.set(i, t)

    operator fun plus(otherArray: TritArray): TritArray {
        var curry = Trit(0)
        var resultArray: Array<Trit> = emptyArray()
        for ((trit1, trit2) in zip(otherArray)) {
            // Add the two trits
            val sumRes = trit1.plus(trit2)
            // Add the curry to the result of the previous sum
            val res = sumRes.second.plus(curry)
            // Keep the curry for the next sum
            curry = sumRes.first.plus(res.first).second
            resultArray = resultArray.plus(res.second)
        }
        resultArray = resultArray.plus(curry)
        return TritArray(resultArray)
    }

    operator fun iterator(): Iterator<Trit> = tritArray.iterator()

    operator fun times(otherArray: TritArray): TritArray {
        var sum = TritArray.empty()
        val intermediateSum = TritArray.zeroedArray(size + otherArray.size + 1)
        for (i: Int in 0 until otherArray.size) {
            for (j: Int in 0 until size)
                intermediateSum[i + j] = this[j] * otherArray[i]
            sum += intermediateSum
            intermediateSum.makeZero()
        }
        return sum.trim()
    }

    operator fun unaryMinus(): TritArray = TritArray(tritArray.map { trit -> -trit }.toTypedArray())

    /**
     * Return the index of the first trit that is not zero.
     *
     * If the array is empty return -1.
     */
    fun firstNonZeroIndex(): Int {
        for (i: Int in (tritArray.size - 1) downTo 0) {
            if (tritArray[i].i != 0) {
                return i
            }
        }
        return -1
    }

    /**
     * Remove all the zeros from the most significant trits.
     */
    fun trim(): TritArray {
        val zerosEnd = firstNonZeroIndex()
        if (zerosEnd != -1)
            return TritArray(tritArray.sliceArray(0..zerosEnd))
        return this
    }

    /**
     * If length is less than 3 add a couple of 0's
     */
    fun fill(): TritArray {
        if (size < 3) {
            var newTritArray = this.tritArray.clone()
            while (newTritArray.size < 3) {
                newTritArray = newTritArray.plus(Trit(0))
            }
            return TritArray(newTritArray)
        }
        return this
    }

    /**
     * Trim the trit array to a size that can be easily converted into trytes.
     *
     * Each trytes has three trits. If the array has a size large than three
     * but is not an exact multiple of three it will be trimmed. The trimming
     * will start with the zeros and if it's still not in the correct size it
     * will be further trimmed. If the array is less than three trits long then
     * zeros will be added to reach the size of at least one tryte.
     */
    fun trimToTryteSize(): TritArray {
        if (size <= 3) {
            var normalized = tritArray.clone()
            while (size < 3) {
                normalized = normalized.plus(Trit(0))
            }
            return TritArray(normalized)
        }
        var zerosEnd = firstNonZeroIndex()
        if (zerosEnd == -1) {
            zerosEnd = size - 1
        }
        return if (zerosEnd % 3 == 0) {
            this
        } else {
            zerosEnd -= (zerosEnd % 3)
            TritArray(tritArray.sliceArray(0..zerosEnd))
        }
    }

    fun toArray(): Array<Trit> = tritArray.clone()

    /**
     * Raise the trit array to the power.
     *
     * The power is in decimal.
     */
    fun toPowerOf(decimalPower: Int): TritArray =
        when (decimalPower) {
            0 -> TritArray(1) { Trit(1) }
            1 -> this
            else -> {
                var res = this
                for (i in 2..decimalPower)
                    res *= res
                res
            }
        }

    /**
     * Returns this array as a Tryte.
     *
     * If the array contains more than three trits, they are ignored and
     * no error or exception is thrown.
     */
    fun toTryte(): Tryte = Tryte(safeGet(0), safeGet(1), safeGet(2))

    /**
     * Returns the i-th element or 0 if i exceeds the size of the array
     */
    fun safeGet(i: Int): Trit =
        if (tritArray.size > i) tritArray[i]
        else Trit(0)

    /**
     * Returns this array as an array of trytes.
     *
     * If the size of the array is not a multiple of
     */
    fun toTryteArray(): TryteArray {
        val tryteArraySize = tritArray.size / 3 + when {
            tritArray.size % 3 == 0 -> 0
            else -> 1
        }
        val array: Array<Tryte> = Array(tryteArraySize) { Tryte.zero }
        for (i in 0 until tryteArraySize) {
            array[i] = Tryte(
                safeGet(i*3),
                safeGet(i*3 + 1),
                safeGet(i*3 + 2)
            )
        }
        return TryteArray(*array)
    }

    /**
     * Zero out all the elements of the trit array
     */
    fun makeZero() = tritArray.fill(Trit(0), 0)

    /**
     * Returns the trits as an array of ints (-1, 0, 1) in little endian.
     */
    fun toTritIntArray(): IntArray = tritArray.map { it.i }.toIntArray()

    fun toDecimal(): Int = tritArray
        .mapIndexed { i, trit -> pow(3, i) * trit.i }
        .reduce { acc, i -> acc + i }

    override fun toString(): String = tritArray.joinToString("")
}

fun IntArray.toTritArray(): TritArray = TritArray(this.size) { Trit(this[it]) }
