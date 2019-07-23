package org.hermes.crypto

import java.security.MessageDigest

class SHA512: SHAHash(MessageDigest.getInstance("SHA512"))