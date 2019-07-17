package org.hermes.utils

/**
 * FrozenHashMap is a HashMap that will not accept any new
 * keys after the end of the constructor.
 */
class FrozenHashMap<K,  V> : HashMap<K, V> {

    private var noNewKeys = false

    constructor(inputFunction: (FrozenHashMap<K, V>) -> Unit) {
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