package org.hermes.hd

import org.bouncycastle.util.Strings

import org.hermes.crypto.SecP256K1PrivKey

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class BTCSignerTest {

    fun sign(keyHex: String, msg: String): String = Strings.fromByteArray(
        BTCSigner.sign(SecP256K1PrivKey(keyHex), msg.toByteArray())
    )

    @Test
    fun sign() {
          assertEquals(
            "G0orznlzaawDMgoOYJHpeNc70XycJ6V45ktP0aoir1eXG8lIAQ6/44Evyh3jWeYxGtzaSIQ+pPNSzfAtHjU7ei0=",
            this@BTCSignerTest.sign("0x0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d",
                                    "blaa")
        )
    }

    @Test
    fun signLegalText() {
        assertEquals(
            "HFzue2eFvZYqIGEo1hvQDuk7flI/a6xQ5H5YvOCq/07pYFFyUjCJcrSFUXU79uLw6Vxt3i+Qcotyi7L3UAY2bKc=",
            this@BTCSignerTest.sign("0x0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d",
                """
By signing this with your private key you verify that you are the sole rightful owner of the key pair whose
public key has a SHA3-512 digest that starts with aaaaaaaaaa.

Expires on: 2019-06-27 10:36:07.726093
""")
        )
    }

    @Test
    fun signLongLegalText() {
        assertEquals(
            "GyZWBvo2roM7eKXOTNz0n3keGGQ10isWKouWe1mhaXTJIukPe5d+MuTUTD05bP4NR4f6txCCb1XpF/7RKYQKtII=",
            this@BTCSignerTest.sign("0x0c28fca386c7a227600b2fe50b7cae11ec86d3bf1fbe471be89827e19d72aa1d",
                """
By signing this with your private key you verify that you are the sole rightful owner of the key pair whose
public key has a SHA3-512 digest that starts with aaaaaaaaaaaaaaaaaaaaaabbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb
ccccccccccccccccccccccccccccccccccccddddddddddddddddddddddddddddddddddddddddddddddddddddeeeeeeeeeeeeeeeeeee
fffffffffffffffffffffffffffffffffffffffffffgggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggggg
hhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhhiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii
jjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjjkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkkk
llllllllllllllllllllllllllllllllllllllllllllllmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmmm
nnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnnooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo
.

Expires on: 2019-06-27 10:36:07.726093
""")
        )
    }
}