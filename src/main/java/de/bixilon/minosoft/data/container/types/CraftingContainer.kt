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

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.click.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.RemoveOnlySlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.other.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class CraftingContainer(connection: PlayConnection, type: ContainerType, title: ChatComponent?) : Container(connection, type, title) {
    override val sections: Array<IntRange> = arrayOf(0..0, 1 until 1 + CRAFTING_SLOTS, 1 + CRAFTING_SLOTS until 1 + CRAFTING_SLOTS + SLOTS_PER_ROW * ROWS)

    override fun getSlotType(slotId: Int): SlotType? {
        if (slotId == 0) {
            return RemoveOnlySlotType
        }
        if (slotId in 1 until 1 + CRAFTING_SLOTS + SLOTS_PER_ROW * ROWS) {
            return DefaultSlotType
        }
        return null
    }

    override fun getSection(slotId: Int): Int? {
        if (slotId == 0) {
            return 0
        }
        if (slotId in 1 until 1 + CRAFTING_SLOTS) {
            return 1
        }
        if (slotId in 1 + CRAFTING_SLOTS until 1 + CRAFTING_SLOTS + SLOTS_PER_ROW * ROWS) {
            return 2
        }
        return null
    }

    override fun getSlotSwap(slot: SlotSwapContainerAction.SwapTargets): Int? {
        if (slot == SlotSwapContainerAction.SwapTargets.OFFHAND) {
            return null // ToDo: It is possible to press F in vanilla, but there is no slot for it
        }
        return 1 + CRAFTING_SLOTS + SLOTS_PER_ROW * 3 + slot.ordinal
    }


    companion object : ContainerFactory<CraftingContainer> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:crafting".toResourceLocation()
        const val CRAFTING_SLOTS = 3 * 3
        const val SLOTS_PER_ROW = 9
        const val ROWS = 4

        override fun build(connection: PlayConnection, type: ContainerType, title: ChatComponent?): CraftingContainer {
            return CraftingContainer(connection, type, title)
        }
    }
}
