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

package de.bixilon.minosoft.data.container

import de.bixilon.minosoft.data.container.sections.RangeSection
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.other.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class InventorySynchronizedContainer(
    connection: PlayConnection,
    type: ContainerType,
    title: ChatComponent? = null,
    protected var synchronizedSlots: RangeSection,
    protected var inventorySlots: RangeSection = RangeSection(PlayerInventory.MAIN_SLOTS_START, PlayerInventory.MAIN_SLOTS),
) : Container(connection, type, title) {
    private val playerInventory = connection.player.inventory

    init {
        check(synchronizedSlots.count == inventorySlots.count) { "Synchronized inventory slots must have the same size!" }
        // ToDo: Add initial slots from inventory
    }

    override fun _remove(slotId: Int): ItemStack? {
        val slot = super._remove(slotId) ?: return null
        if (slotId in synchronizedSlots) {
            playerInventory._remove(slotId - synchronizedSlots.first + inventorySlots.first)
        }

        return slot
    }

    override fun _set(slotId: Int, itemStack: ItemStack?): Boolean {
        val changed = super._set(slotId, itemStack)
        if (slotId in synchronizedSlots) {
            playerInventory._set(slotId - synchronizedSlots.first + inventorySlots.first, itemStack)
        }

        return changed
    }
}
