package org.hermes.crypto

import org.bouncycastle.jcajce.provider.digest.RIPEMD160

object RIPEMD: Hasher(RIPEMD160.Digest())
