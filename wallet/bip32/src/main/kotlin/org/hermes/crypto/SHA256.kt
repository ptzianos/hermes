package org.hermes.crypto

import java.security.MessageDigest

object SHA256 : Hasher(MessageDigest.getInstance("SHA-256"))
