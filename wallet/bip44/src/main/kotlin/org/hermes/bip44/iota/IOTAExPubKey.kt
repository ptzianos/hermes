package org.hermes.bip44.iota

import org.hermes.bip32.BIP32PubKey
import org.hermes.ternary.TryteArray

import org.iota.jota.pow.SpongeFactory
import org.iota.jota.utils.Checksum
import org.iota.jota.utils.Converter
import org.iota.jota.utils.Signing

class IOTAExPubKey(
    override val parent: BIP32PubKey?,
    override val chainCode: ByteArray,
    override val path: String,
    val value: TryteArray
) : BIP32PubKey {

    /**
     * The address without any checksum
     */
    val addressTrits: IntArray by lazy {
        val signing = Signing(SpongeFactory.create(SpongeFactory.Mode.KERL))
        signing.address(value.toTritIntArray())
    }

    /**
     * Returns an IOTA compatible address with the checksum appended to the trits
     */
    override val address: String by lazy { Checksum.addChecksum(Converter.trytes(addressTrits)) }

    override fun child(index: Long): BIP32PubKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getAlgorithm(): String = "Kerl"

    override fun getEncoded(): ByteArray = value.toByteArray()

    // TODO: fix this
    override fun getFormat(): String = "ASN.1"
}
