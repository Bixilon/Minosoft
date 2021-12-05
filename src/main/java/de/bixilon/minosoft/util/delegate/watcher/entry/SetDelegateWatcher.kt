package de.bixilon.minosoft.util.delegate.watcher.entry

import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.delegate.DelegateManager
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import de.bixilon.minosoft.util.delegate.watcher.DelegateListener
import javafx.collections.SetChangeListener
import kotlin.reflect.KProperty

class SetDelegateWatcher<V>(
    override val property: KProperty<MutableSet<V>>,
    private val callback: (SetChangeListener.Change<V>) -> Unit,
) : DelegateListener<MutableSet<V>> {
    override val field: String = property.identifier

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        fun <V> KProperty<MutableSet<V>>.watchSet(reference: Any, callback: ((SetChangeListener.Change<V>) -> Unit)) {
            DelegateManager.register(reference, SetDelegateWatcher(this, callback))
        }

        fun <V> KProperty<MutableSet<V>>.watchSetFX(reference: Any, callback: ((SetChangeListener.Change<V>) -> Unit)) {
            DelegateManager.register(reference, SetDelegateWatcher(this) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
