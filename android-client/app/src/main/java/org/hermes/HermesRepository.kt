package org.hermes

import android.app.*
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.launch
import java.security.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import org.bouncycastle.asn1.x500.X500Name
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder
import org.bouncycastle.jcajce.provider.digest.SHA3
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder
import org.bouncycastle.util.encoders.Hex

import org.hermes.crypto.PasswordHasher
import org.hermes.entities.Event
import org.hermes.iota.Seed
import org.hermes.utils.LinuxSecureRandom


@Singleton
class HermesRepository @Inject constructor(val application: Application,
                                           val db: HermesRoomDatabase,
                                           @param:Named("auth") val sharedPref: SharedPreferences,
                                           private val ks: KeyStore) {

    private val loggingTag = "HermesRepository"

    private lateinit var seed: Seed
    private lateinit var keypair: KeyPair

    private var unsealed: Boolean = false
    private var credentialsLoaded: Boolean = false
    private var ledgerServiceBootstrapped: Boolean = false
    var ledgerServiceRunning: AtomicBoolean = AtomicBoolean(true)
    private var sensorList: LinkedList<LedgerService.Sensor> = LinkedList()
    private var sensorListData: MutableLiveData<List<LedgerService.Sensor>> = {
        val mld = MutableLiveData<List<LedgerService.Sensor>>()
        mld.value = sensorList
        mld
    }()
    private val activeSensorNum: MutableLiveData<Int> = {
        val mld = MutableLiveData<Int>()
        mld.value = 0
        mld
    }()
    val ledgerServiceUptime: MutableLiveData<Int> = {
        val mld = MutableLiveData<Int>()
        mld.value = 0
        mld
    }()
    val packetBroadcastNum: MutableLiveData<Int> = {
        val mld = MutableLiveData<Int>()
        mld.value = 0
        mld
    }()
    val ledgerServiceRunningLiveData: MutableLiveData<Boolean> = {
        val mld = MutableLiveData<Boolean>()
        mld.value = ledgerServiceRunning.get()
        mld
    }()
    private val pkHashString by lazy {
        val sha3512 = SHA3.Digest512().apply { update(keypair.public.encoded) }
        Hex.toHexString(sha3512.digest())
    }
    val pkHash: String
        get() {
            return if (!credentialsLoaded) ""
            else pkHashString
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

        seed = Seed.new()

        // Generate the EC KeyPair
        keypair = KeyPairGenerator.getInstance("EC")
            .apply { initialize(256, SecureRandom.getInstance("SHA1PRNG")) }
            .generateKeyPair()

        // Generate a self-signed cert for the newly created key
        val issuerString = "C=DE, O=hermes"
        // Issues and subject will be the same because it's a self-signed certificate
        val issuer = X500Name(issuerString)
        val subject = X500Name(issuerString)
        val serial = SecureRandom.getInstance("SHA1PRNG").nextLong().toBigInteger()
        val notBefore = Date()
        val notAfter = Date(System.currentTimeMillis() + (1000L * 24L * 60L * 60L * 1000L))
        val v3CertificateBuilder = JcaX509v3CertificateBuilder(issuer, serial, notBefore,
            notAfter, subject, keypair.public)
        val certHolder = v3CertificateBuilder
            .build(JcaContentSignerBuilder("SHA512withECDSA")
            .setProvider("AndroidOpenSSL")
            .build(keypair.private))
        val certificate = JcaX509CertificateConverter()
            .setProvider("AndroidOpenSSL")
            .getCertificate(certHolder)
        certificate.verify(keypair.public)

        // Unfortunately the Android Key Store does not allow a password to be used to encrypt the keypair
        ks.setKeyEntry(application.getString(R.string.auth_private_key), keypair.private, null,
            arrayOf(certificate))

        return sharedPref.edit()
            .putString(application.getString(R.string.auth_hashed_pin), hashedPin)
            .putString(application.getString(R.string.auth_seed), seed.toString())
            .commit()
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

    fun seal() {
        unsealed = false
    }

    fun fetchEvent(id: Int, callback: (event: Event) -> Unit) {
        CoroutineScope(BACKGROUND.asCoroutineDispatcher()).launch {
            callback(db.eventDao().findById(id))
        }
    }

    fun fetchSensor(id: String, callback: (sensor: LedgerService.Sensor) -> Unit) {
        for (sensor in sensorList) {
            if (sensor.dataId == id) {
                callback(sensor)
                return
            }
        }
    }

    /**
     * Returns true if the necessary credentials of the application are available,
     * false otherwise.
     */
    fun credentialsSet(): Boolean {
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

    fun unsealed(): Boolean {
        return unsealed
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

        // TODO: Start the service only if it's not running
        startLedgerService()

        credentialsLoaded = true
    }

    fun getSensorLiveData(): LiveData<List<LedgerService.Sensor>> {
        return sensorListData
    }

    /**
     * Returns the number of minutes the service has been running
     */
    fun getLedgerServiceUptime(): LiveData<Int> {
        return ledgerServiceUptime
    }

    fun getActiveSensorNumLiveData(): LiveData<Int> {
        return activeSensorNum
    }

    fun getPacketsBroadcast(): LiveData<Int> {
        return packetBroadcastNum
    }

    /**
     * Start the LedgerService if it's not running already
     */
    private fun startLedgerService() {
        if (!ledgerServiceBootstrapped) {
            Log.i(loggingTag,"Ledger service is not running. Starting it now")
            val intent = Intent(application.applicationContext, LedgerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) application.startForegroundService(intent)
            else application.startService(intent)
            ledgerServiceBootstrapped = true
        } else {
            Log.i(loggingTag, "Ledger service is already running")
        }
    }

    fun addSensor(sensor: LedgerService.Sensor) {
        sensorList.add(sensor)
        // Do this to notify clients that the data has changed
        sensorListData.postValue(sensorListData.value)
        activeSensorNum.postValue(sensorList.filter { it.active.get() }.size)
    }

    fun removeSensor(sensor: LedgerService.Sensor) {
        sensorList.remove(sensor)
        // Do this to notify clients that the data has changed
        sensorListData.postValue(sensorList)
        activeSensorNum.postValue(sensorList.filter { it.active.get() }.size)
    }

    fun refreshSensorList() {
        sensorListData.value = sensorList
        activeSensorNum.value = sensorList.filter { it.active.get() }.size
    }

    fun getSeed(): Seed? {
        return seed
    }

    fun getKeyPair(): KeyPair? {
        return keypair
    }
}
