/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.other.containers

import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

// https://c4k3.github.io/wiki.vg/images/1/13/Inventory-slots.png
class PlayerInventory(connection: PlayConnection) : Container(
    connection = connection,
    ContainerType(
        resourceLocation = "TBO".toResourceLocation()
    ),
) {

    val equipment: MutableMap<InventorySlots.EquipmentSlots, ItemStack>
        get() {
            val equipment: MutableMap<InventorySlots.EquipmentSlots, ItemStack> = mutableMapOf()

            for (slot in InventorySlots.EquipmentSlots.ARMOR_SLOTS) {
                equipment[slot] = this[slot] ?: continue
            }

            return equipment
        }

    fun getHotbarSlot(hotbarSlot: Int = connection.player.selectedHotbarSlot): ItemStack? {
        check(hotbarSlot in 0..9) { "Hotbar slot out of bounds!" }
        return slots[hotbarSlot + 36] // ToDo
    }

    operator fun get(slot: InventorySlots.EquipmentSlots): ItemStack? {
        return this[when (slot) {
            InventorySlots.EquipmentSlots.HEAD -> 5
            InventorySlots.EquipmentSlots.CHEST -> 6
            InventorySlots.EquipmentSlots.LEGS -> 7
            InventorySlots.EquipmentSlots.FEET -> 8

            InventorySlots.EquipmentSlots.MAIN_HAND -> connection.player.selectedHotbarSlot + 36
            InventorySlots.EquipmentSlots.OFF_HAND -> 45
        }]
    }
}
