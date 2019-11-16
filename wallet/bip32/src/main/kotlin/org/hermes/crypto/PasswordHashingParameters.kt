package org.hermes.crypto

object PasswordHashingParameters {
    var Iterations: Int = 1000
    var Salt: ByteArray = "fjggne0rg2sr24dlgsouug".toByteArray()
    var KeySize: Int = 64 * 8
}
