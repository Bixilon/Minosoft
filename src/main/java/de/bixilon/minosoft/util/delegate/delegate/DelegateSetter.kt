package de.bixilon.minosoft.util.delegate.delegate

interface DelegateSetter<V> {

    fun get(): V
    fun set(value: V)
}
