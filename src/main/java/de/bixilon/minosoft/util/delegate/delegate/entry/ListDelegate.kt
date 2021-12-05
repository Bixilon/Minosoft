package de.bixilon.minosoft.util.delegate.delegate.entry

import de.bixilon.minosoft.util.delegate.DelegateManager
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

open class ListDelegate<V>(
    private var value: ObservableList<V>,
    private val verify: ((ListChangeListener.Change<out V>) -> Unit)?,
) : EntryDelegate<MutableList<V>>() {

    init {
        initListener()
    }

    private fun initListener() {
        value.addListener(ListChangeListener {
            verify?.invoke(it)

            Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Changed list entry $it" }
            DelegateManager.onChange(thisRef, property.identifier, null, it)
        })
    }

    override fun get(): MutableList<V> = value

    override fun set(value: MutableList<V>) {
        this.value = FXCollections.synchronizedObservableList(FXCollections.observableList(value))
        initListener()
    }
}
