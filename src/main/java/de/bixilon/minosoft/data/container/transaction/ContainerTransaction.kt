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

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.stack.ItemStack
import it.unimi.dsi.fastutil.ints.Int2ObjectMap

class ContainerTransaction(
    val container: Container,
) {
    var floating: ItemStack? = null
    var state: TransactionState = TransactionState.PENDING
        private set

    fun commit(): CommittedAction
    fun drop()
    fun revert()

    fun clear()


    operator fun get(slotId: Int): ItemStack?
    fun remove(slotId: Int): ItemStack?
    operator fun minusAssign(slotId: Int) {
        remove(slotId)
    }

    operator fun set(slotId, stack: ItemStack?)
    fun clear()


    data class CommittedAction(val id: Int, val changes: Int2ObjectMap<ItemStack?>)

    enum class TransactionState {
        PENDING,
        COMMITTED,
        DROPPED,
        REVERTED,
    }
}
