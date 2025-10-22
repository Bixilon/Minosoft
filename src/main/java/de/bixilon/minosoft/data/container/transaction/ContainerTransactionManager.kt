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
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import java.util.concurrent.atomic.AtomicInteger

class ContainerTransactionManager(
    val container: Container,
) {
    private val transactions = Int2ObjectOpenHashMap<ContainerTransaction>(10)
    private val id = AtomicInteger(0)

    fun create(transaction: ContainerTransaction): Int {
        container.lock.lock()
        val id = this.id.getAndIncrement()

        while (transactions.size >= MAX_TRANSACTIONS) {
            transactions.iterator().remove()
        }

        transactions[id] = transaction
        container.lock.unlock()

        return id
    }

    fun clear() = container.lock.locked { this.transactions.clear() }

    fun acknowledge(id: Int) = container.lock.locked { transactions -= id }
    fun revert(id: Int) = container.lock.locked { transactions.remove(id).revert() }


    companion object {
        const val MAX_TRANSACTIONS = 30
    }
}
