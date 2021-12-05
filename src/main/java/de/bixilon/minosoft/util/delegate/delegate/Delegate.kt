package de.bixilon.minosoft.util.delegate.delegate

import de.bixilon.minosoft.util.delegate.DelegateManager
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class Delegate<V>(
    private var value: V,
    private var check: ((V) -> Unit)?,
) : ReadWriteProperty<Any, V> {

    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        if (this.value == value) {
            return
        }
        check?.invoke(value)

        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Changed ${this.value} in $thisRef to $value" }
        val previous = this.value
        this.value = value
        DelegateManager.onChange(thisRef, property.identifier, previous, value)
    }
}
