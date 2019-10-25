package org.hermes.hd

import org.bouncycastle.util.encoders.Hex
import org.hermes.utils.toBigInt
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*

internal class ETHKeyEncoderTest {

    @Test
    fun encodePrivateKey() {
        assertEquals(
            "c2c72dfbff11dfb4e9d5b0a20c620c58b15bb7552753601f043db91331b0db15",
            ExPrivKey(
                "m",
                null,
                ByteArray(0),
                Hex.decode("c2c72dfbff11dfb4e9d5b0a20c620c58b15bb7552753601f043db91331b0db15").toBigInt(true),
                ETHKeyEncoder)
                .toString()
        )

        assertEquals(
            "92087b6ff4c9d5790f16e4563191803e24d43bf1b378edbf3d9d951467e5c528",
            ExPrivKey(
                "m",
                null,
                ByteArray(0),
                Hex.decode("92087b6ff4c9d5790f16e4563191803e24d43bf1b378edbf3d9d951467e5c528").toBigInt(true),
                ETHKeyEncoder)
                .toString()
        )
    }

    @Test
    fun encodePublicKey() {
        val exPrivKey1 = ExPrivKey(
            "m",
            null,
            ByteArray(0),
            Hex.decode("c2c72dfbff11dfb4e9d5b0a20c620c58b15bb7552753601f043db91331b0db15").toBigInt(true),
            ETHKeyEncoder)
        assertEquals(
            "a225bf565ff4ea039bccba3e26456e910cd74e4616d67ee0a166e26da6e5e55a08d0fa1659b4b547ba7139ca531f62907b9c2e72b80712f1c81ece43c33f4b8b",
            exPrivKey1.public.toString()
        )

        val exPrivKey2 = ExPrivKey(
            "m",
            null,
            ByteArray(0),
            Hex.decode("92087b6ff4c9d5790f16e4563191803e24d43bf1b378edbf3d9d951467e5c528").toBigInt(true),
            ETHKeyEncoder)
        assertEquals(
            "c36a9f355c4e5ca3e702831eb020f8eb328c1273f9b4b60e258af4bbb29a082bb6614875962afdf8c690ee23d79501fed67f5f05a7b3f5c157cf2f900b7860b3",
            exPrivKey2.public.toString()
        )
    }

    @Test
    fun address() {
        val exPrivKey1 = ExPrivKey(
            "m",
            null,
            ByteArray(0),
            Hex.decode("c2c72dfbff11dfb4e9d5b0a20c620c58b15bb7552753601f043db91331b0db15").toBigInt(true),
            ETHKeyEncoder)
        assertEquals(
            "6ea27154616a29708dce7650b475dd6b82eba6a3",
            exPrivKey1.public.address
        )

        val exPrivKey2 = ExPrivKey(
            "m",
            null,
            ByteArray(0),
            Hex.decode("92087b6ff4c9d5790f16e4563191803e24d43bf1b378edbf3d9d951467e5c528").toBigInt(true),
            ETHKeyEncoder)
        assertEquals(
            "76a2c09ec796070d46100a9283e81fa237e73e64",
            exPrivKey2.public.address
        )
    }
}