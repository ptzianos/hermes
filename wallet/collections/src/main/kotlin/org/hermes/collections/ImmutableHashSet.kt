package org.hermes.collections

import java.util.HashSet
import kotlin.collections.Collection

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
