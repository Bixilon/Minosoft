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

class SimpleContainerAction(
    val slot: Int?,
    val count: ContainerCounts,
) : ContainerAction {
    // ToDo: Action reverting

    private fun pickItem(connection: PlayConnection, containerId: Int, container: Container) {
        val item = container[slot ?: return] ?: return
        if (container.getSlotType(slot)?.canRemove(container, slot, item) != true) {
            return
        }
        // ToDo: Check course of binding
        val previous = item.copy()
        val floatingItem: ItemStack
        if (count == ContainerCounts.ALL) {
            floatingItem = item.copy()
            item.item.count = 0
            container.remove(slot)
        } else {
            // half
            val stayCount = item.item.count / 2
            item.item.count = stayCount
            floatingItem = previous.copy(count = previous.item.count - stayCount)
        }
        container.floatingItem = floatingItem
        connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.createAction(this), mapOf(slot to item), previous))
    }

    private fun putItem(connection: PlayConnection, containerId: Int, container: Container, floatingItem: ItemStack) {
        floatingItem.lock()
        val target = container.slots[slot]
        try {
            if (slot == null) {
                if (count == ContainerCounts.ALL) {
                    floatingItem.item._count = 0
                } else {
                    floatingItem.item._count-- // don't use decrease, item + container is already locked
                }
                return connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, null, 0, count.ordinal, container.createAction(this), mapOf(), null))
            }
            val slotType = container.getSlotType(slot)
            val matches = floatingItem.matches(target)

            if (target != null && matches) {
                if (slotType?.canPut(container, slot, floatingItem) == true) {
                    // merge
                    val subtract = if (count == ContainerCounts.ALL) minOf(target.item.item.maxStackSize - target.item._count, floatingItem.item._count) else 1
                    if (subtract == 0 || target.item._count + subtract > target.item.item.maxStackSize) {
                        return
                    }
                    target.item._count += subtract
                    floatingItem.item._count -= subtract
                } else if (slotType?.canRemove(container, slot, floatingItem) == true) {
                    // remove only (e.g. crafting result)
                    // ToDo: respect count (part or all)
                    val subtract = minOf(floatingItem.item.item.maxStackSize - floatingItem.item._count, target.item._count)
                    if (subtract == 0) {
                        return
                    }
                    target.item._count -= subtract
                    floatingItem.item._count += subtract
                }

                connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.createAction(this), mapOf(slot to target), target))
                return
            }
            if (target != null && slotType?.canRemove(container, slot, target) != true) {
                return
            }
            if (slotType?.canPut(container, slot, floatingItem) != true) {
                return
            }
            // swap
            if (count == ContainerCounts.ALL || (!matches && target != null)) {
                container.floatingItem = target
                container._set(slot, floatingItem)
            } else {
                floatingItem.item._count--
                container._set(slot, floatingItem.copy(count = 1))
            }
            connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.createAction(this), mapOf(slot to floatingItem), target))
        } finally {
            floatingItem.commit()
            target?.lock() // lock to prevent exception
            target?.commit()
        }
    }

    override fun invoke(connection: PlayConnection, containerId: Int, container: Container) {
        val floatingItem = container.floatingItem ?: return pickItem(connection, containerId, container)
        return putItem(connection, containerId, container, floatingItem)
    }

    enum class ContainerCounts {
        ALL,
        PART,
        ;
    }
}
