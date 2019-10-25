package org.hermes.utils

fun <T> Array<T>.applyIfNotEmpty(f: (Array<T>) -> Unit) {
     if (isNotEmpty()) f(this)
}

fun <T> Array<T>.mapIfNotEmpty(f: (Array<T>) -> Array<T>): Array<T> = if (isNotEmpty()) f(this) else this