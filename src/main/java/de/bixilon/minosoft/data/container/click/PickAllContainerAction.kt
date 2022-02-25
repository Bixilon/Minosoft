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

package de.bixilon.minosoft.data.container.click

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP

/**
 * If you double-click on an item in an inventory, all items of the same type will be stacked together and selected
 */
class PickAllContainerAction(
    val slot: Int,
) : ContainerAction {

    override fun invoke(connection: PlayConnection, containerId: Int, container: Container) {
        container.lock.lock()
        try {

            val clicked = container.slots[slot] ?: return
            if (container.getSlotType(slot)?.canRemove(container, slot, clicked) != true) {
                return
            }
            container.slots.remove(slot)
            var countLeft = clicked.item.item.maxStackSize - clicked.item._count
            val changes: MutableMap<Int, ItemStack?> = mutableMapOf()
            for ((slotId, slot) in container.slots) {
                if (!slot.matches(slot)) {
                    continue
                }
                if (container.getSlotType(slotId)?.canRemove(container, slotId, slot) != true) {
                    continue
                }
                val countToRemove = minOf(slot.item._count, countLeft)
                slot.item._count = countToRemove
                countLeft -= countToRemove
                slot.item._count += countToRemove
                changes[slotId] = slot
                if (countLeft <= 0) {
                    break
                }
            }
            container._validate()
            container.floatingItem = clicked
            connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, this.slot, 6, 0, container.createAction(this), changes, clicked))
        } finally {
            container.commit()
        }
    }
}
