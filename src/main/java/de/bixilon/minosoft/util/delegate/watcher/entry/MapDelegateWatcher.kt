package de.bixilon.minosoft.util.delegate.watcher.entry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.delegate.DelegateManager
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import de.bixilon.minosoft.util.delegate.watcher.DelegateListener
import javafx.collections.MapChangeListener
import kotlin.reflect.KProperty

class MapDelegateWatcher<K, V>(
    override val property: KProperty<MutableMap<K, V>>,
    private val callback: (MapChangeListener.Change<K, V>) -> Unit,
) : DelegateListener<MutableMap<K, V>> {
    override val field: String = property.identifier

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        fun <K, V> KProperty<MutableMap<K, V>>.watchMap(reference: Any, callback: ((MapChangeListener.Change<K, V>) -> Unit)) {
            DelegateManager.register(reference, MapDelegateWatcher(this, callback))
        }

        fun <K, V> KProperty<MutableMap<K, V>>.watchMapFX(reference: Any, callback: ((MapChangeListener.Change<K, V>) -> Unit)) {
            DelegateManager.register(reference, MapDelegateWatcher(this) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
