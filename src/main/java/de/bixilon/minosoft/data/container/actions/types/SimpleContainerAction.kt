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

package de.bixilon.minosoft.data.container.actions.types

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.ContainerUtil.slotsOf
import de.bixilon.minosoft.data.container.actions.ContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP

class SimpleContainerAction(
    val slot: Int?,
    val count: ContainerCounts,
) : ContainerAction {
    // ToDo: Action reverting

    private fun pickItem(connection: PlayConnection, containerId: Int, container: Container) {
        val item = container.slots[slot ?: return] ?: return
        if (container.getSlotType(slot)?.canRemove(container, slot, item) != true) {
            return
        }
        // ToDo: Check course of binding
        val previous = item.copy()
        val floatingItem: ItemStack
        if (count == ContainerCounts.ALL) {
            floatingItem = item.copy()
            item.item.count = 0
            container[slot] = null
        } else {
            // half
            val stayCount = item.item.count / 2
            item.item.count = stayCount
            floatingItem = previous.copy(count = previous.item.count - stayCount)
        }

        container.floatingItem = floatingItem
        connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.actions.createId(this), slotsOf(slot to item), floatingItem))
    }

    private fun putItem(connection: PlayConnection, containerId: Int, container: Container, floatingItem: ItemStack) {
        if (slot == null) {
            // slot id is null, we are not targeting anything
            // -> drop item into the void
            if (count == ContainerCounts.ALL) {
                floatingItem.item._count = 0
            } else {
                floatingItem.item._count-- // don't use decrease, item + container is already locked
            }
            return connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, null, 0, count.ordinal, container.actions.createId(this), slotsOf(), null))
        }
        var target = container[slot]
        val slotType = container.getSlotType(slot)
        val matches = floatingItem.matches(target)

        if (target != null && matches) {
            // we can remove or merge the item
            if (slotType?.canPut(container, slot, floatingItem) == true) {
                // merge
                val subtract = if (count == ContainerCounts.ALL) minOf(target.item.item.maxStackSize - target.item.count, floatingItem.item.count) else 1
                if (subtract == 0 || target.item.count + subtract > target.item.item.maxStackSize) {
                    return
                }
                target.item.count += subtract
                floatingItem.item.count -= subtract
            } else if (slotType?.canRemove(container, slot, floatingItem) == true) {
                // remove only (e.g. crafting result)
                // ToDo: respect count (part or all)
                val subtract = minOf(floatingItem.item.item.maxStackSize - floatingItem.item.count, target.item.count)
                if (subtract == 0) {
                    return
                }
                target.item.count -= subtract
                floatingItem.item.count += subtract
            }

            if (floatingItem._valid) {
                container.floatingItem = floatingItem
            } else {
                container.floatingItem = null
            }
            connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.actions.createId(this), slotsOf(slot to target), container.floatingItem))
            return
        }
        if (target != null && slotType?.canRemove(container, slot, target) != true) {
            // we can not remove the item from the slot, cancelling
            return
        }

        if (slotType?.canPut(container, slot, floatingItem) != true) {
            // when can not put any item in there, cancel
            return
        }
        // swap
        if (count == ContainerCounts.ALL || (!matches && target != null)) {
            container.floatingItem = target
            container[slot] = floatingItem
            connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.actions.createId(this), slotsOf(slot to floatingItem), target))
        } else {
            floatingItem.item.count--
            container.floatingItem = floatingItem
            target = floatingItem.copy(count = 1)
            container[slot] = target
            connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.actions.createId(this), slotsOf(slot to target), floatingItem))
        }
    }

    override fun invoke(connection: PlayConnection, containerId: Int, container: Container) {
        try {
            container.lock()
            val floatingItem = container.floatingItem?.copy() ?: return pickItem(connection, containerId, container)
            return putItem(connection, containerId, container, floatingItem)
        } finally {
            container.commit()
        }
    }

    enum class ContainerCounts {
        ALL,
        PART,
        ;
    }
}
