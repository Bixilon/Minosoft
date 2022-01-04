package de.bixilon.minosoft.util.delegate

import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.list.ListChange
import de.bixilon.kutil.watcher.list.ListDataWatcher.Companion.observeList
import de.bixilon.kutil.watcher.map.MapChange
import de.bixilon.kutil.watcher.map.MapDataWatcher.Companion.observeMap
import de.bixilon.kutil.watcher.set.SetChange
import de.bixilon.kutil.watcher.set.SetDataWatcher.Companion.observeSet
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import kotlin.reflect.KProperty0

object JavaFXDelegate {

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<V>.observeFX(owner: Any, observer: (V) -> Unit) {
        this.observe(owner) { JavaFXUtil.runLater { observer(it) } }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<Set<V>>.observeSetFX(owner: Any, observer: (SetChange<V>) -> Unit) {
        this.observeSet(owner) { JavaFXUtil.runLater { observer(it) } }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<List<V>>.observeListFX(owner: Any, observer: (ListChange<V>) -> Unit) {
        this.observeList(owner) { JavaFXUtil.runLater { observer(it) } }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <K, V> KProperty0<Map<K, V>>.observeMapFX(owner: Any, observer: (MapChange<K, V>) -> Unit) {
        this.observeMap(owner) { JavaFXUtil.runLater { observer(it) } }
    }
}
