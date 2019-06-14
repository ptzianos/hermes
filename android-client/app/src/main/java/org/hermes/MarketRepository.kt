package org.hermes

import android.util.Log
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Response
import retrofit2.Retrofit

import org.hermes.entities.User
import org.hermes.market.APIToken
import org.hermes.market.HermesMarketV1
import org.hermes.market.RegistrationResponse


@Singleton
class MarketRepository @Inject constructor(
    private val db: HermesRoomDatabase,
    private val retroBuilder: Retrofit.Builder,
    private val cryptoRepository: CryptoRepository
) {

    private val loggingTag = "MarketRepository"

    fun registerUser(domain: String = "hermes-data.io"): Boolean {
        Log.i(loggingTag, "Deleting any user in the database for domain $domain")
        db.userDao().deleteByDomain(domain)
        Log.i(loggingTag,"Registering user to the marketplace")
        val apiService = retroBuilder.baseUrl("https://$domain")
            .build()
            .create(HermesMarketV1::class.java)
        val publicKeyDer = cryptoRepository.publicKeyDER()
        val registrationResp: Response<RegistrationResponse>
        try {
            registrationResp = apiService
                .register(publicKeyDer, "ECDSA")
                .execute()
        } catch (e: Exception) {
            Log.e(loggingTag, "$e")
            return false
        }
        if (!registrationResp.isSuccessful) {
            Log.e(loggingTag, "Could not register user with marketplace: " +
                    "${registrationResp.code()}")
            return false
        } else {
            val registrationResponse = registrationResp.body()!!
            Log.i(loggingTag, "Verifying user's key with market https://$domain")
            val apiToken = createToken(
                apiService,
                registrationResponse.uuid,
                registrationResponse.publicKeyVerificationToken,
                registrationResponse.publicKeyVerificationMessage)
            if (apiToken != null) {
                Log.i(loggingTag, "Storing user with ${registrationResponse.uuid} from market " +
                        "https://$domain to the database")
                db.userDao().insertAll(User(
                    marketUUID = registrationResponse.uuid, name = registrationResponse.name,
                    domain = domain, token = apiToken.token
                ))
            }
            return apiToken != null
        }
    }

    fun createToken(apiService: HermesMarketV1, userId: String, proofOfOwnershipToken: String,
                    proofOfOwnershipMessage: String): APIToken? {
        val signedMessage = cryptoRepository.signMessage(proofOfOwnershipMessage)
        val verificationResp = apiService
            .createToken(userId, proofOfOwnershipToken, signedMessage)
            .execute()
        return verificationResp.body()
    }
}
