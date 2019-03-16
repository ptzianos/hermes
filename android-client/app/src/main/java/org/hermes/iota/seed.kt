package org.hermes.iota

class InvalidSeedException: Exception()

/**
 * Re-implementation of some cryptographic utilities used by the IOTA
 * Trinity wallet for producing a seed.
 *
 * @see https://medium.com/@abmushi/iota-signature-and-validation-b95b3f9ec534
 *
 * Based on the article mentioned above, the seed is 81 trytes long.
 * It is combined with another value and then hashed 27 * security_level
 * to produce a private key
 */

class Seed(val value: CharArray) {

    companion object {

        const val DEFAULT_SEED_SECURITY = 3

        const val MAX_SEED_LENGTH = 81

        const val ADDRESS_LENGTH = 90

        const val VALID_SEED_REGEX = "/^[A-Z9]+$/"

        fun new(): Seed {
            val charArray = CharArray(MAX_SEED_LENGTH)

            for (i: Int in 0 until MAX_SEED_LENGTH) {
                charArray[i] = Tryte.random().char
            }
            return Seed(charArray)
        }
    }

    fun validate() {
        if (value.size != MAX_SEED_LENGTH) throw InvalidSeedException()
        for (c: Char in value) {
            try { Tryte(c) }
            catch (t: InvalidTryte) { throw InvalidSeedException() }
        }
    }

    fun extractPrivateKey(): CharArray {
        return CharArray(0) { 'C' }
    }
}
