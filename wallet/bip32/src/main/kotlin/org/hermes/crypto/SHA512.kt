package org.hermes.crypto

import java.security.MessageDigest

object SHA512 : Hasher(MessageDigest.getInstance("SHA-512"))
