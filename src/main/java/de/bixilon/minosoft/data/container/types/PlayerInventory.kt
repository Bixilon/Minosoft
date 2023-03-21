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

package de.bixilon.minosoft.data.container.types

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.observer.map.MapObserver.Companion.observeMap
import de.bixilon.minosoft.data.container.ClientContainer
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.actions.types.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.equipment.EquipmentSlots
import de.bixilon.minosoft.data.container.sections.ContainerSection
import de.bixilon.minosoft.data.container.sections.HotbarSection
import de.bixilon.minosoft.data.container.sections.PassiveInventorySection
import de.bixilon.minosoft.data.container.sections.RangeSection
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.RemoveOnlySlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.slots.equipment.ChestSlotType
import de.bixilon.minosoft.data.container.slots.equipment.FeetSlotType
import de.bixilon.minosoft.data.container.slots.equipment.HeadSlotType
import de.bixilon.minosoft.data.container.slots.equipment.LegsSlotType
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.entities.entities.player.local.PlayerItemManager
import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.ClientActionC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_8_9
import de.bixilon.minosoft.util.KUtil.toResourceLocation

// https://c4k3.github.io/wiki.vg/images/1/13/Inventory-slots.png
class PlayerInventory(
    private val items: PlayerItemManager,
    connection: PlayConnection,
) : Container(connection = connection, type = TYPE), ClientContainer {
    override val sections: Array<ContainerSection> get() = SECTIONS
    val equipment: LockMap<EquipmentSlots, ItemStack> = lockMapOf()

    init {
        this::slots.observeMap(this) {
            for ((slotId, stack) in it.removes) {
                if (slotId - HOTBAR_OFFSET == items.hotbar) {
                    this.equipment -= EquipmentSlots.MAIN_HAND
                    continue
                }
                this.equipment -= slotId.equipmentSlot ?: continue
            }
            for ((slotId, stack) in it.adds) {
                if (slotId - HOTBAR_OFFSET == items.hotbar) {
                    this.equipment[EquipmentSlots.MAIN_HAND] = stack
                    continue
                }
                this.equipment[slotId.equipmentSlot ?: continue] = stack
            }
        }
    }


    fun getHotbarSlot(hotbarSlot: Int = items.hotbar): ItemStack? {
        check(hotbarSlot in 0..HOTBAR_SLOTS) { "Hotbar slot out of bounds!" }
        return this[hotbarSlot + HOTBAR_OFFSET]
    }

    operator fun get(slot: EquipmentSlots): ItemStack? {
        return this[slot.slot]
    }

    operator fun set(slot: EquipmentSlots, stack: ItemStack?) {
        this[slot.slot] = stack
    }

    operator fun get(hand: Hands): ItemStack? {
        return this[(when (hand) {
            Hands.MAIN -> EquipmentSlots.MAIN_HAND
            Hands.OFF -> EquipmentSlots.OFF_HAND
        })]
    }

    @JvmName("setEquipment")
    fun set(vararg slots: Pair<EquipmentSlots, ItemStack?>) {
        val realSlots: MutableList<Pair<Int, ItemStack?>> = mutableListOf()

        for ((slot, stack) in slots) {
            realSlots += Pair(slot.slot, stack)
        }

        set(*realSlots.toTypedArray())
    }

    override fun onOpen() {
        if (connection.version <= V_1_8_9) { // TODO: find out version
            connection.network.send(ClientActionC2SP(ClientActionC2SP.ClientActions.OPEN_INVENTORY))
        }
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

    val EquipmentSlots.slot: Int
        get() = when (this) {
            EquipmentSlots.HEAD -> ARMOR_OFFSET + 0
            EquipmentSlots.CHEST -> ARMOR_OFFSET + 1
            EquipmentSlots.LEGS -> ARMOR_OFFSET + 2
            EquipmentSlots.FEET -> ARMOR_OFFSET + 3

            EquipmentSlots.MAIN_HAND -> items.hotbar + HOTBAR_OFFSET
            EquipmentSlots.OFF_HAND -> OFFHAND_SLOT
        }


    val Int.equipmentSlot: EquipmentSlots?
        get() = when (this) {
            ARMOR_OFFSET + 0 -> EquipmentSlots.HEAD
            ARMOR_OFFSET + 1 -> EquipmentSlots.CHEST
            ARMOR_OFFSET + 2 -> EquipmentSlots.LEGS
            ARMOR_OFFSET + 3 -> EquipmentSlots.FEET
            OFFHAND_SLOT -> EquipmentSlots.OFF_HAND
            // ToDo: Main hand
            else -> null
        }

    companion object : ContainerFactory<PlayerInventory> {
        const val CONTAINER_ID = 0
        override val identifier: ResourceLocation = "minecraft:player_inventory".toResourceLocation()
        val TYPE = ContainerType(
            identifier = identifier,
            factory = this,
        )
        const val HOTBAR_OFFSET = 36
        const val ARMOR_OFFSET = 5

        const val MAIN_SLOTS_PER_ROW = 9
        const val MAIN_ROWS = 4
        const val PASSIVE_SLOTS = MAIN_SLOTS_PER_ROW * (MAIN_ROWS - 1)
        const val MAIN_SLOTS = MAIN_SLOTS_PER_ROW * MAIN_ROWS
        const val MAIN_SLOTS_START = ARMOR_OFFSET + 4

        const val HOTBAR_SLOTS = MAIN_SLOTS_PER_ROW
        const val OFFHAND_SLOT = 45

        private val SECTIONS = arrayOf<ContainerSection>(
            RangeSection(ARMOR_OFFSET, 4),
            PassiveInventorySection(MAIN_SLOTS_START, false),
            HotbarSection(HOTBAR_OFFSET, false),
        )

        override fun build(connection: PlayConnection, type: ContainerType, title: ChatComponent?): PlayerInventory {
            Broken("Can not create player inventory!")
        }
    }
}
