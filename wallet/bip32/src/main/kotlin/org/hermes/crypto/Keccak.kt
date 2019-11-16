package org.hermes.crypto

import org.bouncycastle.jcajce.provider.digest.Keccak

object Keccak: Hasher(Keccak.Digest256())
