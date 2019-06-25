package org.hermes

import android.app.Application
import android.content.SharedPreferences
import android.security.keystore.KeyProperties
import android.util.Log
import java.security.*
import java.security.interfaces.ECPrivateKey
import java.util.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.crypto.digests.SHA1Digest
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.signers.ECDSASigner
import org.bouncycastle.crypto.signers.HMacDSAKCalculator
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.util.encoders.Base64
import org.bouncycastle.util.encoders.Hex

import org.hermes.crypto.PasswordHasher
import org.hermes.crypto.Secp256K1Curve
import org.hermes.iota.Seed

@Singleton
class CryptoRepository @Inject constructor(val application: Application,
                                           val db: HermesRoomDatabase,
                                           @param:Named("auth") val sharedPref: SharedPreferences,
                                           private val ks: KeyStore
) {

    private val loggingTag = "CryptoRepository"

    private lateinit var seed: Seed
    private lateinit var keypair: KeyPair

    private var unsealed: Boolean = false
    private var credentialsLoaded: Boolean = false

    fun sealed(): Boolean = !unsealed

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    /**
     * Returns true if the PIN matches the stored hash, false otherwise.
     * It does not check if there is a stored hash to compare against. The caller of the function
     * must perform these checks.
     */
    fun unseal(pin: String): Boolean {
        val hashedPin = PasswordHasher.hashPassword(pin.toCharArray())
        val storedPin = sharedPref.getString(application.getString(R.string.auth_hashed_pin), "")
        if (storedPin == hashedPin.toString()) {
            if (!credentialsLoaded) {
                loadCredentials()
            }
            unsealed = true
            return true
        }
        return false
    }

    /**
     * This method produces a new set of credentials for the application and if there is
     * already a set stored, they will be overwritten. A new random seed will be produced
     * for use with the IOTA ledger and an EC keypair with 256 bits size. The user's pin
     * will be stored in hashed form along with the rest of the credentials.
     *
     * TODO: Throw a clear error when the credentials cannot be stored.
     */
    fun generateCredentials(pin: String): Boolean {
        val hashedPin = PasswordHasher.hashPassword(pin.toCharArray()).toString()
        val secureRandom = SecureRandom.getInstance("SHA1PRNG")
        seed = Seed.new()

        keypair = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidOpenSSL")
            .apply {initialize(256, secureRandom) }
            .genKeyPair()

        // Generate a self-signed cert for the newly created key
        val issuerString = "C=DE, O=hermes"
        // Issues and subject will be the same because it's a self-signed certificate
        val issuer = X500Name(issuerString)
        val subject = X500Name(issuerString)
        val serial = secureRandom.nextLong().toBigInteger()
        val notBefore = Date()
        val notAfter = Date(System.currentTimeMillis() + (1000L * 24L * 60L * 60L * 1000L))
        val v3CertificateBuilder = JcaX509v3CertificateBuilder(issuer, serial, notBefore,
            notAfter, subject, keypair.public)
        val certHolder = v3CertificateBuilder
            .build(
                JcaContentSignerBuilder("SHA512withECDSA")
                    .setProvider("AndroidOpenSSL")
                    .build(keypair.private))
        val certificate = JcaX509CertificateConverter()
            .setProvider("AndroidOpenSSL")
            .getCertificate(certHolder)
        certificate.verify(keypair.public)

        // Unfortunately the Android Key Store does not allow a password
        // to be used to encrypt the keypair
        ks.setKeyEntry(application.getString(R.string.auth_private_key), keypair.private, null,
            arrayOf(certificate))

        return sharedPref.edit()
            .putString(application.getString(R.string.auth_hashed_pin), hashedPin)
            .putString(application.getString(R.string.auth_seed), seed.toString())
            .commit()
    }

    /**
     * Returns true if the necessary credentials of the application are available,
     * false otherwise.
     */
    fun credentialsGenerated(): Boolean {
        val seedEmpty = sharedPref.getString(application.getString(R.string.auth_seed), "")
            .isNullOrBlank()
        val hashedPinEmpty = sharedPref.getString(application.getString(R.string.auth_hashed_pin), "")
            .isNullOrBlank()
        val keypairEmpty = !ks.containsAlias(application.getString(R.string.auth_private_key))

        if (seedEmpty != hashedPinEmpty || hashedPinEmpty != keypairEmpty) {
            Log.e(loggingTag, "Application is in an incorrect state. Seed, keypair or pin not available!")
        }
        return !(seedEmpty || hashedPinEmpty || keypairEmpty)
    }

    /**
     * Decrypt the credentials using the user's PIN
     */
    private fun loadCredentials() {
        Log.i(loggingTag, "Unlocking credentials of the application")

        seed = Seed(
            (sharedPref.getString(application.getString(R.string.auth_seed), "") as String)
                .toCharArray()
        )

        val privateKeyEntry =
            ks.getEntry(application.getString(R.string.auth_private_key), null) as KeyStore.PrivateKeyEntry
        val privateKey = privateKeyEntry.privateKey
        val publicKey = privateKeyEntry.certificate.publicKey
        keypair = KeyPair(publicKey, privateKey)
        credentialsLoaded = true
    }

    fun clearCredentials() = sharedPref
            .edit()
            .remove(application.getString(R.string.auth_seed))
            .remove(application.getString(R.string.auth_hashed_pin))
            .remove(application.getString(R.string.auth_private_key))
            .commit()

    fun publicKeyPEM(): String {
        return "-----BEGIN EC PRIVATE KEY-----\n" + Base64.toBase64String(keypair.public.encoded) + "\n-----END EC PRIVATE KEY-----\n"
    }

    fun signMessage(msg: String): String {
        val signer = ECDSASigner(HMacDSAKCalculator(SHA1Digest())).apply {
            init(true,
                ECPrivateKeyParameters((keypair.private as ECPrivateKey).s, Secp256K1Curve.ecDomainParameters)
            )
        }
        val components = signer.generateSignature(msg.toByteArray())
        return Hex.toHexString(components[0].toByteArray() + components[1].toByteArray())
    }
}