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
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.list.ListChange
import de.bixilon.kutil.observer.list.ListObserver.Companion.observeList
import de.bixilon.kutil.observer.map.MapChange
import de.bixilon.kutil.observer.map.MapObserver.Companion.observeMap
import de.bixilon.kutil.observer.map.bi.BiMapObserver.Companion.observeBiMap
import de.bixilon.kutil.observer.set.SetChange
import de.bixilon.kutil.observer.set.SetObserver.Companion.observeSet
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.Rendering
import kotlin.reflect.KProperty0

object RenderingDelegate {

    private fun requireContext(): RenderContext {
        return Rendering.currentContext ?: throw IllegalStateException("Can only be registered in a render context!")
    }

    private fun <V> runInContext(context: RenderContext, value: V, runnable: (V) -> Unit) {
        val changeContext = Rendering.currentContext
        if (changeContext === context) {
            runnable(value)
        } else {
            context.queue += { runnable(value) }
        }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<V>.observeRendering(owner: Any, instant: Boolean = false, context: RenderContext = requireContext(), observer: (V) -> Unit) {
        this.observe(owner, instant) { runInContext(context, it, observer) }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<Set<V>>.observeSetRendering(owner: Any, instant: Boolean = false, context: RenderContext = requireContext(), observer: (SetChange<V>) -> Unit) {
        this.observeSet(owner, instant) { runInContext(context, it, observer) }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<List<V>>.observeListRendering(owner: Any, instant: Boolean = false, context: RenderContext = requireContext(), observer: (ListChange<V>) -> Unit) {
        this.observeList(owner, instant) { runInContext(context, it, observer) }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <K, V> KProperty0<Map<K, V>>.observeMapRendering(owner: Any, instant: Boolean = false, context: RenderContext = requireContext(), observer: (MapChange<K, V>) -> Unit) {
        this.observeMap(owner, instant) { runInContext(context, it, observer) }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <K, V> KProperty0<AbstractBiMap<K, V>>.observeBiMapRendering(owner: Any, instant: Boolean = false, context: RenderContext = requireContext(), observer: (MapChange<K, V>) -> Unit) {
        this.observeBiMap(owner, instant) { runInContext(context, it, observer) }
    }
}
