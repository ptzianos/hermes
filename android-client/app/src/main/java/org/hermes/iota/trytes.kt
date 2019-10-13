package org.hermes.iota

import java.math.BigInteger
import java.security.SecureRandom
import java.util.*

import kotlin.collections.ArrayList
import kotlin.collections.HashMap

import org.bouncycastle.pqc.math.linearalgebra.IntegerFunctions.pow

import org.hermes.collections.ImmutableHashSet
import org.hermes.collections.OrderedImmutableHashSet

class InvalidTrit: Exception()
class InvalidTryte: Exception()
class OutOfTernaryBounds: Exception()


/**
 * General properties of the Balanced Trinary system used inside IOTA.
 */
object BalancedTernary {

    val BYTE_RADIX: IntArray = intArrayOf(1, 3, 9, 27, 81)

    const val UNSIGNED_BYTE_MAX_VAL: Int = 242

    const val TRYTE_ALPHABET = "9ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    const val TRYTE_SPACE = TRYTE_ALPHABET.length
    const val BYTE_SPACE = 243

    val TRYTE_CHAR_SET: ImmutableHashSet<Char>
    val TRYTES: OrderedImmutableHashSet<Tryte>

    const val TRYTE_VALUE_MAX = 13
    const val BYTE_VALUE_MAX = 121

    const val TRYTE_VALUE_MIN = -13
    const val BYTE_VALUE_MIN = -121

    // Number of trits in a tryte
    const val TRYTE_WIDTH = 3
    // Minimum number of bits to represent a tryte
    const val BYTE_WIDTH = 5

    private val byTrits: HashMap<Triple<Trit, Trit, Trit>, Tryte> = HashMap()
    private val byCharacter: HashMap<Char, Tryte> = HashMap()

    init {
        // Create an immutable set with all the different characters that can encode trytes
        val tryteCharHashSet = HashSet<Char>()
        for (c: Char in TRYTE_ALPHABET)
            tryteCharHashSet.add(c)
        TRYTE_CHAR_SET = ImmutableHashSet(tryteCharHashSet)

        // Create a mapping from integers in range -13..13 to tryte objects
        val orderedTrytes = ArrayList<Tryte>()
        val minimumTryte = Tryte(Trit(-1), Trit(-1), Trit(-1))
        orderedTrytes.add(minimumTryte)
        for (i: Int in -12..13)
            orderedTrytes.add(orderedTrytes.last().inc())
        TRYTES = OrderedImmutableHashSet(orderedTrytes)

        // Create a mapping from sequences of bytes to trytes. Not all possible bytes
        // are included in this mapping, just those that map to trytes.
        for (tryte in TRYTES) {
            byCharacter[tryte.char] = tryte
            byTrits[tryte.trits] = tryte
        }
    }

    /**
     * Return the tryte that directly maps to an integer from -13 to 13.
     * For conversion from arbitrary decimal values to balanced trinary, use the
     * `toTrinary` method.
     *
     * @throws OutOfTernaryBounds when the integer provider is outside -13 to 13.
     */
    fun tryteFromDecimal(i: Int): Tryte {
        if (i < -13 || i > 13) {
            throw OutOfTernaryBounds()
        }
        return TRYTES[i]
    }

    /**
     * Convert a decimal integer to the balanced ternary system.
     *
     * TODO: Add tests for this
     */
    fun toTrinary(i: Int): TritArray {
        val isNegative = i < 0
        var value = when {
            isNegative -> -1 * i
            else -> i
        }
        var intermediate = TritArray(0) { Trit(0) }
        var pow = 0
        while (value > 0) {
            // No, I don't mean LSD as in the drug but Least Significant Decimal.
            val lsd = value % 10
            value = (value / 10)
            intermediate += TRYTES[lsd + 13].toTritArray() * Tryte.ten.toTritArray().toPowerOf(pow)
            pow += 1
        }
        intermediate = intermediate.fill()
        return when {
            isNegative -> -intermediate
            else -> intermediate
        }
    }

    fun fromChar(char: Char): Tryte = byCharacter[char] ?: throw OutOfTernaryBounds()

    fun fromTrits(triple: Triple<Trit, Trit, Trit>): Tryte = byTrits[triple] ?: throw OutOfTernaryBounds()
}


/**
 * A class that represents a single trit of the balanced trinary system.
 */
class Trit(i: Int) {

    val i: Int

    init {
        if (!arrayOf(-1, 0, 1).contains(i)) {
            throw InvalidTrit()
        }
        this.i = i
    }

    /**
     * Adds this trit to the other trit and
     */
    fun plus(other: Trit, curry: Int = 0): Pair<Trit, Trit> {
        /**
         * Adds the values of two trits and returns the sum and the curry.
         *
         * @return a Pair where the first value is the curry and the
         *          second is the sum.
         */
        fun addTrits(a: Int, b: Int): Pair<Int, Int> {
            return when {
                a == 1 && b == 1 -> Pair(1, -1)
                a == -1 && b == -1 -> Pair(-1, 1)
                else -> Pair(0, a + b)
            }
        }
        val (curry1, sum1) = addTrits(other.i, this.i)
        val (curry2, sum2) = addTrits(sum1, curry)
        // There is no combination that can give a two digit curry, so we can safely ignore it.
        val (_, finalCurry) = addTrits(curry1, curry2)
        return Pair(Trit(finalCurry), Trit(sum2))
    }

    operator fun times(other: Trit): Trit = Trit(this.i * other.i)

