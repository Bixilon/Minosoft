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

package de.bixilon.minosoft.config.profile.delegate

import de.bixilon.kutil.observer.DataObserver
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

@Deprecated("Kutil")
class BackingDelegate<V>(
    private val get: () -> V,
    private val set: (V) -> Unit,
) : DataObserver<V>(get()), ReadWriteProperty<Any, V> {

    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        return get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        set(value)
        super.setValue(thisRef, property, value)
    }
}
