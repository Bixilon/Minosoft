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

package de.bixilon.minosoft.data.container.locking

import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.container.ContainerTestUtil.createContainer
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.items.AppleTestO
import de.bixilon.minosoft.data.registries.items.CoalTest0
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["container"], dependsOnGroups = ["item"])
class ContainerLockingTest {

    fun verifyRevisionBulk() {
        val container = createContainer()

        assertEquals(container.revision, 0L)
        container.lock()

        container.clear()
        container[0] = ItemStack(AppleTestO.item, count = 15)
        container[2] = ItemStack(CoalTest0.item)

        assertEquals(container.revision, 0L)
        container.commit()
        assertEquals(container.revision, 1L)
        assertEquals(container[0], ItemStack(AppleTestO.item, count = 15))
    }

    fun verifyNoChange() {
        val container = createContainer()

        container.lock()
        container.commit()
        assertEquals(container.revision, 0L)
    }

    fun verifyRevisionSingle() {
        val container = createContainer()

        assertEquals(container.revision, 0L)

        container[0] = ItemStack(AppleTestO.item, count = 15)

        assertEquals(container.revision, 1L)
        assertNotNull(container[0])
        container[0]!!.item.decreaseCount()
        assertEquals(container.revision, 2L)
        assertEquals(container[0], ItemStack(AppleTestO.item, count = 16))
    }

    fun bulkEdit() {
        val container = createContainer()

        assertEquals(container.revision, 0L)
        container.lock()

        container[0] = ItemStack(AppleTestO.item, count = 15)

        assertNotNull(container[0])
        container[0]!!.item.decreaseCount()
        assertEquals(container[0]!!.item.count, 14)

        assertEquals(container.revision, 0L)
        container.commit()
        assertEquals(container.revision, 1L)
    }

    fun invalidateItem() {
        val container = createContainer()

        container.lock()

        container[0] = ItemStack(AppleTestO.item, count = 1)

        assertNotNull(container[0])
        container[0]!!.item.decreaseCount()
        assertNull(container[0])
        container.commit()
    }

    fun bulkEditSingleItem() {
        val container = createContainer()

        container[0] = ItemStack(AppleTestO.item, count = 1)

        container[0]!!.lock()
        container[0]!!.item.increaseCount()
        container[0]!!.enchanting.repairCost = 123
        container[0]!!.commit()
    }

    fun ensureLockedBulk() {
        val container = createContainer()

        container.lock()
        container[0] = ItemStack(AppleTestO.item, count = 1)
        var end = 0L
        Thread { container[1] = ItemStack(AppleTestO.item); end = TimeUtil.nanos() }.start()
        Thread.sleep(30L)
        assertEquals(end, 0L)
        container.commit()
        Thread.sleep(30L)
        assertNotEquals(end, 0L)
    }

    fun ensureLockedSingle() {
        val container = createContainer()

        container[0] = ItemStack(AppleTestO.item, count = 1)
        container[0]!!.lock()

        var end = 0L
        Thread { container[0]!!.item.increaseCount(); end = TimeUtil.nanos() }.start()
        Thread.sleep(30L)
        assertEquals(end, 0L)
        container[0]!!.commit()
        Thread.sleep(30L)
        assertNotEquals(end, 0L)
    }

    fun iterating() {
        val container = createContainer()

        container[0] = ItemStack(AppleTestO.item, count = 1)

        container.lock.acquire()
        var slots = 0
        for ((slotId, stack) in container) {
            if (slotId == 0) {
                assertEquals(stack, ItemStack(AppleTestO.item, count = 1))
            }
            slots++
        }
        assertEquals(slots, 1)

        container.lock.release()
    }
}
