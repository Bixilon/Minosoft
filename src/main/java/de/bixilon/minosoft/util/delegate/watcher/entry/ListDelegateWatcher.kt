package de.bixilon.minosoft.util.delegate.watcher.entry

import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.delegate.DelegateManager
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import de.bixilon.minosoft.util.delegate.watcher.DelegateListener
import javafx.collections.ListChangeListener
import kotlin.reflect.KProperty

class ListDelegateWatcher<V>(
    override val property: KProperty<MutableList<V>>,
    private val callback: (ListChangeListener.Change<V>) -> Unit,
) : DelegateListener<MutableList<V>> {
    override val field: String = property.identifier


    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        fun <V> KProperty<MutableList<V>>.watchList(reference: Any, callback: ((ListChangeListener.Change<V>) -> Unit)) {
            DelegateManager.register(reference, ListDelegateWatcher(this, callback))
        }

        fun <V> KProperty<MutableList<V>>.watchListFX(reference: Any, callback: ((ListChangeListener.Change<V>) -> Unit)) {
            DelegateManager.register(reference, ListDelegateWatcher(this) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
