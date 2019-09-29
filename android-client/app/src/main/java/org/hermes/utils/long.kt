package org.hermes.utils

fun Long.toByteArray(): ByteArray = this.toBigInteger().toByteArray()