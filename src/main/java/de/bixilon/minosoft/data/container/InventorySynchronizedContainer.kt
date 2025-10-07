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

package de.bixilon.minosoft.data.container

import de.bixilon.minosoft.data.container.sections.RangeSection
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class InventorySynchronizedContainer(
    session: PlaySession,
    type: ContainerType,
    title: ChatComponent? = null,
    protected var synchronizedSlots: RangeSection,
    protected var inventorySlots: RangeSection = RangeSection(PlayerInventory.MAIN_SLOTS_START, PlayerInventory.MAIN_SLOTS),
) : Container(session, type, title) {
    private val playerInventory = session.player.items.inventory

    init {
        check(synchronizedSlots.count == inventorySlots.count) { "Synchronized inventory slots must have the same size!" }
        // ToDo: Add initial slots from inventory
    }

    override fun onRemove(slotId: Int, stack: ItemStack): Boolean {
        if (slotId in synchronizedSlots) {
            playerInventory.slots -= slotId - synchronizedSlots.first + inventorySlots.first
        }
        return super.onRemove(slotId, stack)
    }

    override fun onSet(slotId: Int, previous: ItemStack, next: ItemStack): Boolean {
        if (slotId in synchronizedSlots) {
            playerInventory.slots[slotId - synchronizedSlots.first + inventorySlots.first] = next
        }
        return super.onSet(slotId, previous, next)
    }

    override fun onAdd(slotId: Int, stack: ItemStack): Boolean {
        if (slotId in synchronizedSlots) {
            playerInventory.slots[slotId - synchronizedSlots.first + inventorySlots.first] = stack
        }
        return super.onAdd(slotId, stack, slotId, stack)
    }
}
