package org.hermes.hd

import org.hermes.iota.Seed
import org.hermes.utils.toTritArray

import org.iota.jota.pow.SpongeFactory
import org.iota.jota.utils.IotaAPIUtils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.runners.Parameterized

internal class IOTAExPrivKeyTest {

    companion object {

        val seed = Seed("FWLL9BRUODINUXS9CRBOXUXOFFRFDWERXNCPLCKAMBNEPDJXTPXILKNDJGEPKSOMOLEZFHSPYQVKF9TWP".toCharArray())

        @Parameterized.Parameters(name = "{index}: CKDPriv({0}) = {2}")
        @JvmStatic
        fun IOTAKeyGenTestVectors(): Collection<Array<Any>> =
            (0 until 1000 step 50).map { it ->
                arrayOf(it,
                    IotaAPIUtils.newAddress(
                        seed.toString(),
                        Seed.DEFAULT_SEED_SECURITY,
                        it,
                        true,
                        SpongeFactory.create(SpongeFactory.Mode.KERL))
                )
            }

    }

    @ParameterizedTest
    @MethodSource(value = [ "IOTAKeyGenTestVectors" ])
    fun CKDPriv(childIndex: Int, expectedAddress: String) {
        val privKeyVal = IOTAExPrivKey.PrivKey(seed.toIntArray(), childIndex.toLong())
        val privKey = IOTAExPrivKey(
            "m/${childIndex}}", null, ByteArray(0),
            privKeyVal.toTritArray().toTryteArray())
        assertEquals(expectedAddress, privKey.public.address)
    }
}
