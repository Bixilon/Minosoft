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

package de.bixilon.minosoft.data.container.actions.types

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.ContainerUtil.slotsOf
import de.bixilon.minosoft.data.container.actions.ContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.transaction.ContainerTransaction
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemStackCreateC2SP

class SimpleContainerAction(
    val slot: Int?,
    val count: ContainerCounts,
) : ContainerAction {

    private fun pickItem(session: PlaySession, containerId: Int, container: Container) {
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

        container.floating = floatingItem


        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            session.connection += ItemStackCreateC2SP(slot, item)
        } else {
            session.connection += ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.actions.createId(this), slotsOf(slot to item), floatingItem)
        }
    }

    private fun dropItem(session: PlaySession, containerId: Int, container: Container, floatingItem: ItemStack) {
        // slot id is null, we are not targeting anything
        // -> drop item into the void
        val previous = floatingItem.item._count
        if (count == ContainerCounts.ALL) {
            floatingItem.item._count = 0
        } else {
            floatingItem.item._count-- // don't use decrease, item + container is already locked
        }
        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            session.connection += ItemStackCreateC2SP(-1, if (count == ContainerCounts.ALL) floatingItem.copy(count = previous) else floatingItem.copy(count = 1))
        } else {
            session.connection += ContainerClickC2SP(containerId, container.serverRevision, null, 0, count.ordinal, container.actions.createId(this), slotsOf(), null)
        }
    }

    private fun swapItems(session: PlaySession, containerId: Int, container: Container, floatingItem: ItemStack, matches: Boolean, target: ItemStack?) {
        val slot = slot ?: return

        var nextContainer: ItemStack?
        var nextFloating: ItemStack?

        if (count == ContainerCounts.ALL || (!matches && target != null)) {
            nextContainer = floatingItem
            nextFloating = target
        } else {
            floatingItem.item.count--

            nextContainer = floatingItem.copy(count = 1)
            nextFloating = floatingItem
        }

        container[slot] = nextContainer
        container.floating = nextFloating

        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            session.connection += ItemStackCreateC2SP(slot, nextContainer)
        } else {
            session.connection += ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.actions.createId(this), slotsOf(slot to nextContainer), nextFloating)
        }
    }

    private fun putItem(session: PlaySession, containerId: Int, container: Container, floatingItem: ItemStack) {
        if (slot == null) {
            dropItem(session, containerId, container, floatingItem)
            return
        }
        val target = container[slot]
        val slotType = container.getSlotType(slot)
        val matches = floatingItem.matches(target)

        if (target != null && matches) {
            // we can remove or merge the item
            if (slotType?.canPut(container, slot, floatingItem) == true) {
                // merge
                val item = target.item.item
                val maxStackSize = if (item is StackableItem) item.maxStackSize else 1
                val subtract = if (count == ContainerCounts.ALL) minOf(maxStackSize - target.item.count, floatingItem.item.count) else 1
                if (subtract == 0 || target.item.count + subtract > maxStackSize) {
                    return
                }
                target.item.count += subtract
                floatingItem.item.count -= subtract
            } else if (slotType?.canRemove(container, slot, floatingItem) == true) {
                // remove only (e.g. crafting result)
                // ToDo: respect count (part or all)
                val subtract = minOf((if (floatingItem.item.item is StackableItem) floatingItem.item.item.maxStackSize else 1) - floatingItem.item.count, target.item.count)
                if (subtract == 0) {
                    return
                }
                target.item.count -= subtract
                floatingItem.item.count += subtract
            }

            if (floatingItem.valid) {
                container.floating = floatingItem
            } else {
                container.floating = null
            }

            if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
                session.connection += ItemStackCreateC2SP(slot, target)
            } else {
                session.connection.send(ContainerClickC2SP(containerId, container.serverRevision, slot, 0, count.ordinal, container.actions.createId(this), slotsOf(slot to target), container.floating))
            }

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

        swapItems(session, containerId, container, floatingItem, matches, target)
    }

    override fun invoke(session: PlaySession, containerId: Int, container: Container, transaction: ContainerTransaction) {
        try {
            container.lock()
            val floatingItem = container.floating?.copy()
            if (floatingItem == null) {
                pickItem(session, containerId, container)
            } else {
                putItem(session, containerId, container, floatingItem)
            }
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
