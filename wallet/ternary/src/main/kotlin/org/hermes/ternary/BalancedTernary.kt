package org.hermes.ternary

import org.hermes.collections.ImmutableHashSet
import org.hermes.collections.OrderedImmutableHashSet

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
