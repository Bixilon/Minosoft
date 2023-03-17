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

package de.bixilon.minosoft.data.container.equipment

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.enchantment.slots.SlotSpecificEnchantment

class EntityEquipment(
    private val entity: Entity,
    val equipment: LockMap<EquipmentSlots, ItemStack> = lockMapOf(),
) {

    operator fun get(slot: EquipmentSlots): ItemStack? {
        return equipment[slot]
    }

    operator fun set(slot: EquipmentSlots, value: ItemStack?) {
        if (value == null) {
            this -= slot
            return
        }
        equipment[slot] = value
    }

    fun remove(slot: EquipmentSlots) {
        this.equipment -= slot
    }

    operator fun minusAssign(slot: EquipmentSlots) = remove(slot)


    operator fun get(enchantment: Enchantment?): Int {
        if (enchantment == null) {
            return 0
        }
        var maxLevel = 0
        this.equipment.lock.acquire()
        for ((slot, item) in this.equipment.unsafe) {
            if (enchantment is SlotSpecificEnchantment && !enchantment.canApply(entity, slot, item)) {
                continue
            }
            val level = item._enchanting?.enchantments?.get(enchantment) ?: continue
            if (level > maxLevel) {
                maxLevel = level
            }
        }
        this.equipment.lock.release()
        return maxLevel
    }
}
