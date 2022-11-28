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
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.terminal.RunConfiguration
import kotlin.reflect.KProperty0

object JavaFXDelegate {

    private fun checkErosState() {
        if (RunConfiguration.DISABLE_EROS) {
            throw IllegalStateException("Eros is disabled!")
        }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<V>.observeFX(owner: Any, instant: Boolean = true, observer: (V) -> Unit) {
        checkErosState()
        this.observe(owner, instant) { JavaFXUtil.runLater { observer(it) } }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<Set<V>>.observeSetFX(owner: Any, instant: Boolean = true, observer: (SetChange<V>) -> Unit) {
        checkErosState()
        this.observeSet(owner, instant) { JavaFXUtil.runLater { observer(it) } }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <V> KProperty0<List<V>>.observeListFX(owner: Any, instant: Boolean = true, observer: (ListChange<V>) -> Unit) {
        checkErosState()
        this.observeList(owner, instant) { JavaFXUtil.runLater { observer(it) } }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <K, V> KProperty0<Map<K, V>>.observeMapFX(owner: Any, instant: Boolean = true, observer: (MapChange<K, V>) -> Unit) {
        checkErosState()
        this.observeMap(owner, instant) { JavaFXUtil.runLater { observer(it) } }
    }

    @Suppress("NON_PUBLIC_CALL_FROM_PUBLIC_INLINE")
    fun <K, V> KProperty0<AbstractBiMap<K, V>>.observeBiMapFX(owner: Any, instant: Boolean = true, observer: (MapChange<K, V>) -> Unit) {
        checkErosState()
        this.observeBiMap(owner, instant) { JavaFXUtil.runLater { observer(it) } }
    }
}
