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
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.container.InventoryDelegate
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.items.DurableItem

class DurabilityProperty(
    private val stack: ItemStack,
    unbreakable: Boolean = false,
    durability: Int = if (stack.item.item is DurableItem) stack.item.item.maxDurability else -1,
) : Property {
    var _unbreakable = unbreakable
    var unbreakable by InventoryDelegate(stack, this::_unbreakable)
    var _durability = durability
    var durability by InventoryDelegate(stack, this::_durability)

    val damageable: Boolean
        get() {
            try {
                stack.lock.acquire()
                return stack.item.item is DurableItem && stack.item.item.maxDurability > 0 || !_unbreakable
            } finally {
                stack.lock.release()
            }
        }

    val _valid: Boolean
        get() {
            if (_unbreakable) {
                return true
            }
            if (stack.item.item !is DurableItem || stack.item.item.maxDurability <= 1) {
                return true
            }
            if (_durability <= 0) { // ToDo
                return false
            }
            return true
        }

    override fun isDefault(): Boolean {
        return !_unbreakable && (stack.item.item is DurableItem && _durability == stack.item.item.maxDurability)
    }

    fun updateNbt(nbt: MutableJsonObject): Boolean {
        nbt.remove(UNBREAKABLE_TAG)?.toBoolean()?.let { this._unbreakable = it }
        nbt.remove(DAMAGE_TAG)?.toInt()?.let { if (stack.item.item is DurableItem) this._durability = stack.item.item.maxDurability - it }

        return !isDefault()
    }

    override fun hashCode(): Int {
        return Objects.hashCode(_unbreakable, _durability)
    }

    override fun equals(other: Any?): Boolean {
        if (isDefault() && other == null) return true
        if (other !is DurabilityProperty) return false
        if (other.hashCode() != hashCode()) {
            return false
        }
        return _durability == other._durability && _unbreakable == other._unbreakable
    }

    fun copy(
        stack: ItemStack,
        unbreakable: Boolean = this._unbreakable,
        durability: Int = this._durability,
    ): DurabilityProperty {
        return DurabilityProperty(stack, unbreakable, durability)
    }

    companion object {
        private const val UNBREAKABLE_TAG = "unbreakable"
        private const val DAMAGE_TAG = "Damage"
    }
}
