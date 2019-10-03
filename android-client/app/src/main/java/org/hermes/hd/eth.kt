package org.hermes.hd

import org.bouncycastle.util.encoders.Hex

import org.hermes.crypto.NoEvenBit
import org.hermes.crypto.Keccak
import org.hermes.crypto.SecP256K1PrivKey


object ETHKeyEncoder: KeyEncoder<ExPrivKey, ExPubKey>() {
    override fun encodePrivateKey(key: ExPrivKey, options: Map<String, Any>): String =
        // Drop the 0x00 byte at the beginning
        Hex.toHexString(key.value.toByteArray().drop(1).toByteArray())

    override fun encodePublicKey(key: ExPubKey, options: Map<String, Any>): String =
        // Drop the 0x04 byte at the beginning
        Hex.toHexString(key.bcPoint.getEncoded(false).drop(1).toByteArray())

    override fun address(key: ExPubKey, options: Map<String, Any>): String =
        Hex.toHexString(Keccak.hash(key.bcPoint.getEncoded(false).drop(1).toByteArray()))
            .drop(24)
}

object ETHSigner: BaseECDSASigner() {
    override fun sign(key: SecP256K1PrivKey, data: ByteArray): ByteArray {
        val signature = chainSign(
            key = key,
            prefix = "Ethereum Signed Message:\n",
            data = data,
            digest = org.bouncycastle.jcajce.provider.digest.Keccak.Digest256()
        )

        if (signature.vb == null) throw NoEvenBit()
        val completeArray = ByteArray(65)
        System.arraycopy(signature.rb, 0, completeArray, 0, 32)
        System.arraycopy(signature.sb, 0, completeArray, 32, 32)
        completeArray[64] = signature.vb
        return completeArray
    }
}
