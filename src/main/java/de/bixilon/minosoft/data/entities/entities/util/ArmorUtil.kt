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

package de.bixilon.minosoft.data.entities.entities.util

import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.items.armor.ArmorItem

object ArmorUtil {

    val LockMap<InventorySlots.EquipmentSlots, ItemStack>.protectionLevel: Float
        get() {
            var protectionLevel = 0.0f

            this.lock.acquire()
            for (equipment in this.original.values) {
                val item = equipment.item.item

                if (item is ArmorItem) {
                    // could also be a pumpkin or just trash
                    protectionLevel += item.protection
                }
            }
            this.lock.release()

            return protectionLevel
        }

    fun LockMap<InventorySlots.EquipmentSlots, ItemStack>.getHighestLevel(enchantment: Enchantment?): Int {
        if (enchantment == null) {
            return 0
        }
        var maxLevel = 0
        this.lock.acquire()
        for (stack in this.original.values) {
            val level = stack._enchanting?.enchantments?.get(enchantment) ?: continue
            if (level > maxLevel) {
                maxLevel = level
            }
        }
        this.lock.release()
        return maxLevel
    }
}
