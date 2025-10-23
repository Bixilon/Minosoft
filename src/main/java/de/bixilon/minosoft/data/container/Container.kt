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

import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantRWLock
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.container.actions.ContainerAction
import de.bixilon.minosoft.data.container.actions.types.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.sections.ContainerSection
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.transaction.ContainerTransaction
import de.bixilon.minosoft.data.container.transaction.ContainerTransactionManager
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.CloseContainerC2SP

abstract class Container(
    val session: PlaySession,
    val type: ContainerType,
    val title: ChatComponent? = null,
    val id: Int,
) {
    val lock = ReentrantRWLock()
    val items = ContainerItems(lock)
    val transactions = ContainerTransactionManager(this)
    var serverRevision = 0
    var floating: ItemStack? by observed(null)

    open val sections: Array<ContainerSection> get() = emptyArray()

    open fun getSlotType(slotId: Int): SlotType? = DefaultSlotType
    open fun getSlotSwap(slot: SlotSwapContainerAction.SwapTargets): Int? = null
    open fun readProperty(property: Int, value: Int) = Unit


    open fun getSection(slotId: Int): ContainerSection? {
        for (section in sections) {
            if (slotId !in section) continue

            return section
        }
        return null
    }

    internal open fun onOpen() = Unit
    protected open fun onClose() {
        floating = null // ToDo: They are dropped, but not in all versions
    }

    protected open fun onAdd(slotId: Int, stack: ItemStack) = true
    protected open fun onSet(slotId: Int, previous: ItemStack, next: ItemStack) = true
    protected open fun onRemove(slotId: Int, stack: ItemStack) = true

    fun close(force: Boolean = false) {
        onClose()

        if (id != PlayerInventory.CONTAINER_ID && this !is ClientContainer) {
            session.player.items.containers -= id
        }


        if (!force && session.player.items.opened === this) {
            session.player.items.opened = null
            session.connection.send(CloseContainerC2SP(id))
        }
    }


    fun add(stack: ItemStack): ContainerTransaction = lock.locked {
        val existing = ArrayList<Int>()
        var next: Int? = null
        val max = if (stack.item is StackableItem) stack.item.maxStackSize else 1

        for (section in sections) {
            for (slot in section) {
                val item = items[slot]
                if (item == null) {
                    if (next == null) {
                        next = slot
                    }
                    if (max == 1) break
                    continue
                }

                if (item.matches(stack) && max > 1) {
                    existing += slot
                }
            }
        }

        val transaction = ContainerTransaction(this)
        var left = stack.count

        for (slot in existing) {
            val existing = items[slot]!!
            val merge = minOf(left, max - existing.count)
            if (merge <= 0) break

            transaction[slot] = existing.copy(count = merge)
            left -= merge

            if (left <= 0) break
        }

        if (left > 0 && next != null) {
            transaction[next] = stack.copy(count = left)
        }

        return transaction
    }

    fun execute(action: ContainerAction) {
        val transaction = ContainerTransaction(this)
        lock.locked { action.execute(session, this, transaction) }
    }
}
