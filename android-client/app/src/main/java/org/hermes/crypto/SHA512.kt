package org.hermes.crypto

import java.security.MessageDigest

object SHA512: SHAHash(MessageDigest.getInstance("SHA-512"))