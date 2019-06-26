package org.hermes.crypto

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


internal class SecP256K1PrivKeyTest {

    @Test
    fun testPrivateKeyImportAndWIF() {
        val privateKey = SecP256K1PrivKey("0C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D")
        assertEquals("0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d", privateKey.asHex())
        assertEquals("5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ", privateKey.asWIF())
        assertTrue(SecP256K1PrivKey.validateWIFCheckSum(privateKey.asWIF()))
    }

    @Test
    fun testPrivateKeyWith0xImportAndWIF() {
        val privateKey = SecP256K1PrivKey("0x0C28FCA386C7A227600B2FE50B7CAE11EC86D3BF1FBE471BE89827E19D72AA1D")
        assertEquals("0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d", privateKey.asHex())
        assertEquals("5HueCGU8rMjxEXxiPuD5BDku4MkFqeZyd4dZ1jvhTVqvbTLvyTJ", privateKey.asWIF())
        assertTrue(SecP256K1PrivKey.validateWIFCheckSum(privateKey.asWIF()))
    }

    @Test
    fun signWithPrivateKey() {
        assertEquals(
            "G0orznlzaawDMgoOYJHpeNc70XycJ6V45ktP0aoir1eXG8lIAQ6/44Evyh3jWeYxGtzaSIQ+pPNSzfAtHjU7ei0=",
            SecP256K1PrivKey("0x0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d").electrumSign("blaa")
        )
    }

    @Test
    fun producePublicKey() {
        // TODO: Implement this
        val privateKey = SecP256K1PrivKey("0x0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d")
        val publicKey = SecP256K1PubKey.fromPrivateKey(privateKey)
        assertEquals(
            "04d0de0aaeaefad02b8bdc8a01a1b8b11c696bd3d66a2c5f10780d95b7df42645cd85228a6fb29940e858e7e55842ae2bd115d1ed7cc0e82d934e929c97648cb0a",
            publicKey.encodedHex)
    }

    @Test
    fun getAlgorithm() {
        assertEquals(
            "EC",
            SecP256K1PrivKey("0x0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d").algorithm
        )
    }

    @Test
    fun getFormat() {
        assertEquals(
            "PKCS#8",
            SecP256K1PrivKey("0x0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d").format
        )
    }

    @Test
    fun getEncoded() {
        assertEquals(
            "-----BEGIN EC PRIVATE KEY-----\n" +
                    "MIIBCwIBAQQg2NVLqC0rX/S95yiRquuUmlVdK8ODNXpCwrTCW1I5zkmggeMwgeAC\n" +
                    "AQEwLAYHKoZIzj0BAQIhAP////////////////////////////////////7///wv\n" +
                    "MEQEIAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABCAAAAAAAAAAAAAA\n" +
                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAABwRBBHm+Zn753LusVaBilc6HCwcCm/zbLc4o\n" +
                    "2VnygVsW+BeYSDradyajxGVdpPv8DhEIqP0XtEimhVQZnEfQj/sQ1LgCIQD/////\n" +
                    "///////////////+uq7c5q9IoDu/0l6M0DZBQQIBAQ==\n" +
                    "-----END EC PRIVATE KEY-----\n",
            String(SecP256K1PrivKey("d8d54ba82d2b5ff4bde72891aaeb949a555d2bc383357a42c2b4c25b5239ce49").encoded!!)
        )
    }
}