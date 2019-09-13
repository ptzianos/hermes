package org.hermes.crypto

import org.bouncycastle.jcajce.provider.digest.RIPEMD160
import java.lang.Exception
import java.security.MessageDigest
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordHashingParameters {
    var Iterations: Int = 1000
    var Salt: ByteArray = "fjggne0rg2sr24dlgsouug".toByteArray()
    var KeySize: Int = 64 * 8
}

data class HashedPassword(val hashingAlgorithm: String = CryptoAlgorithms.PBKDF2_HMC_SHA1 ,
                          val iterations: Int = PasswordHashingParameters.Iterations,
                          val salt: ByteArray = PasswordHashingParameters.Salt,
                          val hash: ByteArray) {
    override fun toString(): String {
        return hashingAlgorithm + "\$$iterations" + "\$${PasswordHasher.bytesToHex(salt)}" +
                "\$${PasswordHasher.bytesToHex(hash)}"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is HashedPassword) {
            return false
        }
        return hashingAlgorithm == other.hashingAlgorithm && iterations == other.iterations &&
                salt.toString() == other.salt.toString() && hash.toString() == other.hash.toString()
    }

    override fun hashCode(): Int {
        return toString().hashCode()
    }
}

object PasswordHasher {

    class WrongHashFormatException: Exception()

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

    fun hashPassword(password: CharArray,
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

open class Hasher(val impl: MessageDigest) {

    fun hash(b: ByteArray): ByteArray {
        return impl.digest(b)
    }

    fun hashTwice(b: ByteArray): ByteArray {
        return hash(hash(b))
    }
}

object SHA256: Hasher(MessageDigest.getInstance("SHA-256"))

object SHA512: Hasher(MessageDigest.getInstance("SHA-512"))

object RIPEMD: Hasher(RIPEMD160.Digest())