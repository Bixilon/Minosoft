/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container.stack.property

import com.google.common.base.Objects
import de.bixilon.minosoft.data.container.InventoryDelegate
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.items.Item

class ItemProperty(
    private val stack: ItemStack,
    val item: Item,
    count: Int = 1,
) : Property {
    var _count = count
    var count by InventoryDelegate(stack, this::_count)


    fun decreaseCount() {
        stack.lock.lock()
        _count -= 1
        stack.commitChange()
    }

    fun increaseCount() {
        stack.lock.lock()
        _count += 1
        stack.commitChange()
    }

    override fun isDefault(): Boolean = false

    override fun hashCode(): Int {
        return Objects.hashCode(item, _count)
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ItemProperty) {
            return false
        }
        if (other.hashCode() != hashCode()) {
            return false
        }
        return item == other.item && _count == other._count
    }
}
