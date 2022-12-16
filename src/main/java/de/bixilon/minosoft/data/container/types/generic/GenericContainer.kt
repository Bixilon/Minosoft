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

package de.bixilon.minosoft.data.container.types.generic

import de.bixilon.minosoft.data.container.InventorySynchronizedContainer
import de.bixilon.minosoft.data.container.click.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.sections.ContainerSection
import de.bixilon.minosoft.data.container.sections.HotbarSection
import de.bixilon.minosoft.data.container.sections.PassiveInventorySection
import de.bixilon.minosoft.data.container.sections.RangeSection
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class GenericContainer(
    val rows: Int,
    connection: PlayConnection,
    type: ContainerType,
    title: ChatComponent?,
) : InventorySynchronizedContainer(connection, type, title, RangeSection(rows * SLOTS_PER_ROW, PlayerInventory.MAIN_SLOTS)) {
    override val sections: Array<ContainerSection> = arrayOf(
        RangeSection(0, rows * SLOTS_PER_ROW),
        HotbarSection(rows * SLOTS_PER_ROW + PlayerInventory.PASSIVE_SLOTS),
        PassiveInventorySection(rows * SLOTS_PER_ROW),
    )

    override fun getSlotType(slotId: Int): SlotType? {
        if (slotId in 0 until rows * SLOTS_PER_ROW + PlayerInventory.MAIN_SLOTS) {
            return DefaultSlotType
        }
        return null
    }

    override fun getSlotSwap(slot: SlotSwapContainerAction.SwapTargets): Int? {
        if (slot == SlotSwapContainerAction.SwapTargets.OFFHAND) {
            return null // ToDo: It is possible to press F in vanilla, but there is no slot for it
        }
        return rows * SLOTS_PER_ROW + 3 * SLOTS_PER_ROW + slot.ordinal // rows, inventory
    }


    companion object {
        const val SLOTS_PER_ROW = 9
    }
}
