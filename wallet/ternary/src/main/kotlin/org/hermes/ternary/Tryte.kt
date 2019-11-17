package org.hermes.ternary

import java.security.SecureRandom

class InvalidTryte : Exception()

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
