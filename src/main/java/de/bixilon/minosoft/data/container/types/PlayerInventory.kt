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

package de.bixilon.minosoft.data.container.types

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.watcher.map.MapDataWatcher.Companion.observeMap
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.InventorySlots
import de.bixilon.minosoft.data.container.click.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.RemoveOnlySlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.slots.equipment.ChestSlotType
import de.bixilon.minosoft.data.container.slots.equipment.FeetSlotType
import de.bixilon.minosoft.data.container.slots.equipment.HeadSlotType
import de.bixilon.minosoft.data.container.slots.equipment.LegsSlotType
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.other.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

// https://c4k3.github.io/wiki.vg/images/1/13/Inventory-slots.png
class PlayerInventory(connection: PlayConnection) : Container(connection = connection, type = TYPE) {
    override val sections: Array<IntRange> get() = SECTIONS
    val equipment: LockMap<InventorySlots.EquipmentSlots, ItemStack> = lockMapOf()

    init {
        this::slots.observeMap(this) {
            for ((slotId, stack) in it.removes) {
                if (slotId - HOTBAR_OFFSET == connection.player.selectedHotbarSlot) {
                    this.equipment -= InventorySlots.EquipmentSlots.MAIN_HAND
                    continue
                }
                this.equipment -= slotId.equipmentSlot ?: continue
            }
            for ((slotId, stack) in it.adds) {
                if (slotId - HOTBAR_OFFSET == connection.player.selectedHotbarSlot) {
                    this.equipment[InventorySlots.EquipmentSlots.MAIN_HAND] = stack
                    continue
                }
                this.equipment[slotId.equipmentSlot ?: continue] = stack
            }
        }
    }


    fun getHotbarSlot(hotbarSlot: Int = connection.player.selectedHotbarSlot): ItemStack? {
        check(hotbarSlot in 0..HOTBAR_SLOTS) { "Hotbar slot out of bounds!" }
        return this[hotbarSlot + HOTBAR_OFFSET]
    }

    operator fun get(slot: InventorySlots.EquipmentSlots): ItemStack? {
        return this[slot.slot]
    }

    operator fun set(slot: InventorySlots.EquipmentSlots, itemStack: ItemStack?) {
        this[slot.slot] = itemStack
    }

    operator fun get(hand: Hands): ItemStack? {
        return this[(when (hand) {
            Hands.MAIN -> InventorySlots.EquipmentSlots.MAIN_HAND
            Hands.OFF -> InventorySlots.EquipmentSlots.OFF_HAND
        })]
    }

    @JvmName("setEquipment")
    fun set(vararg slots: Pair<InventorySlots.EquipmentSlots, ItemStack?>) {
        val realSlots: MutableList<Pair<Int, ItemStack?>> = mutableListOf()

        for ((slot, itemStack) in slots) {
            realSlots += Pair(slot.slot, itemStack)
        }

        set(*realSlots.toTypedArray())
    }

    override fun getSlotType(slotId: Int): SlotType? {
        return when (slotId) {
            0 -> RemoveOnlySlotType // crafting result
            in 1..4 -> DefaultSlotType // crafting
            ARMOR_OFFSET + 0 -> HeadSlotType
            ARMOR_OFFSET + 1 -> ChestSlotType
            ARMOR_OFFSET + 2 -> LegsSlotType
            ARMOR_OFFSET + 3 -> FeetSlotType
            in MAIN_SLOTS_START..HOTBAR_OFFSET + HOTBAR_SLOTS + 1 -> DefaultSlotType // all slots, including offhand
            else -> null
        }
    }

    override fun getSlotSwap(slot: SlotSwapContainerAction.SwapTargets): Int {
        if (slot == SlotSwapContainerAction.SwapTargets.OFFHAND) {
            return OFFHAND_SLOT
        }
        return HOTBAR_OFFSET + slot.ordinal
    }

    override fun getSection(slotId: Int): Int? {
        return when (slotId) {
            in 0..4 -> null // crafting
            in ARMOR_OFFSET..ARMOR_OFFSET + 4 -> 0 // armor
            in MAIN_SLOTS_START until HOTBAR_OFFSET -> 1 // inventory
            in HOTBAR_OFFSET..HOTBAR_OFFSET + HOTBAR_SLOTS -> 2 // hotbar
            else -> null // offhand, else
        }
    }

    val InventorySlots.EquipmentSlots.slot: Int
        get() = when (this) {
            InventorySlots.EquipmentSlots.HEAD -> ARMOR_OFFSET + 0
            InventorySlots.EquipmentSlots.CHEST -> ARMOR_OFFSET + 1
            InventorySlots.EquipmentSlots.LEGS -> ARMOR_OFFSET + 2
            InventorySlots.EquipmentSlots.FEET -> ARMOR_OFFSET + 3

            InventorySlots.EquipmentSlots.MAIN_HAND -> connection.player.selectedHotbarSlot + HOTBAR_OFFSET
            InventorySlots.EquipmentSlots.OFF_HAND -> OFFHAND_SLOT
        }


    val Int.equipmentSlot: InventorySlots.EquipmentSlots?
        get() = when (this) {
            ARMOR_OFFSET + 0 -> InventorySlots.EquipmentSlots.HEAD
            ARMOR_OFFSET + 1 -> InventorySlots.EquipmentSlots.CHEST
            ARMOR_OFFSET + 2 -> InventorySlots.EquipmentSlots.LEGS
            ARMOR_OFFSET + 3 -> InventorySlots.EquipmentSlots.FEET
            OFFHAND_SLOT -> InventorySlots.EquipmentSlots.OFF_HAND
            // ToDo: Main hand
            else -> null
        }

    companion object : ContainerFactory<PlayerInventory> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:player_inventory".toResourceLocation()
        val TYPE = ContainerType(
            resourceLocation = RESOURCE_LOCATION,
            factory = this,
        )
        const val HOTBAR_OFFSET = 36
        const val ARMOR_OFFSET = 5

        const val MAIN_SLOTS_PER_ROW = 9
        const val MAIN_ROWS = 4
        const val MAIN_SLOTS = MAIN_SLOTS_PER_ROW * MAIN_ROWS
        const val MAIN_SLOTS_START = ARMOR_OFFSET + 4

        const val HOTBAR_SLOTS = MAIN_SLOTS_PER_ROW
        const val OFFHAND_SLOT = 45

        private val SECTIONS = arrayOf(
            ARMOR_OFFSET..ARMOR_OFFSET + 4,
            ARMOR_OFFSET + 5 until HOTBAR_OFFSET,
            HOTBAR_OFFSET..HOTBAR_OFFSET + HOTBAR_SLOTS,
        )

        override fun build(connection: PlayConnection, type: ContainerType, title: ChatComponent?): PlayerInventory {
            return PlayerInventory(connection)
        }
    }
}
