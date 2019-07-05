package org.hermes.market

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class EmailAddress(val uuid: String, val address: String, val verified: Boolean)

data class APIToken (
    @Expose @SerializedName("token") val token: String,
    @Expose @SerializedName("expiration_date") val expirationDate: String
)

data class MarketAd(val uuid: String, val network: String, val currency: String)

data class MarketUser(val uuid: String, val name: String, val fullname: String)

data class PublicKey(
        val uuid: String,
        val verified: String,
        @SerializedName("added_on") val addedOn: String
)

data class PublicKeyVerificationRequest(
    @Expose @SerializedName("public_key_verification_token") val publicKeyVerificationToken: String,
    @Expose @SerializedName("public_key_verification_message") val publicKeyVerificationMessage: String
)

data class RegistrationResponse(
    @Expose @SerializedName("uuid") val uuid: String,
    @Expose @SerializedName("name") val name: String,
    @Expose @SerializedName("fullname") val fullname: String,
    @Expose @SerializedName("public_key_id") val publicKeyId: String,
    @Expose @SerializedName("public_key_verification_token") val publicKeyVerificationToken: String,
    @Expose @SerializedName("public_key_verification_message") val publicKeyVerificationMessage: String
)
