package org.hermes.market

import retrofit2.Call
import retrofit2.http.*


interface HermesMarketV1 {
    @GET("/api/v1/users/me")
    fun getMyUser(): Call<User>

    @FormUrlEncoded
    @POST("/api/v1/users/register")
    fun register(@Field("public_key") publicKey: String,
                 @Field("public_key_type") publicKeyType: String): Call<RegistrationResponse>

    @GET("/api/v1/users/{user_id}/keys/{key_id}/message")
    fun getVerificationMessage(@Path("user_id") userId: String,
                               @Path("key_id") keyId: String): Call<PublicKeyVerificationRequest>

    @FormUrlEncoded
    @POST("/api/v1/users/{user_id}/tokens/")
    fun createToken(@Path("user_id") userId: String,
                    @Field("proof_of_ownership_token") proofOfOwnershipToken: String,
                    @Field("proof_of_ownership") proofOfOwnershipMessage: String): Call<APIToken>

    @DELETE("/api/v1/users/{user_id}/tokens/{token_id}")
    fun revokeToken(@Path("user_id") userId: String,
                    @Path("token_id") tokenId: String): Call<Void>

    @GET("/api/v1/ads/")
    fun listAds(): Call<List<Ad>>

    @POST("/api/v1/ads/")
    fun createAd(): Call<Ad>

    @GET("/api/v1/ads/{ad_id}")
    fun getAd(@Path("ad_id") adId: String): Call<Ad>

    @DELETE("/api/v1/ads/{ad_id}")
    fun deleteAd(@Path("ad_id") adId: String): Call<Void>
}

//@GET("user")
//Call<UserDetails> getUserDetails(@Header("Authorization") String credentials)
