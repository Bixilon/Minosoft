/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container.click

import de.bixilon.minosoft.data.container.click.ContainerTestUtil.createContainer
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.items.AppleTestO
import de.bixilon.minosoft.data.registries.items.EggTestO
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionUtil.createConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["container"])
class PickAllContainerActionTest {

    fun testEmpty() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.invokeAction(PickAllContainerAction(0))
        assertNull(container[0])
        assertNull(container.floatingItem)
        connection.assertNoPacket()
    }

    fun testAlready() {
        // not invokable in minecraft
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(EggTestO.item, count = 7)
        container.invokeAction(PickAllContainerAction(6))
        assertEquals(container.floatingItem, ItemStack(EggTestO.item, count = 7))
        assertNull(container[6])
        connection.assertNoPacket()
    }

    fun testSingle() {
        // theoretical test, not invokable
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTestO.item, 1)
        container.invokeAction(PickAllContainerAction(0))
        assertNull(container[0])
        assertEquals(container.floatingItem, ItemStack(AppleTestO.item, count = 1))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 6, 0, 0, mapOf(0 to null), ItemStack(AppleTestO.item, count = 1)))
    }

    fun test2Single() {
        // partly theoretical, one slot should be empty
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTestO.item, 1)
        container[1] = ItemStack(AppleTestO.item, 2)
        container.invokeAction(PickAllContainerAction(1))
        assertNull(container[0])
        assertNull(container[1])
        assertEquals(container.floatingItem, ItemStack(AppleTestO.item, count = 3))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 1, 6, 0, 0, mapOf(0 to null, 1 to null), ItemStack(AppleTestO.item, count = 3)))
    }

    fun testNotTaking() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTestO.item, 1)
        container[1] = ItemStack(EggTestO.item, 1)
        container[2] = ItemStack(AppleTestO.item, 2)
        container.invokeAction(PickAllContainerAction(2))
        assertNull(container[0])
        assertEquals(container[1], ItemStack(EggTestO.item, 1))
        assertNull(container[2])
        assertEquals(container.floatingItem, ItemStack(AppleTestO.item, count = 3))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 2, 6, 0, 0, mapOf(0 to null, 2 to null), ItemStack(AppleTestO.item, count = 3)))
    }

    fun testStack() {
        // theoretical, slot already empty
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTestO.item, 64)
        container[5] = ItemStack(AppleTestO.item, 2)
        container.invokeAction(PickAllContainerAction(0))
        assertNull(container[0])
        assertEquals(container[5], ItemStack(AppleTestO.item, count = 2))
        assertEquals(container.floatingItem, ItemStack(AppleTestO.item, count = 64))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 6, 0, 0, mapOf(0 to null), ItemStack(AppleTestO.item, count = 64)))
    }

    fun testExceeds() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTestO.item, 64)
        container[5] = ItemStack(AppleTestO.item, 2)
        container.invokeAction(PickAllContainerAction(5))
        assertEquals(container[0], ItemStack(AppleTestO.item, count = 62))
        assertNull(container[5])
        assertEquals(container.floatingItem, ItemStack(AppleTestO.item, count = 64))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 5, 6, 0, 0, mapOf(0 to ItemStack(AppleTestO.item, count = 2), 5 to null), ItemStack(AppleTestO.item, count = 64)))
    }

    fun testRevertSingle() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTestO.item, 7)
        val action = PickAllContainerAction(5)
        container.invokeAction(action)
        assertNull(container[0])
        assertEquals(container.floatingItem, ItemStack(AppleTestO.item, count = 7))
        container.revertAction(action)
        assertEquals(container[0], ItemStack(AppleTestO.item, count = 7))
        assertNull(container.floatingItem)
    }

    fun testRevert2() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[0] = ItemStack(AppleTestO.item, 7)
        container[1] = ItemStack(AppleTestO.item, 8)
        val action = PickAllContainerAction(5)
        container.invokeAction(action)
        assertNull(container[0])
        assertNull(container[1])
        assertEquals(container.floatingItem, ItemStack(AppleTestO.item, count = 15))
        container.revertAction(action)
        assertEquals(container[0], ItemStack(AppleTestO.item, count = 7))
        assertEquals(container[1], ItemStack(AppleTestO.item, count = 8))
        assertNull(container.floatingItem)
    }
}
