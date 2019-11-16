package org.hermes.extensions

fun String.endsWithAnyOf(vararg s: String): Boolean {
    for (_s in s)
        if (this.endsWith(_s))
            return true
    return false
}
