package org.hermes.ternary


class InvalidTrit: Exception()

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

    override fun toString(): String = when(i) {
        -1 -> "T"
        else -> i.toString()
    }
}
