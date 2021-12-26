package de.bixilon.minosoft.util.delegate.delegate.entry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.util.delegate.delegate.DelegateSetter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class EntryDelegate<V> : ReadWriteProperty<Any, V>, DelegateSetter<V> {
    protected lateinit var property: KProperty<V>
    protected lateinit var thisRef: Any


    protected fun checkLateinitValues(property: KProperty<*>) {
        if (!this::property.isInitialized) {
            this.property = property.unsafeCast()
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        checkLateinitValues(property)
        this.thisRef = thisRef
        return get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        checkLateinitValues(property)
        this.thisRef = thisRef
        set(value)
    }
}
