package org.hermes.hd

import org.hermes.crypto.SHA256

/**
 * Base58 encoder based on:
 * @see{https://github.com/bitcoinj/bitcoinj/blob/master/core/src/main/java/org/bitcoinj/core/Base58.java}
 */
object Base58 {

    class InvalidFormat : Exception()
    class InvalidChecksum : Exception()

    private val ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray()
    private val INDICES = IntArray(128)
    private val zeroChar = ALPHABET[0]
    private const val zeroByte = 0.toByte()

    init {
        for (i in 0 until ALPHABET.size)
            INDICES[ALPHABET[i].toInt()] = i
    }

    /**
     * Encodes the given bytes as a base58 string (no checksum is appended).
     */
    fun toBase58String(input: ByteArray, appendChecksum: Boolean = false): String {
        if (input.isEmpty()) return ""

        // Convert base-256 digits to base-58 digits (plus conversion to ASCII characters)
        val inputClone = input.clone() +
                (if (appendChecksum) SHA256.hashTwice(input).sliceArray(0 until 4) else ByteArray(0))

        val encoded = CharArray(input.size * 2) // upper bound
        var outputStart = encoded.size

        // Count how many zeros there are in the beginning of the byte array
        var zeros = 0
        for (i in 0 until inputClone.size)
            if (inputClone[i] == zeroByte) zeros++
            else break

        var encodedZeros = 0
        for (i in zeros until inputClone.size)
            while (inputClone[i] != zeroByte) {
                encoded[--outputStart] = ALPHABET[divmod(inputClone, i, 256, 58)]
                if (encoded[outputStart] == zeroChar) encodedZeros++
                else encodedZeros = 0
            }

        (zeros downTo 1).forEach { _ -> encoded[--outputStart] = zeroChar }

        return String(encoded, outputStart, encoded.size - outputStart)
    }

    /**
     * Decodes the given base58 string into the original data bytes.
     *
     * @param input the base58-encoded string to decode
     * @return the decoded data bytes
     */
    @Throws(InvalidFormat::class)
    fun decode(input: String, verifyChecksum: Boolean = false): ByteArray {
        if (input.isEmpty())
            return ByteArray(0)

        // Convert the base58-encoded ASCII chars to a base58 byte sequence (base58 digits).
        val input58 = ByteArray(input.length)
        for (i in 0 until input.length)
            input58[i] = INDICES.getOrNull(input[i].toInt())?.toByte() ?: throw InvalidFormat()

        // Count leading zeros.
        var zeros = 0
        while (zeros < input58.size && input58[zeros] == zeroByte)
            ++zeros

        // Convert base-58 digits to base-256 digits.
        val decoded = ByteArray(input.length)
        var outputStart = decoded.size
        for (inputStart in zeros until input58.size) {
            if (input58[inputStart] == zeroByte) continue
            decoded[--outputStart] = divmod(input58, inputStart, 58, 256).toByte()
        }
        // Ignore extra leading zeroes that were added during the calculation.
        while (outputStart < decoded.size && decoded[outputStart] == zeroByte)
            ++outputStart

        if (verifyChecksum) {
            if (decoded.size < 4) throw InvalidFormat()
            val checksum = decoded.sliceArray(decoded.size - 4 until decoded.size)
            val expectedChecksum = SHA256
                .hashTwice(decoded.sliceArray(outputStart - zeros until decoded.size - 4))
                .sliceArray(0 until 4)
            if (!checksum.contentEquals(expectedChecksum)) throw InvalidChecksum()
            return decoded.sliceArray(outputStart - zeros until decoded.size - 4)
        }
        // Return decoded data (including original number of leading zeros).
        return decoded.sliceArray(outputStart - zeros until decoded.size)
    }

    /**
     * Divides a number, represented as an array of bytes each containing a single digit
     * in the specified base, by the given divisor. The given number is modified in-place
     * to contain the quotient, and the return value is the remainder.
     *
     * @param number the number to divide
     * @param firstDigit the index within the array of the first non-zero digit
     * (this is used for optimization by skipping the leading zeros)
     * @param base the base in which the number's digits are represented (up to 256)
     * @param divisor the number to divide by (up to 256)
     * @return the remainder of the division operation
     */
    private fun divmod(number: ByteArray, firstDigit: Int, base: Int, divisor: Int): Int {
        // this is just long division which accounts for the base of the input digits
        var remainder = 0
        for (i in firstDigit until number.size) {
            val digit = number[i].toInt() and 0xFF
            val temp = remainder * base + digit
            number[i] = (temp / divisor).toByte()
            remainder = temp % divisor
        }
        return remainder
    }
}
