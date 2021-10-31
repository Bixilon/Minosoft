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
            // ToDo: Optimize
            val equipment: MutableMap<InventorySlots.EquipmentSlots, ItemStack> = mutableMapOf()

            for (slot in InventorySlots.EquipmentSlots.ARMOR_SLOTS) {
                equipment[slot] = this[slot] ?: continue
            }

            return equipment
        }

    fun getHotbarSlot(hotbarSlot: Int = connection.player.selectedHotbarSlot): ItemStack? {
        check(hotbarSlot in 0..HOTBAR_SLOTS) { "Hotbar slot out of bounds!" }
        return slots[hotbarSlot + HOTBAR_OFFSET] // ToDo
    }

    operator fun get(slot: InventorySlots.EquipmentSlots): ItemStack? {
        return this[slot.slot]
    }

    operator fun set(slot: InventorySlots.EquipmentSlots, itemStack: ItemStack?) {
        this[slot.slot] = itemStack
    }

    @JvmName("setEquipment")
    fun set(vararg slots: Pair<InventorySlots.EquipmentSlots, ItemStack?>) {
        val realSlots: MutableList<Pair<Int, ItemStack?>> = mutableListOf()

        for ((slot, itemStack) in slots) {
            realSlots += Pair(slot.slot, itemStack)
        }

        super.set(*realSlots.toTypedArray())
    }

    val InventorySlots.EquipmentSlots.slot: Int
        get() {
            return when (this) {
                InventorySlots.EquipmentSlots.HEAD -> ARMOR_OFFSET + 0
                InventorySlots.EquipmentSlots.CHEST -> ARMOR_OFFSET + 1
                InventorySlots.EquipmentSlots.LEGS -> ARMOR_OFFSET + 2
                InventorySlots.EquipmentSlots.FEET -> ARMOR_OFFSET + 3

                InventorySlots.EquipmentSlots.MAIN_HAND -> connection.player.selectedHotbarSlot + HOTBAR_OFFSET
                InventorySlots.EquipmentSlots.OFF_HAND -> 45
            }
        }

    companion object {
        const val HOTBAR_OFFSET = 36
        const val ARMOR_OFFSET = 5
        const val HOTBAR_SLOTS = 9
    }
}
