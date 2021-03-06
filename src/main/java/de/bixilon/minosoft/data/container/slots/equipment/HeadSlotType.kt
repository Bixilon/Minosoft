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

package de.bixilon.minosoft.data.container.slots.equipment

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.items.armor.ArmorItem

object HeadSlotType : EquipmentSlotType {

    override fun canPut(container: Container, slot: Int, stack: ItemStack): Boolean {
        val item = stack.item.item
        if (item.resourceLocation == MinecraftBlocks.CARVED_PUMPKIN) {
            return super.canPut(container, slot, stack)
        }
        if (item !is ArmorItem) {
            return false
        }
        return item.equipmentSlot == InventorySlots.EquipmentSlots.HEAD && super.canPut(container, slot, stack)
    }
}
