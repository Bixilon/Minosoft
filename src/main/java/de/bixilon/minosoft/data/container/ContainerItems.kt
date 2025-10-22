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

import de.bixilon.kutil.concurrent.lock.LockUtil.acquired
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantRWLock
import de.bixilon.kutil.observer.map.MapObserver.Companion.observedMap
import de.bixilon.minosoft.data.container.stack.ItemStack

class ContainerItems(
    val lock: ReentrantRWLock,
) : Iterable<Map.Entry<Int, ItemStack>> {
    val slots: MutableMap<Int, ItemStack> by observedMap(mutableMapOf())


    operator fun get(slotId: Int) = lock.acquired { slots[slotId] }
    fun remove(slotId: Int) = lock.locked { slots.remove(slotId) }
    operator fun minusAssign(slotId: Int) {
        remove(slotId)
    }

    operator fun set(slotId: Int, stack: ItemStack) = lock.locked {
        slots[slotId] = stack
    }

    @JvmName("setNull")
    operator fun set(slotId: Int, stack: ItemStack?) = lock.locked {
        if (stack == null) {
            slots -= slotId
        } else {
            slots[slotId] = stack
        }

    }

    fun clear() = lock.locked {
        slots.clear()
    }

    override fun iterator() = slots.iterator()
}
