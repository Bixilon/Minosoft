/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.delegate

import de.bixilon.kutil.collections.map.bi.AbstractBiMap
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.kutil.watcher.list.ListChange
import de.bixilon.kutil.watcher.list.ListDataWatcher.Companion.observeList
import de.bixilon.kutil.watcher.map.MapChange
import de.bixilon.kutil.watcher.map.MapDataWatcher.Companion.observeMap
import de.bixilon.kutil.watcher.map.bi.BiMapDataWatcher.Companion.observeBiMap
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

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <K, V> KProperty0<AbstractBiMap<K, V>>.observeBiMapRendering(owner: Any, observer: (MapChange<K, V>) -> Unit) {
        val context = requireContext()
        this.observeBiMap(owner) { runInContext(context, it, observer) }
    }
}
