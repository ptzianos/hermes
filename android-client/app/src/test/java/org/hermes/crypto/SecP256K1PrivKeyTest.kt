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
        val privateKey = SecP256K1PrivKey("57c617d9b4e1f7af6ec97ca2ff57e94a28279a7eedd4d12a99fa11170e94f5a4")
        // pubKey: 14903880589752143041982648392150622260876031794510409801157403400876048842930, 49005791221922006887990349039963577247542408960238324899212392709157895456489
        // pub hex: 0420f34c2786b4bae593e22596631b025f3ff46e200fc1d4b52ef49bbdc2ed00b26c584b7e32523fb01be2294a1f8a5eb0cf71a203cc034ced46ea92a8df16c6e9
        // pub points: 14903880589752143041982648392150622260876031794510409801157403400876048842930, 49005791221922006887990349039963577247542408960238324899212392709157895456489
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