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
import de.bixilon.minosoft.data.container.actions.types.PickAllContainerAction
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
class PickAllContainerActionTest {

    fun testEmpty() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.actions.invoke(PickAllContainerAction(0))
        assertEquals(container.slots, slotsOf())
        assertNull(container.floatingItem)
        connection.assertNoPacket()
    }

    fun testSingle() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(EggTest0.item, count = 7)
        container.actions.invoke(PickAllContainerAction(6))
        assertEquals(container.slots, slotsOf())
        // connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 6, 0, 0, slotsOf(), ItemStack(EggTest0.item, count = 7)))
    }

    fun test2Single() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(AppleTest0.item, 1)
        container[1] = ItemStack(AppleTest0.item, 2)
        container.actions.invoke(PickAllContainerAction(0))
        assertEquals(container.slots, slotsOf())
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 3))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 6, 0, 0, slotsOf(1 to null), ItemStack(AppleTest0.item, count = 3)))
    }

    fun testNotTaking() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(AppleTest0.item, 1)
        container[1] = ItemStack(EggTest0.item, 1)
        container[2] = ItemStack(AppleTest0.item, 2)
        container.actions.invoke(PickAllContainerAction(0))

        assertEquals(container.slots, slotsOf(1 to ItemStack(EggTest0.item, 1)))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 3))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 6, 0, 0, slotsOf(2 to null), ItemStack(AppleTest0.item, count = 3)))
    }

    fun testStack() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(AppleTest0.item, 64)
        container[5] = ItemStack(AppleTest0.item, 2)
        container.actions.invoke(PickAllContainerAction(0))
        assertEquals(container.slots, slotsOf(5 to ItemStack(AppleTest0.item, 2)))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 64))
        connection.assertNoPacket()
    }

    fun testExceeds() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTest0.item, 63)
        container.floatingItem = ItemStack(AppleTest0.item, 3)
        container.actions.invoke(PickAllContainerAction(5))
        assertEquals(container.slots, slotsOf(0 to ItemStack(AppleTest0.item, 2)))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 64))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 5, 6, 0, 0, slotsOf(0 to ItemStack(AppleTest0.item, count = 2)), ItemStack(AppleTest0.item, count = 64)))
    }

    @Test(enabled = false)
    fun testRevertSingle() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTest0.item, 7)
        container.floatingItem = ItemStack(AppleTest0.item, 9)
        val action = PickAllContainerAction(1)
        container.actions.invoke(action)
        assertEquals(container.slots, slotsOf())
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 16))
        container.actions.revert(action)
        assertEquals(container.slots, slotsOf(0 to ItemStack(AppleTest0.item, 7)))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 9))
    }

    @Test(enabled = false)
    fun testRevert2() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTest0.item, 7)
        container[1] = ItemStack(AppleTest0.item, 8)
        container.floatingItem = ItemStack(AppleTest0.item, 4)
        val action = PickAllContainerAction(2)
        container.actions.invoke(action)
        assertNull(container[0])
        assertNull(container[1])
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 19))
        container.actions.revert(action)
        assertEquals(container[0], ItemStack(AppleTest0.item, count = 7))
        assertEquals(container[1], ItemStack(AppleTest0.item, count = 8))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, 4))
    }
}
