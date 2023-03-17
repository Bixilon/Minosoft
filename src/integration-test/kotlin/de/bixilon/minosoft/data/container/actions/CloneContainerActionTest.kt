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
import de.bixilon.minosoft.data.container.actions.types.CloneContainerAction
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
class CloneContainerActionTest {

    fun testEmpty() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.actions.invoke(CloneContainerAction(0))
        assertNull(container.floatingItem)
        connection.assertNoPacket()
    }

    fun testAlready1() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(EggTest0.item, count = 7)
        container.actions.invoke(CloneContainerAction(6))
        assertEquals(container.floatingItem, ItemStack(EggTest0.item, count = 7))
        assertNull(container[6])
        connection.assertNoPacket()
    }

    fun testAlready2() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[6] = ItemStack(AppleTest0.item, count = 7)
        container.floatingItem = ItemStack(EggTest0.item, count = 7)
        container.actions.invoke(CloneContainerAction(6))
        assertEquals(container.floatingItem, ItemStack(EggTest0.item, count = 7))
        assertEquals(container[6], ItemStack(AppleTest0.item, count = 7))
        connection.assertNoPacket()
    }

    fun testTaking() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[1] = ItemStack(AppleTest0.item)
        container.actions.invoke(CloneContainerAction(1))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 64))
        // TODO: Not sending any packet in 1.18.2?
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 1, 3, 0, 0, slotsOf(), ItemStack(AppleTest0.item, count = 64)))
    }

    fun taskTalking2() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[3] = ItemStack(AppleTest0.item, count = 8)
        container.actions.invoke(CloneContainerAction(3))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 64))
        // TODO: Not sending any packet in 1.18.2?
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 3, 3, 0, 0, slotsOf(), ItemStack(AppleTest0.item, count = 64)))
    }

    fun testStackLimit() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[8] = ItemStack(EggTest0.item, count = 9)
        container.actions.invoke(CloneContainerAction(8))
        assertEquals(container.floatingItem, ItemStack(EggTest0.item, count = 16))
        // TODO: Not sending any packet in 1.18.2?
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 8, 3, 0, 0, slotsOf(), ItemStack(EggTest0.item, count = 16)))
    }

    fun testRevert() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[8] = ItemStack(EggTest0.item, count = 9)
        val action = CloneContainerAction(8)
        container.actions.invoke(action)
        assertEquals(container.floatingItem, ItemStack(EggTest0.item, count = 16))
        container.actions.revert(action)
        assertNull(container.floatingItem)
    }
}