    operator fun unaryMinus(): Trit = Trit(this.i * -1)

    override operator fun equals(other: Any?): Boolean {
        if (other is Trit) {
            return i == other.i
        }
        return false
    }

    override fun toString(): String = i.toString()
}


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
        return TritArray(size() + otherArray.size()) {
            i: Int ->
            when {
                i < size() -> tritArray[i]
                else -> otherArray.tritArray[i - size()]
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
        for (i: Int in 0 until Math.max(size(), otherArray.size())) {
            when{
                i >= size() -> zipped.add(Pair(Trit(0), otherArray.tritArray[i]))
                i >= otherArray.size() -> zipped.add(Pair(tritArray[i], Trit(0)))
                else -> zipped.add(Pair(tritArray[i], otherArray.tritArray[i]))
            }
        }
        return zipped
    }

    fun size(): Int = tritArray.size

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
        val intermediateSum = TritArray.zeroedArray(size() + otherArray.size() + 1)
        for (i: Int in 0 until otherArray.size()) {
            for (j: Int in 0 until size())
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
        if (size() < 3) {
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
        if (size() <= 3) {
            var normalized = tritArray.clone()
            while (size() < 3) {
                normalized = normalized.plus(Trit(0))
            }
            return TritArray(normalized)
        }
        var zerosEnd = firstNonZeroIndex()
        if (zerosEnd == -1) {
            zerosEnd = size() - 1
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


class Tryte {

    val char: Char
    val decimalValue: Int
    val trits: Triple<Trit, Trit, Trit>

    companion object {
        /**
         * Return a random tryte.
         */
        fun random(): Tryte {
            val randomLetter = SecureRandom().nextInt(BalancedTernary.TRYTE_ALPHABET.length)
            return Tryte(BalancedTernary.TRYTE_ALPHABET[randomLetter])
        }

        val zero: Tryte
            get() = Tryte(Trit(0), Trit(0), Trit(0))

        val one: Tryte
            get() = Tryte(Trit(1), Trit(0), Trit(0))

        val ten: Tryte
            get() = Tryte(Trit(1), Trit(0), Trit(1))
    }

    /**
     * Wrap the individual trits into one tryte.
     */
    constructor(first: Trit, second: Trit, third: Trit) {
        trits = Triple(first, second, third)
        decimalValue = first.i + 3 * second.i + 9 * third.i
        char = BalancedTernary.TRYTE_ALPHABET[decimalValue + 13]
    }

    /**
     * Constructs a Tryte out of a character.
     *
     * The character must belong to the balanced trinary alphabet.
     */
    constructor(char: Char) {
        val upperCaseChar: Char = if (char.isLowerCase()) char.toUpperCase() else char
        val charIndex = BalancedTernary.TRYTE_ALPHABET.indexOf(upperCaseChar)
        if (charIndex == -1) {
            throw InvalidTryte()
        }
        this.char = upperCaseChar
        decimalValue = charIndex - 13
        val tritArray = BalancedTernary.toTrinary(decimalValue)
        this.trits = Triple(tritArray[0], tritArray[1], tritArray[2])
    }

    /**
     * Constructs a Tryte out of an integer.
     *
     * The integer must be between -13 and 13.
     */
    constructor(i: Int) {
        if (i < -13 || i > 13) {
            throw InvalidTryte()
        }
        this.char = BalancedTernary.TRYTE_ALPHABET[i + 13]
        decimalValue = i
        val tritArray = BalancedTernary.toTrinary(decimalValue)
        this.trits = Triple(tritArray[0], tritArray[1], tritArray[2])
    }

    /**
     * Return this tryte as an array of trits.
     */
    fun toTritArray(): TritArray = TritArray(trits)

    operator fun unaryPlus(): Tryte = this

    operator fun unaryMinus(): Tryte {
        val negativeTritSequence: Triple<Trit, Trit, Trit> = Triple(
                Trit(-1 * trits.first.i),
                Trit(-1 * trits.second.i),
                Trit(-1 * trits.third.i)
        )
        return BalancedTernary.fromTrits(negativeTritSequence)
    }

    /**
     * Increment Tryte by one.
     *
     * If the Tryte overflows that's not something that will be handled
     * by this method.
     */
    operator fun inc(): Tryte = this + one

    /**
     * Adds b to this Tryte.
     *
     * It throws away the curry if there is an overflow.
     */
    operator fun plus(b: Tryte): Tryte {
        val newTritArray = toTritArray() + b.toTritArray()
        return Tryte(newTritArray[0], newTritArray[1], newTritArray[2])
    }

    operator fun times(b: Tryte): Tryte = (toTritArray() * b.toTritArray()).toTryte()

    /**
     * Raise the tryte to a power.
     *
     * The result can be of arbitrary length, so a TryteArray will be
     * returned instead of a simple Tryte.
     */
    fun toPowerOf(decimalPower: Int): TryteArray = toTritArray().toPowerOf(decimalPower).toTryteArray()

    override operator fun equals(other: Any?): Boolean =
        when (other is Tryte) {
            true -> trits.first == other.trits.first &&
                    trits.second == other.trits.second &&
                    trits.third == other.trits.third
            else -> false
        }
}


/**
 * A class representing an array of trytes.
 */
class TryteArray {

    private val trytes: Array<Tryte>

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
        trytes = Array(charArray.size) { i ->  Tryte(charArray[i]) }
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

    fun toByteArray(): ByteArray =
}