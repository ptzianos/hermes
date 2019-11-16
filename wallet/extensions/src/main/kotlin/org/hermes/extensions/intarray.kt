package org.hermes.extensions

import org.hermes.ternary.Trit
import org.hermes.ternary.TritArray

fun IntArray.toTritArray(): TritArray = TritArray(this.size) { Trit(this[it]) }
