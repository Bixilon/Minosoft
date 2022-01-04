package de.bixilon.minosoft.util.delegate

import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.list.ListChange
import de.bixilon.kutil.watcher.list.ListDataWatcher.Companion.observeList
import de.bixilon.kutil.watcher.map.MapChange
import de.bixilon.kutil.watcher.map.MapDataWatcher.Companion.observeMap
import de.bixilon.kutil.watcher.set.SetChange
import de.bixilon.kutil.watcher.set.SetDataWatcher.Companion.observeSet
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Rendering
import kotlin.reflect.KProperty0

object RenderingDelegate {

    private fun requireContext(): RenderWindow {
        return Rendering.currentContext ?: throw IllegalStateException("Can only be registered in a render context!")
    }

    private fun <V> runInContext(context: RenderWindow, value: V, runnable: (V) -> Unit) {
        val changeContext = Rendering.currentContext
        if (changeContext === context) {
            runnable(value)
        } else {
            context.queue += { runnable(value) }
        }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<V>.observeRendering(owner: Any, observer: (V) -> Unit) {
        val context = requireContext()
        this.observe(owner) { runInContext(context, it, observer) }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<Set<V>>.observeSetRendering(owner: Any, observer: (SetChange<V>) -> Unit) {
        val context = requireContext()
        this.observeSet(owner) { runInContext(context, it, observer) }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<List<V>>.observeListRendering(owner: Any, observer: (ListChange<V>) -> Unit) {
        val context = requireContext()
        this.observeList(owner) { runInContext(context, it, observer) }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <K, V> KProperty0<Map<K, V>>.observeMapRendering(owner: Any, observer: (MapChange<K, V>) -> Unit) {
        val context = requireContext()
        this.observeMap(owner) { runInContext(context, it, observer) }
    }
}
