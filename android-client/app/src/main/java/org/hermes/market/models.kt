package org.hermes.market

import com.google.gson.annotations.SerializedName

data class EmailAddress(val uuid: String, val address: String, val verified: Boolean)

data class APIToken (val uuid: String, val expires: String, val created_on: String)

data class Ad(val uuid: String,val network: String, val currency: String)

data class User(val uuid: String, val name: String, val fullname: String)

data class PublicKey(
        val uuid: String,
        val verified: String,
        @SerializedName("added_on") val addedOn: String
)

data class PublicKeyVerificationRequest(
        @SerializedName("public_key_verification_token") val publicKeyVerificationToken: String,
        @SerializedName("public_key_verification_message") val publicKeyVerificationMessage: String
)

data class RegistrationResponse(val uuid: String, val name: String, val fullname: String)
