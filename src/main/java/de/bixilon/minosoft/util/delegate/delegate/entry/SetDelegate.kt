package de.bixilon.minosoft.util.delegate.delegate.entry

import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.util.delegate.DelegateManager
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener

open class SetDelegate<V>(
    private var value: ObservableSet<V>,
    private val verify: ((SetChangeListener.Change<out V>) -> Unit)?,
) : EntryDelegate<MutableSet<V>>() {

    init {
        initListener()
    }

    private fun initListener() {
        value.addListener(SetChangeListener {
            verify?.invoke(it)

            if (StaticConfiguration.LOG_DELEGATE) {
                Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Changed set entry $it" }
            }
            DelegateManager.onChange(thisRef, property.identifier, null, it)
        })
    }

    override fun get(): MutableSet<V> = value

    override fun set(value: MutableSet<V>) {
        this.value = FXCollections.synchronizedObservableSet(FXCollections.observableSet(value))
        initListener()
    }
}
