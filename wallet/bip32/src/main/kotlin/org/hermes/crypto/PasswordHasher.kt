package org.hermes.crypto

import java.lang.Exception
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHasher {

    class WrongHashFormatException : Exception()

    /**
     * Unpacks the components of a hash from the string and returns them.
     */
    fun unPackHash(hashLike: String): HashedPassword {
        val subStrings = hashLike.split("\$")
        if (subStrings.size != 4) {
            throw WrongHashFormatException()
        }
        return HashedPassword(
            subStrings[0],
            subStrings[1].toInt(),
            subStrings[2].toByteArray(),
            subStrings[3].toByteArray()
        )
    }

    fun hashPassword(
        password: CharArray,
        salt: ByteArray = PasswordHashingParameters.Salt,
        iterations: Int = PasswordHashingParameters.Iterations
    ): HashedPassword {
        val passwordBasedEncryptionSpec = PBEKeySpec(
                password,
                salt,
                iterations,
                PasswordHashingParameters.KeySize)
        val secretKeyFactory = SecretKeyFactory.getInstance(CryptoAlgorithms.PBKDF2_HMC_SHA1)
        val hash = secretKeyFactory.generateSecret(passwordBasedEncryptionSpec).encoded
        return HashedPassword(hash = hash)
    }

    fun bytesToHex(byteArray: ByteArray): String {
        return byteArray
                .map { b: Byte -> String.format("%02X", b) }
                .fold("") { acc, i -> acc + i }
    }
}
