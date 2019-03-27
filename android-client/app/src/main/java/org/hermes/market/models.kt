package org.hermes.market

data class EmailAddress(val uuid: String, val address: String, val verified: Boolean)

data class APIToken (val uuid: String, val expires: String, val created_on: String)

data class Ad(val uuid: String,val network: String, val currency: String)

data class User(val uuid: String, val name: String, val fullname: String)

data class PublicKey(val uuid: String, val verified: String, val added_on: String)

data class PublicKeyVerificationRequest(val public_key_verification_token: String,
                                        val public_key_verification_message: String)

data class RegistrationResponse(val uuid: String, val name: String, val fullname: String)
