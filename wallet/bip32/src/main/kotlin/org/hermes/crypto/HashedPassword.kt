package org.hermes.crypto

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
