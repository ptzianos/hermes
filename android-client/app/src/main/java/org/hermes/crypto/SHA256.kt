package org.hermes.crypto;

import java.security.MessageDigest

object SHA256: SHAHash(MessageDigest.getInstance("SHA-256"))
