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

package de.bixilon.minosoft.data.container.actions

import de.bixilon.minosoft.data.container.ContainerTestUtil.createContainer
import de.bixilon.minosoft.data.container.ContainerUtil.slotsOf
import de.bixilon.minosoft.data.container.actions.types.SimpleContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.items.AppleTest0
import de.bixilon.minosoft.data.registries.items.EggTest0
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["container"], dependsOnGroups = ["block", "item", "item_stack"])
class SimpleContainerActionTest {

    fun testEmpty() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.actions.invoke(SimpleContainerAction(0, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf())
        assertNull(container.floatingItem)
        // connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 0, 0, 0, slotsOf(), null))
        connection.assertNoPacket()
    }

    fun testPutAll() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(AppleTest0.item, count = 7)
        container.actions.invoke(SimpleContainerAction(0, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf(0 to ItemStack(AppleTest0.item, count = 7)))
        assertNull(container.floatingItem)
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 0, 0, 0, slotsOf(0 to ItemStack(AppleTest0.item, count = 7)), null))
    }

    fun testPutOne() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(AppleTest0.item, count = 7)
        container.actions.invoke(SimpleContainerAction(0, SimpleContainerAction.ContainerCounts.PART))
        assertEquals(container.slots, slotsOf(0 to ItemStack(AppleTest0.item, count = 1)))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 6))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 0, 1, 0, slotsOf(0 to ItemStack(AppleTest0.item, count = 1)), ItemStack(AppleTest0.item, count = 6)))
    }

    fun testPutAlreadyAll() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(AppleTest0.item, count = 7)
        container[8] = ItemStack(AppleTest0.item, count = 2)
        container.actions.invoke(SimpleContainerAction(8, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf(8 to ItemStack(AppleTest0.item, count = 9)))
        assertNull(container.floatingItem)
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 8, 0, 0, 0, slotsOf(8 to ItemStack(AppleTest0.item, count = 9)), null))
    }

    fun testPutAlreadySingle() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(AppleTest0.item, count = 7)
        container[12] = ItemStack(AppleTest0.item, count = 3)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.PART))
        assertEquals(container.slots, slotsOf(12 to ItemStack(AppleTest0.item, count = 4)))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 6))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 1, 0, slotsOf(12 to ItemStack(AppleTest0.item, count = 4)), ItemStack(AppleTest0.item, count = 6)))
    }

    fun testPutExceeds() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(EggTest0.item, count = 14)
        container[12] = ItemStack(EggTest0.item, count = 15)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf(12 to ItemStack(EggTest0.item, count = 16)))
        assertEquals(container.floatingItem, ItemStack(EggTest0.item, 13))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 0, 0, slotsOf(12 to ItemStack(EggTest0.item, count = 16)), ItemStack(EggTest0.item, count = 13)))
    }

    fun testRemoveAll() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[12] = ItemStack(AppleTest0.item, count = 3)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf())
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 3))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 0, 0, slotsOf(12 to null), ItemStack(AppleTest0.item, count = 3)))
    }

    fun testRemoveHalf() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[12] = ItemStack(AppleTest0.item, count = 8)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.PART))
        assertEquals(container.slots, slotsOf(12 to ItemStack(AppleTest0.item, 4)))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 4))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 1, 0, slotsOf(12 to ItemStack(AppleTest0.item, 4)), ItemStack(AppleTest0.item, count = 4)))
    }

    fun testRemoveHalfOdd() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[12] = ItemStack(AppleTest0.item, count = 9)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.PART))
        assertEquals(container.slots, slotsOf(12 to ItemStack(AppleTest0.item, 4)))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 5))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 1, 0, slotsOf(12 to ItemStack(AppleTest0.item, 4)), ItemStack(AppleTest0.item, count = 5)))
    }

    // TODO: mixing types, revert
}
