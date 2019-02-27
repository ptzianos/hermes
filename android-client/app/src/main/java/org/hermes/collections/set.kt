package org.hermes.collections

/**
 * A HashSet that once created can not be modified.
 */
open class ImmutableHashSet<E>(collection: Collection<E>) {

    private val hashSet: HashSet<E> = HashSet(collection)

    fun contains(elem: E): Boolean {
        return hashSet.contains(elem)
    }

    fun asIterable(): Iterable<E> {
        return hashSet.asIterable()
    }

    fun asSequence(): Sequence<E> {
        return hashSet.asSequence()
    }

    operator fun iterator(): Iterator<E> {
        return this.hashSet.iterator()
    }

    fun size(): Int {
        return this.hashSet.size
    }
}

/**
 * An immutable hash set that also allows the user to use it as an array.
 */
class OrderedImmutableHashSet<E>(al: List<E>): ImmutableHashSet<E>(al) {
    private val orderedList: List<E> = al

    operator fun get(i: Int): E {
        return orderedList[i]
    }

    fun indexOf(elem: E): Int {
        return orderedList.indexOf(elem)
    }
}