package de.bixilon.minosoft.util.delegate.delegate.entry

import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.util.delegate.DelegateManager
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import kotlin.reflect.KProperty

open class MapDelegate<K, V>(
    private var value: ObservableMap<K, V>,
    private val verify: ((MapChangeListener.Change<out K, out V>) -> Unit)?,
) : EntryDelegate<MutableMap<K, V>>() {

    init {
        initListener()
    }

    private fun initListener() {
        value.addListener(MapChangeListener {
            verify?.invoke(it)
            if (StaticConfiguration.LOG_DELEGATE) {
                Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Changed map entry $it" }
            }
            DelegateManager.onChange(thisRef, property.identifier, null, it)
        })
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): MutableMap<K, V> {
        checkLateinitValues(property)
        return value
    }

    override fun get(): MutableMap<K, V> = value

    override fun set(value: MutableMap<K, V>) {
        this.value = FXCollections.synchronizedObservableMap(FXCollections.observableMap(value))
        initListener()
        return
    }
}
