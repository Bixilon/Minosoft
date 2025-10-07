/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container.stack.properties

import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import java.util.*

data class DurabilityProperty(
    private val stack: ItemStack,
    val unbreakable: Boolean = false,
    val durability: Int = if (stack.item is DurableItem) stack.item.maxDurability else -1,
) : Property {

    val damageable get() = stack.item is DurableItem && stack.item.maxDurability > 0 || !unbreakable

    val _valid: Boolean
        get() {
            if (unbreakable) {
                return true
            }
            if (stack.item !is DurableItem || stack.item.maxDurability <= 1) {
                return true
            }
            if (durability <= 0) { // ToDo
                return false
            }
            return true
        }

    fun updateNbt(nbt: MutableJsonObject): Boolean {
        nbt.remove(UNBREAKABLE_TAG)?.toBoolean()?.let { this.unbreakable = it }
        nbt.remove(DAMAGE_TAG)?.toInt()?.let { if (stack.item is DurableItem) this.durability = stack.item.maxDurability - it }
    }

    companion object {
        private const val UNBREAKABLE_TAG = "unbreakable"
        private const val DAMAGE_TAG = "Damage"
    }
}
