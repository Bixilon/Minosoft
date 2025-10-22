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

package de.bixilon.minosoft.data.container.transaction

import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.stack.ItemStack
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class ContainerTransaction(
    val container: Container,
) {
    private val flags = ContainerTransactionFlags.set()
    private val previous = PreviousState()
    private val changes = Int2ObjectOpenHashMap<ItemStack?>()
    var state: TransactionState = TransactionState.PENDING
        private set

    var floating: ItemStack? = null
        get() {
            if (ContainerTransactionFlags.FLOATING_ITEM_CHANGED in flags) {
                return field
            }
            return container.floating
        }
        set(value) {
            assert(state == TransactionState.PENDING)
            flags += ContainerTransactionFlags.FLOATING_ITEM_CHANGED
            field = value
        }

    fun commit(): CommittedAction {
        assert(state == TransactionState.PENDING)
        val id = container.transactions.create(this)

        container.lock.locked {
            for ((slotId, next) in this.changes) {
                val previous = container.items[slotId]
                if (previous == next) continue
                this.previous.items[slotId] = previous

                container.items[slotId] = next
            }
            if (ContainerTransactionFlags.FLOATING_ITEM_CHANGED in flags) {
                previous.floating = container.floating
                container.floating = floating
            }
        }

        state = TransactionState.COMMITTED

        return CommittedAction(id, this.changes)
    }

    fun drop() {
        assert(state == TransactionState.PENDING)
        state = TransactionState.DROPPED
    }

    fun revert() {
        assert(state == TransactionState.COMMITTED)

        container.lock.locked {
            for ((slotId, previous) in this.previous.items) {
                container.items[slotId] = previous
            }
            if (ContainerTransactionFlags.FLOATING_ITEM_CHANGED in flags) {
                container.floating = previous.floating
            }
        }

        state = TransactionState.REVERTED
    }


    operator fun get(slotId: Int): ItemStack? {
        assert(state == TransactionState.PENDING)
        return this.changes.getOrDefault(slotId, container.items[slotId])
    }

    fun remove(slotId: Int) {
        assert(state == TransactionState.PENDING)
        this.changes[slotId] = null
    }

    operator fun minusAssign(slotId: Int) = remove(slotId)

    operator fun set(slotId: Int, stack: ItemStack?) {
        assert(state == TransactionState.PENDING)
        this.changes[slotId] = stack
    }


    data class CommittedAction(val id: Int, val changes: Int2ObjectMap<ItemStack?>)

    enum class TransactionState {
        PENDING,
        COMMITTED,
        DROPPED,
        REVERTED,
    }
}
