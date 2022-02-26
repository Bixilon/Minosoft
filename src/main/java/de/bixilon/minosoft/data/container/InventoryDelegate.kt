/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container

import de.bixilon.minosoft.data.container.stack.ItemStack
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

class InventoryDelegate<T>(
    val stack: ItemStack,
    val field: KMutableProperty0<T>,
) : ReadWriteProperty<Any, T> {

    override operator fun getValue(thisRef: Any, property: KProperty<*>): T {
        try {
            stack.lock.acquire()
            return field.getValue(thisRef, property)
        } finally {
            stack.lock.release()
        }
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        stack.lock()
        field.setValue(thisRef, property, value)
        stack.commit()
    }
}
