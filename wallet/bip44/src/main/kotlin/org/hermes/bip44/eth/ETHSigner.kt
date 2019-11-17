package org.hermes.bip44.eth

import org.hermes.bip32.BaseECDSASigner
import org.hermes.crypto.ECDSASignature
import org.hermes.crypto.SecP256K1PrivKey

object ETHSigner : BaseECDSASigner() {
    override fun sign(key: SecP256K1PrivKey, data: ByteArray): ByteArray {
        val signature = chainSign(
            key = key,
            prefix = "Ethereum Signed Message:\n",
            data = data,
            digest = org.bouncycastle.jcajce.provider.digest.Keccak.Digest256()
        )

        val completeArray = ByteArray(65)
        completeArray[64] = signature.vb ?: throw ECDSASignature.NoEvenBit()
        System.arraycopy(signature.rb, 0, completeArray, 0, 32)
        System.arraycopy(signature.sb, 0, completeArray, 32, 32)
        return completeArray
    }
}
