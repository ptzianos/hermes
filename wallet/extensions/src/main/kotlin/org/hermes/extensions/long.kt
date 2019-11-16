package org.hermes.extensions

fun Long.toByteArray(): ByteArray = this.toBigInteger().toByteArray()
