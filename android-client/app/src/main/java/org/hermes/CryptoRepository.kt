package org.hermes

import android.app.Application
import android.content.SharedPreferences
import android.util.Log
import org.bouncycastle.jcajce.provider.digest.SHA3
import java.security.*
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.util.encoders.Hex

import org.hermes.crypto.PasswordHasher
import org.hermes.crypto.SecP256K1PrivKey
import org.hermes.crypto.SecP256K1PubKey
import org.hermes.iota.Seed


@Singleton
class CryptoRepository @Inject constructor(
    val application: Application,
    val db: HermesRoomDatabase,
    @param:Named("auth") val sharedPref: SharedPreferences
) {

    private val loggingTag = "CryptoRepository"

    private lateinit var seed: Seed
    private var privateKey: SecP256K1PrivKey? = null
    private var publicKey: SecP256K1PubKey? = null

    private var unsealed: Boolean = false
    private var credentialsLoaded: Boolean = false

    fun sealed(): Boolean = !unsealed

    fun unsealed(): Boolean = unsealed

    init {
        Security.addProvider(BouncyCastleProvider())
    }

    private val pkHashString by lazy {
        val sha3512 = SHA3.Digest512().apply { update(publicKey?.encoded) }
        Hex.toHexString(sha3512.digest())
    }
    val pkHash: String
        get() {
            return if (!credentialsLoaded) ""
            else pkHashString
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
        clearCredentials()
        val hashedPin = PasswordHasher.hashPassword(pin.toCharArray()).toString()
        seed = Seed.new()
        privateKey = SecP256K1PrivKey.random()

//        val parameterSpec: KeyGenParameterSpec = KeyGenParameterSpec.Builder(
//            application.getString(R.string.auth_private_key),
//            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
//        ).run {
//            setDigests(KeyProperties.DIGEST_SHA1, KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
//            setKeySize(256)
//            build()
//        }
//
//        // Keypair is also added into the keystore directly
//        keypair = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")
//            .apply { initialize(parameterSpec, secureRandom) }
//            .genKeyPair()
//        // Generate a self-signed cert for the newly created key
//        val issuerString = "C=DE, O=hermes"
//        // Issues and subject will be the same because it's a self-signed certificate
//        val issuer = X500Name(issuerString)
//        val subject = X500Name(issuerString)
//        val serial = secureRandom.nextLong().toBigInteger()
//        val notBefore = Date()
//        val notAfter = Date(System.currentTimeMillis() + (1000L * 24L * 60L * 60L * 1000L))
//        val v3CertificateBuilder = JcaX509v3CertificateBuilder(issuer, serial, notBefore,
//            notAfter, subject, keypair.public)
//        val certHolder = v3CertificateBuilder
//            .build(
//                JcaContentSignerBuilder("SHA512withECDSA")
//                    .setProvider("AndroidKeyStore")
//                    .build(keypair.private))
//        val certificate = JcaX509CertificateConverter()
//            .setProvider("AndroidKeyStore")
//            .getCertificate(certHolder)
//        certificate.verify(keypair.public)
//
//        // Unfortunately the Android Key Store does not allow a password
//        // to be used to encrypt the keypair
//        ks.setKeyEntry(application.getString(R.string.auth_private_key), keypair.private, null,
//            arrayOf(certificate))

        return sharedPref.edit()
            .putString(application.getString(R.string.auth_seed), seed.toString())
            .putString(application.getString(R.string.auth_hashed_pin), hashedPin)
            // TODO:  See if this can be added to the keystore
            .putString(application.getString(R.string.auth_private_key), Hex.toHexString(privateKey?.value?.toByteArray()))
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
        val keypairEmpty = sharedPref.getString(application.getString(R.string.auth_private_key), "")
            .isNullOrBlank()

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
        privateKey = SecP256K1PrivKey(sharedPref.getString(application.getString(R.string.auth_private_key), "") as String)
        publicKey = SecP256K1PubKey.fromPrivateKey(privateKey!!)

//        val privateKeyEntry =
//            ks.getEntry(application.getString(R.string.auth_private_key), null)
//        val privateKey = privateKeyEntry.privateKey
//        val publicKey = privateKeyEntry.certificate.publicKey
//        keypair = KeyPair(publicKey, privateKey)
        credentialsLoaded = true
    }

    fun clearCredentials() = sharedPref
            .edit()
            .remove(application.getString(R.string.auth_seed))
            .remove(application.getString(R.string.auth_hashed_pin))
            .remove(application.getString(R.string.auth_private_key))
            .commit()

    fun seal() {
        unsealed = false
        privateKey = null

    }

    fun publicKeyHex(): String = publicKey?.encodedHex ?: ""

    fun signMessageElectrumStyle(msg: String): String = privateKey?.electrumSign(msg) ?: ""

    fun IOTASeed(): Seed = seed

    fun privateKey(): SecP256K1PrivKey? = privateKey
}