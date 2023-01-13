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

package de.bixilon.minosoft.data.container.click

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

/**
 * If you double-click on an item in an inventory, all items of the same type will be stacked together and selected
 */
class PickAllContainerAction(
    @Deprecated("packet only") val slot: Int,
) : ContainerAction {
    // ToDo: Action reverting

    override fun invoke(connection: PlayConnection, containerId: Int, container: Container) {
        // TODO (1.18.2) minecraft always sends a packet
        container.lock()
        try {
            val previous = container[slot]
            val floating = container.floatingItem?.copy()
            if (previous != null || floating == null) {
                return
            }
            var countLeft = floating.item.item.maxStackSize - floating.item.count
            val changes: Int2ObjectMap<ItemStack?> = Int2ObjectOpenHashMap()
            for ((slotId, slot) in container.slots) {
                if (!floating.matches(slot)) {
                    continue
                }
                if (container.getSlotType(slotId)?.canRemove(container, slotId, slot) != true) {
                    continue
                }
                val countToRemove = minOf(slot.item.count, countLeft)
                slot.item.count -= countToRemove
                countLeft -= countToRemove
                floating.item.count += countToRemove
                if (slot._valid) {
                    changes[slotId] = slot
                } else {
                    changes[slotId] = null
                }
                if (countLeft <= 0) {
                    break
                }
            }
            container.validate()
            if (floating == container.floatingItem) {
                // no change
                return
            }
            container.floatingItem = floating
            connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, this.slot, 6, 0, container.createAction(this), changes, floating))
        } finally {
            container.commit()
        }
    }
}
