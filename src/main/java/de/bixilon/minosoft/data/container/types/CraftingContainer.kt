/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.minosoft.data.container.InventorySynchronizedContainer
import de.bixilon.minosoft.data.container.actions.types.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.sections.ContainerSection
import de.bixilon.minosoft.data.container.sections.HotbarSection
import de.bixilon.minosoft.data.container.sections.PassiveInventorySection
import de.bixilon.minosoft.data.container.sections.RangeSection
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.RemoveOnlySlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class CraftingContainer(session: PlaySession, type: ContainerType, title: ChatComponent?, id: Int) : InventorySynchronizedContainer(session, type, title, RangeSection(CRAFTING_SLOTS + 1, PlayerInventory.MAIN_SLOTS), id = id) {
    override val sections: Array<ContainerSection> get() = SECTIONS

    override fun getSlotType(slotId: Int): SlotType? {
        if (slotId == 0) {
            return RemoveOnlySlotType
        }
        if (slotId in 1 until 1 + CRAFTING_SLOTS + PlayerInventory.MAIN_SLOTS) {
            return DefaultSlotType
        }
        return null
    }

    override fun getSlotSwap(slot: SlotSwapContainerAction.SwapTargets): Int? {
        if (slot == SlotSwapContainerAction.SwapTargets.OFFHAND) {
            return null // ToDo: It is possible to press F in vanilla, but there is no slot for it
        }
        return 1 + CRAFTING_SLOTS + PlayerInventory.MAIN_SLOTS_PER_ROW * 3 + slot.ordinal
    }


    companion object : ContainerFactory<CraftingContainer> {
        override val identifier = minecraft("crafting")
        const val CRAFTING_SLOTS = 3 * 3
        val SECTIONS: Array<ContainerSection> = arrayOf(
            // crafting slots are not shift clickable, no section
            HotbarSection(CRAFTING_SLOTS + 1 + PlayerInventory.PASSIVE_SLOTS),
            PassiveInventorySection(CRAFTING_SLOTS + 1),
        )


        override fun build(session: PlaySession, type: ContainerType, title: ChatComponent?, slots: Int, id: Int): CraftingContainer {
            return CraftingContainer(session, type, title, id)
        }
    }
}
