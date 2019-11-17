package org.hermes.bip32

import java.security.MessageDigest
import org.bouncycastle.util.encoders.Base64
import org.hermes.crypto.ECDSASignature
import org.hermes.crypto.SecP256K1PrivKey

object BTCSigner : BaseECDSASigner() {

    override fun sign(key: SecP256K1PrivKey, data: ByteArray): ByteArray {
        val signature = chainSign(
            key = key,
            prefix = "Bitcoin Signed Message:\n",
            data = data,
            hashRounds = 2,
            digest = MessageDigest.getInstance("SHA-256")
        )

        if (signature.vb == null) throw ECDSASignature.NoEvenBit()
        val completeArray = ByteArray(65)
        completeArray[0] = signature.vb
        System.arraycopy(signature.rb, 0, completeArray, 1, 32)
        System.arraycopy(signature.canonicalSb, 0, completeArray, 33, 32)
        return Base64.encode(completeArray)
    }
}
