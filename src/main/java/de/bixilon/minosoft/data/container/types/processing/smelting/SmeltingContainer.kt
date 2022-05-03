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

package de.bixilon.minosoft.data.container.types.processing.smelting

import de.bixilon.minosoft.data.container.click.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.FuelSlotType
import de.bixilon.minosoft.data.container.slots.RemoveOnlySlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.types.CraftingContainer
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.container.types.processing.ProcessingContainer
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class SmeltingContainer(connection: PlayConnection, type: ContainerType, title: ChatComponent?) : ProcessingContainer(connection, type, title) {
    var processTime: Int = 0
        private set
        get() = minOf(field, maxProcessTime)
    var maxProcessTime: Int = 200
        private set

    var fuel: Int = 0
        private set
        get() = minOf(field, maxFuel)
    var maxFuel: Int = 0
        private set


    override fun getSlotType(slotId: Int): SlotType? {
        if (slotId == 0) {
            return DefaultSlotType // ToDo: only smeltable items
        }
        if (slotId == 1) {
            return FuelSlotType
        }
        if (slotId == 2) {
            return RemoveOnlySlotType
        }
        if (slotId in SMELTING_SLOTS until +SMELTING_SLOTS + PlayerInventory.MAIN_SLOTS) {
            return DefaultSlotType
        }
        return null
    }

    override fun getSection(slotId: Int): Int? {
        if (slotId == 2) {
            return 0
        }
        if (slotId == 0 || slotId == 1) {
            return 1
        }
        if (slotId in SMELTING_SLOTS until SMELTING_SLOTS + CraftingContainer.CRAFTING_SLOTS) {
            return 2
        }
        return null
    }

    override fun getSlotSwap(slot: SlotSwapContainerAction.SwapTargets): Int? {
        if (slot == SlotSwapContainerAction.SwapTargets.OFFHAND) {
            return null // ToDo: It is possible to press F in vanilla, but there is no slot for it
        }
        return SMELTING_SLOTS + PlayerInventory.MAIN_SLOTS_PER_ROW * 3 + slot.ordinal
    }

    override fun readProperty(property: Int, value: Int) {
        when (property) {
            0 -> this.fuel = maxOf(value, 0)
            1 -> this.maxFuel = maxOf(value, 0)
            2 -> this.processTime = maxOf(value, 0)
            3 -> this.maxProcessTime = maxOf(value, 0)
            else -> super.readProperty(property, value)
        }
    }

    companion object {
        const val SMELTING_SLOTS = 3
    }
}
