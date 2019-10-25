package org.hermes.utils

import org.hermes.iota.TryteArray

fun Long.toByteArray(): ByteArray = this.toBigInteger().toByteArray()

fun Long.toTryteArray(): TryteArray = TryteArray(this.toByteArray())
