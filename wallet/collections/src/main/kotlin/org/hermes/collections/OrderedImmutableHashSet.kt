package org.hermes.collections

/**
 * An immutable hash set that also allows the user to use it as an array.
 */
class OrderedImmutableHashSet<E>(al: List<E>) : ImmutableHashSet<E>(al) {
    private val orderedList: List<E> = al

    operator fun get(i: Int): E {
        return orderedList[i]
    }

    fun indexOf(elem: E): Int {
        return orderedList.indexOf(elem)
    }
}
