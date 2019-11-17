package org.hermes.collections

import java.util.HashMap

/**
 * ImmutableHashMap is a HashMap that will not accept any new
 * keys after the constructor has returned.
 */
class ImmutableHashMap<K, V>(inputFunction: (ImmutableHashMap<K, V>) -> Unit) : HashMap<K, V>() {

    private var noNewKeys = false

    init {
        inputFunction(this)
        noNewKeys = true
    }

    override fun put(key: K, value: V): V? {
        return if (noNewKeys) null
        else super.put(key, value)
    }

    override fun putAll(from: Map<out K, V>) {
        if (noNewKeys) return
        super.putAll(from)
    }

    override fun putIfAbsent(key: K, value: V): V? {
        return if (noNewKeys) null
        else super.putIfAbsent(key, value)
    }
}
