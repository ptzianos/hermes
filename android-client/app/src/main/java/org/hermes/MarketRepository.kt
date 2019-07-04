package org.hermes

import android.app.Application
import android.content.SharedPreferences
import android.os.Handler
import android.util.Log
import org.bouncycastle.util.encoders.Base64
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import retrofit2.Response
import retrofit2.Retrofit

import org.hermes.entities.Ad
import org.hermes.entities.Sensor
import org.hermes.entities.User
import org.hermes.market.HermesMarketV1
import org.hermes.market.RegistrationResponse


@Singleton
class MarketRepository @Inject constructor(
    val application: Application,
    private val db: HermesRoomDatabase,
    private val retroBuilder: Retrofit.Builder,
    private val cryptoRepository: CryptoRepository,
    @param:Named("auth") val sharedPrefs: SharedPreferences
) {

    private val loggingTag = "MarketRepository"

    private var token: String? = null

    fun registerUser(domain: String = "hermes-data.io"): Boolean {
        Log.i(loggingTag, "Deleting any user in the database for domain $domain")
        db.userDao().deleteByDomain(domain)
        Log.i(loggingTag,"Registering user to the marketplace")
        val apiService = retroBuilder.baseUrl("https://$domain")
            .build()
            .create(HermesMarketV1::class.java)
        val publicKeyHex = cryptoRepository.publicKeyHex()
        Log.i(loggingTag, "Registering application to the marketplace with public key $publicKeyHex")
        val registrationResp: Response<RegistrationResponse>
        try {
            registrationResp = apiService
                .register(publicKeyHex, "ecdsa+electrum")
                .execute()
        } catch (e: Exception) {
            Log.e(loggingTag, "$e")
            return false
        }
        if (!registrationResp.isSuccessful || registrationResp.body() == null) {
            Log.e(loggingTag, "Could not register user with marketplace: " +
                    "${registrationResp.code()}")
            return false
        }
        val registrationResponse = registrationResp.body()!!
        Log.i(loggingTag, "Storing user with ${registrationResponse.uuid} from market " +
                "https://$domain to the database")
        db.userDao().insertAll(User(
            marketUUID = registrationResponse.uuid, name = registrationResponse.name,
            domain = domain, token = null
        ))
        Log.i(loggingTag, "Verifying user's key with market https://$domain")
        val apiToken = getToken(
            registrationResponse.uuid,
            apiService,
            registrationResponse.uuid,
            registrationResponse.publicKeyVerificationToken,
            registrationResponse.publicKeyVerificationMessage)
        if (apiToken != null) {
            sharedPrefs.edit()
                .putBoolean(domain + "_token", true)
                .apply()
        }
        return true
    }

    fun getToken(userUUID: String, apiService: HermesMarketV1, userId: String, proofOfOwnershipToken: String,
                 proofOfOwnershipMessage: String): String? {
        val signedMessage = cryptoRepository.signMessageElectrumStyle(proofOfOwnershipMessage)
        Log.i(loggingTag, "Sending signature $signedMessage for message: $proofOfOwnershipMessage")
        val verificationResp = apiService
            .createToken(userId, proofOfOwnershipToken, signedMessage)
            .execute()
        val verificationBody = verificationResp.body()
        if (verificationResp.isSuccessful && verificationBody != null) {
            Log.i(loggingTag, "Updating user's $userUUID token in the database")
            db.userDao().updateToken(userUUID, Base64.toBase64String(verificationBody.token.toByteArray()))
            token = verificationBody.token
        }
        return verificationBody?.token
    }

    fun loadToken(domain: String = "hermes-data.io") {
        val user: User? = db.userDao().findByMarket(domain = domain)
        token = user?.token
    }

    fun registered(domain: String = "hermes-data.io"): Boolean {
        return sharedPrefs.getBoolean(domain + "_registered", false)
    }

    fun tokenAcquired(domain: String = "hermes-data.io"): Boolean {
        return sharedPrefs.getBoolean(domain + "_token", false)
    }

    fun postOrPingAd(sensor: Sensor, domain: String = "hermes-data.io",
                     toastHandler: Handler, callback: (ad: Ad) -> Unit, rootAddress: String) {
        val user = db.userDao().findByMarket(domain)
        if (user == null) {
            toastHandler.sendMessage(toastHandler.obtainMessage().apply {
                obj = "You have not registered with marketplace at $domain"
            })
            return
        }
        val ad = db.adDao().findByUserId(user.uid!!)
        if (ad == null) {
            Log.e(loggingTag, "There is no advertisement posted for this sensor to $domain. Posting now.")
            val apiService = try {
                retroBuilder.baseUrl("https://$domain")
                    .build()
                    .create(HermesMarketV1::class.java)
            } catch (e: Exception) {
                Log.e(loggingTag, "Could not post advertisement to the marketplace $e")
                toastHandler.sendMessage(toastHandler.obtainMessage().apply {
                    obj = "There was an error while trying to create advertisement"
                })
                return
            }
            if (user.token == null) {
                Log.e(loggingTag, "There is no valid token for the user for market $domain")
                toastHandler.sendMessage(toastHandler.obtainMessage().apply {
                    obj = "There was an error while trying to create advertisement"
                })
                return
            }
            Log.e(loggingTag, "api token used for ad post is ${user.token}")
            val adResp = apiService
                .createAd(sensor.mtype, sensor.unit, rootAddress, "Bearer ${user.token!!}")
                .execute()
            if (!adResp.isSuccessful) {
                Log.e(loggingTag, "Could not post advertisement to the marketplace ${adResp.message()}")
                toastHandler.sendMessage(toastHandler.obtainMessage().apply {
                    obj = "There was an error while trying to create advertisement"
                })
                return
            }
            val marketAd = adResp.body()!!
            val newAd = Ad(uuid = marketAd.uuid, network = StorageBackend.IOTA.name, currency = "ETH",
                userId = user.uid!!, sensorUUID = sensor.uuid)
            db.adDao().insertAll(newAd)
            Log.i(loggingTag, "Saved new Ad into the database with uuid: ${newAd.uid}")
            callback(newAd)
        }
    }
}
