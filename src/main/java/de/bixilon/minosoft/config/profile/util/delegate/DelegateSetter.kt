package de.bixilon.minosoft.config.profile.util.delegate

interface DelegateSetter<V> {

    fun get(): V
    fun set(value: V)
}
