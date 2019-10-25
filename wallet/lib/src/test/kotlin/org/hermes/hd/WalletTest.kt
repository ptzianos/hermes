package org.hermes.hd

import org.bouncycastle.util.encoders.Hex

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

internal class WalletTest {

    @Test
    fun key() {
        val wallet = Wallet(Hex.decode("000102030405060708090a0b0c0d0e0f"))
        assertEquals(
            "xprv9s21ZrQH143K3QTDL4LXw2F7HEK3wJUD2nW2nRk4stbPy6cq3jPPqjiChkVvvNKmPGJxWUtg6LnF5kejMRNNU3TGtRBeJgk33yuGBxrMPHi",
            wallet["m"].toString()
        )
        assertEquals(
            "xpub661MyMwAqRbcFtXgS5sYJABqqG9YLmC4Q1Rdap9gSE8NqtwybGhePY2gZ29ESFjqJoCu1Rupje8YtGqsefD265TMg7usUDFdp6W1EGMcet8",
            wallet["m"].public.toString()
        )
        assertEquals(
            "15mKKb2eos1hWa6tisdPwwDC1a5J1y9nma",
            wallet["m"].public.address
        )
        ////////////////////////////////////////////////////////////////////////////////
        assertEquals(
            "xprv9uHRZZhk6KAJC1avXpDAp4MDc3sQKNxDiPvvkX8Br5ngLNv1TxvUxt4cV1rGL5hj6KCesnDYUhd7oWgT11eZG7XnxHrnYeSvkzY7d2bhkJ7",
            wallet["m/0H"].toString()
        )
        assertEquals(
            "xpub68Gmy5EdvgibQVfPdqkBBCHxA5htiqg55crXYuXoQRKfDBFA1WEjWgP6LHhwBZeNK1VTsfTFUHCdrfp1bgwQ9xv5ski8PX9rL2dZXvgGDnw",
            wallet["m/0H"].public.toString()
        )
        assertEquals(
            "19Q2WoS5hSS6T8GjhK8KZLMgmWaq4neXrh",
            wallet["m/0H"].public.address
        )
        ////////////////////////////////////////////////////////////////////////////////
        assertEquals(
            "xprv9wTYmMFdV23N2TdNG573QoEsfRrWKQgWeibmLntzniatZvR9BmLnvSxqu53Kw1UmYPxLgboyZQaXwTCg8MSY3H2EU4pWcQDnRnrVA1xe8fs",
            wallet["m/0H/1"].toString()
        )
        assertEquals(
            "xpub6ASuArnXKPbfEwhqN6e3mwBcDTgzisQN1wXN9BJcM47sSikHjJf3UFHKkNAWbWMiGj7Wf5uMash7SyYq527Hqck2AxYysAA7xmALppuCkwQ",
            wallet["m/0H/1"].public.toString()
        )
        assertEquals(
            "1JQheacLPdM5ySCkrZkV66G2ApAXe1mqLj",
            wallet["m/0H/1"].public.address
        )
        ////////////////////////////////////////////////////////////////////////////////
        assertEquals(
            "xprv9z4pot5VBttmtdRTWfWQmoH1taj2axGVzFqSb8C9xaxKymcFzXBDptWmT7FwuEzG3ryjH4ktypQSAewRiNMjANTtpgP4mLTj34bhnZX7UiM",
            wallet["m/0H/1/2H"].toString()
        )
        assertEquals(
            "xpub6D4BDPcP2GT577Vvch3R8wDkScZWzQzMMUm3PWbmWvVJrZwQY4VUNgqFJPMM3No2dFDFGTsxxpG5uJh7n7epu4trkrX7x7DogT5Uv6fcLW5",
            wallet["m/0H/1/2H"].public.toString()
        )
        assertEquals(
            "1NjxqbA9aZWnh17q1UW3rB4EPu79wDXj7x",
            wallet["m/0H/1/2H"].public.address
        )
        ////////////////////////////////////////////////////////////////////////////////
        assertEquals(
            "xprvA2JDeKCSNNZky6uBCviVfJSKyQ1mDYahRjijr5idH2WwLsEd4Hsb2Tyh8RfQMuPh7f7RtyzTtdrbdqqsunu5Mm3wDvUAKRHSC34sJ7in334",
            wallet["m/0H/1/2H/2"].toString()
        )
        assertEquals(
            "xpub6FHa3pjLCk84BayeJxFW2SP4XRrFd1JYnxeLeU8EqN3vDfZmbqBqaGJAyiLjTAwm6ZLRQUMv1ZACTj37sR62cfN7fe5JnJ7dh8zL4fiyLHV",
            wallet["m/0H/1/2H/2"].public.toString()
        )
        assertEquals(
            "1LjmJcdPnDHhNTUgrWyhLGnRDKxQjoxAgt",
            wallet["m/0H/1/2H/2"].public.address
        )
        ////////////////////////////////////////////////////////////////////////////////
        assertEquals(
            "xprvA41z7zogVVwxVSgdKUHDy1SKmdb533PjDz7J6N6mV6uS3ze1ai8FHa8kmHScGpWmj4WggLyQjgPie1rFSruoUihUZREPSL39UNdE3BBDu76",
            wallet["m/0H/1/2H/2/1000000000"].toString()
        )
        assertEquals(
            "xpub6H1LXWLaKsWFhvm6RVpEL9P4KfRZSW7abD2ttkWP3SSQvnyA8FSVqNTEcYFgJS2UaFcxupHiYkro49S8yGasTvXEYBVPamhGW6cFJodrTHy",
            wallet["m/0H/1/2H/2/1000000000"].public.toString()
        )
        assertEquals(
            "1LZiqrop2HGR4qrH1ULZPyBpU6AUP49Uam",
            wallet["m/0H/1/2H/2/1000000000"].public.address
        )
    }
}