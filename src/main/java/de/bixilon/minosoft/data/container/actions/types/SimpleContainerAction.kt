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
    val slot: Int,
    val count: ContainerCounts,
) : ContainerAction {

    private fun pickItem(session: PlaySession, container: Container, transaction: ContainerTransaction) {
        val previous = container.slots[slot] ?: return
        if (container.getSlotType(slot)?.canRemove(container, slot, previous) != true) {
            return
        }
        val next: ItemStack?
        // ToDo: Check course of binding
        if (count == ContainerCounts.ALL) {
            transaction.floating = previous
            next = null
        } else {
            // half
            next = previous.copy(count = previous.count / 2)
            transaction.floating = previous.copy(count = previous.count - next.count)
        }
        transaction[slot] = next


        val (id, changes) = transaction.commit()
        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            session.connection += ItemStackCreateC2SP(slot, next)
        } else {
            session.connection += ContainerClickC2SP(container.id, container.serverRevision, slot, 0, count.ordinal, id, changes, transaction.floating)
        }
    }

    private fun swapItems(session: PlaySession, container: Container, floating: ItemStack, matches: Boolean, target: ItemStack?, transaction: ContainerTransaction) {
        val slot = slot

        val nextContainer: ItemStack?
        val nextFloating: ItemStack?

        if (count == ContainerCounts.ALL || (!matches && target != null)) {
            nextContainer = floating
            nextFloating = target
        } else {
            floating.count--

            nextContainer = floating.copy(count = 1)
            nextFloating = floating
        }

        container[slot] = nextContainer
        container.floating = nextFloating

        val (id, changes) = transaction.commit()
        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            session.connection += ItemStackCreateC2SP(slot, nextContainer)
        } else {
            session.connection += ContainerClickC2SP(container.id, container.serverRevision, slot, 0, count.ordinal, id, changes, nextFloating)
        }
    }

    private fun merge(session: PlaySession, container: Container, floating: ItemStack, transaction: ContainerTransaction) {
        val target = container[slot] ?: return
        val slotType = container.getSlotType(slot)

        if (slotType?.canPut(container, slot, floating) == true) {
            // merge
            val item = target.item
            val maxStackSize = if (item is StackableItem) item.maxStackSize else 1
            val subtract = if (count == ContainerCounts.ALL) minOf(maxStackSize - target.count, floating.count) else 1
            if (subtract == 0 || target.count + subtract > maxStackSize) return
            target.count += subtract
            floating.count -= subtract
        } else if (slotType?.canRemove(container, slot, floating) == true) {
            // remove only (e.g. crafting result)
            // ToDo: respect count (part or all)
            val subtract = minOf((if (floating.item is StackableItem) floating.item.maxStackSize else 1) - floating.count, target.count)
            if (subtract == 0) return

            target.count -= subtract
            floating.count += subtract
        }

        val (id, changes) = transaction.commit()
        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            session.connection += ItemStackCreateC2SP(slot, target)
        } else {
            session.connection.send(ContainerClickC2SP(container.id, container.serverRevision, slot, 0, count.ordinal, id, changes, container.floating))
        }
    }


    private fun putItem(session: PlaySession, container: Container, floatingItem: ItemStack, transaction: ContainerTransaction) {
        val target = container[slot]
        val matches = floatingItem.matches(target)

        if (target != null && matches) {
            merge(session, container, floatingItem, transaction)
            return
        }
        val slotType = container.getSlotType(slot)
        if (target != null && slotType?.canRemove(container, slot, target) != true) {
            return
        }

        if (slotType?.canPut(container, slot, floatingItem) != true) {
            return
        }

        swapItems(session, container, floatingItem, matches, target, transaction)
    }

    override fun invoke(session: PlaySession, container: Container, transaction: ContainerTransaction) {
        val floating = container.floating

        if (floating == null) {
            pickItem(session, container, transaction)
        } else {
            putItem(session, container, floating, transaction)
        }
    }

    enum class ContainerCounts {
        ALL,
        PART,
        ;
    }
}
