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

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.minosoft.data.inventory.InventorySlots
import de.bixilon.minosoft.data.inventory.click.SlotSwapContainerAction
import de.bixilon.minosoft.data.inventory.stack.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.other.containers.slots.DefaultSlotType
import de.bixilon.minosoft.data.registries.other.containers.slots.RemoveOnlySlotType
import de.bixilon.minosoft.data.registries.other.containers.slots.SlotType
import de.bixilon.minosoft.data.registries.other.containers.slots.equipment.ChestSlotType
import de.bixilon.minosoft.data.registries.other.containers.slots.equipment.FeetSlotType
import de.bixilon.minosoft.data.registries.other.containers.slots.equipment.HeadSlotType
import de.bixilon.minosoft.data.registries.other.containers.slots.equipment.LegsSlotType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

// https://c4k3.github.io/wiki.vg/images/1/13/Inventory-slots.png
class PlayerInventory(connection: PlayConnection) : Container(
    connection = connection,
    ContainerType(
        resourceLocation = "TBO".toResourceLocation()
    ),
) {
    val equipment: LockMap<InventorySlots.EquipmentSlots, ItemStack> = lockMapOf() // ToDo: Update map


    fun getHotbarSlot(hotbarSlot: Int = connection.player.selectedHotbarSlot): ItemStack? {
        check(hotbarSlot in 0..HOTBAR_SLOTS) { "Hotbar slot out of bounds!" }
        return slots[hotbarSlot + HOTBAR_OFFSET]
    }

    operator fun get(slot: InventorySlots.EquipmentSlots): ItemStack? {
        return this[slot.slot]
    }

    operator fun set(slot: InventorySlots.EquipmentSlots, itemStack: ItemStack?) {
        this[slot.slot] = itemStack
    }

    operator fun get(hand: Hands): ItemStack? {
        return get(when (hand) {
            Hands.MAIN -> InventorySlots.EquipmentSlots.MAIN_HAND
            Hands.OFF -> InventorySlots.EquipmentSlots.OFF_HAND
        })
    }

    @JvmName("setEquipment")
    fun set(vararg slots: Pair<InventorySlots.EquipmentSlots, ItemStack?>) {
        val realSlots: MutableList<Pair<Int, ItemStack?>> = mutableListOf()

        for ((slot, itemStack) in slots) {
            realSlots += Pair(slot.slot, itemStack)
        }

        super.set(*realSlots.toTypedArray())
    }

    override fun getSlotType(slotId: Int): SlotType? {
        return when (slotId) {
            0 -> RemoveOnlySlotType // crafting result
            in 1..4 -> DefaultSlotType // crafting
            ARMOR_OFFSET + 0 -> HeadSlotType
            ARMOR_OFFSET + 1 -> ChestSlotType
            ARMOR_OFFSET + 2 -> LegsSlotType
            ARMOR_OFFSET + 3 -> FeetSlotType
            in ARMOR_OFFSET + 3..HOTBAR_OFFSET + HOTBAR_SLOTS + 1 -> DefaultSlotType // all slots, including offhand
            else -> null
        }
    }

    override fun getSlotSwap(slot: SlotSwapContainerAction.SwapTargets): Int {
        return when (slot) {
            SlotSwapContainerAction.SwapTargets.HOTBAR_1 -> HOTBAR_OFFSET + 0
            SlotSwapContainerAction.SwapTargets.HOTBAR_2 -> HOTBAR_OFFSET + 1
            SlotSwapContainerAction.SwapTargets.HOTBAR_3 -> HOTBAR_OFFSET + 2
            SlotSwapContainerAction.SwapTargets.HOTBAR_4 -> HOTBAR_OFFSET + 3
            SlotSwapContainerAction.SwapTargets.HOTBAR_5 -> HOTBAR_OFFSET + 4
            SlotSwapContainerAction.SwapTargets.HOTBAR_6 -> HOTBAR_OFFSET + 5
            SlotSwapContainerAction.SwapTargets.HOTBAR_7 -> HOTBAR_OFFSET + 6
            SlotSwapContainerAction.SwapTargets.HOTBAR_8 -> HOTBAR_OFFSET + 7
            SlotSwapContainerAction.SwapTargets.HOTBAR_9 -> HOTBAR_OFFSET + 8

            SlotSwapContainerAction.SwapTargets.OFFHAND -> 45
        }
    }

    override fun getSection(slotId: Int): Int? {
        return when (slotId) {
            in 0..4 -> null // crafting
            in ARMOR_OFFSET..ARMOR_OFFSET + 4 -> 0 // armor
            in ARMOR_OFFSET + 5 until HOTBAR_OFFSET -> 1 // inventory
            in HOTBAR_OFFSET..HOTBAR_OFFSET + HOTBAR_SLOTS -> 2 // hotbar
            else -> null // offhand, else
        }
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
