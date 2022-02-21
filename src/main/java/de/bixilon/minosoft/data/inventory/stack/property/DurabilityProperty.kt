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

package de.bixilon.minosoft.data.inventory.stack.property

import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.inventory.InventoryDelegate
import de.bixilon.minosoft.data.inventory.stack.ItemStack

class DurabilityProperty(
    private val stack: ItemStack,
    unbreakable: Boolean = false,
    durability: Int = stack.item.item.maxDurability,
) : Property {
    var _unbreakable = unbreakable
    var unbreakable by InventoryDelegate(stack, this::_unbreakable)
    var _durability = durability
    var durability by InventoryDelegate(stack, this::_durability)

    val damageable: Boolean
        get() {
            try {
                stack.lock.acquire()
                return stack.item.item.maxDurability > 0 || !_unbreakable
            } finally {
                stack.lock.release()
            }
        }

    val _valid: Boolean
        get() {
            if (_unbreakable) {
                return true
            }
            if (stack.item.item.maxDurability <= 1) {
                return true
            }
            if (_durability <= 0) { // ToDo
                return false
            }
            return true
        }

    override fun updateNbt(nbt: MutableJsonObject) {
        nbt.remove(UNBREAKABLE_TAG)?.toBoolean()?.let { this._unbreakable = it }
    }

    fun copy(
        stack: ItemStack,
        unbreakable: Boolean = this.unbreakable,
        durability: Int = this.durability,
    ): DurabilityProperty {
        return DurabilityProperty(stack, unbreakable, durability)
    }

    companion object {
        private const val UNBREAKABLE_TAG = "unbreakable"
    }
}
