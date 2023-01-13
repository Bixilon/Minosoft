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
import de.bixilon.minosoft.data.container.actions.types.DropContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.items.AppleTestO
import de.bixilon.minosoft.data.registries.items.EggTestO
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["container"], dependsOnGroups = ["block", "item", "item_stack"])
class DropContainerActionTest {

    fun dropEmptySingle() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.invokeAction(DropContainerAction(7, false))
        assertNull(container.floatingItem)
        connection.assertNoPacket()
    }

    fun dropEmptyStack() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.invokeAction(DropContainerAction(9, true))
        assertNull(container.floatingItem)
        connection.assertNoPacket()
    }

    fun testDropSingle() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[9] = ItemStack(AppleTestO.item, count = 8)
        container.invokeAction(DropContainerAction(9, false))
        assertNull(container.floatingItem)
        assertEquals(container[9], ItemStack(AppleTestO.item, count = 7))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 9, 4, 0, 0, slotsOf(9 to ItemStack(AppleTestO.item, count = 7)), null))
    }

    fun testDropSingleEmpty() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[9] = ItemStack(AppleTestO.item, count = 1)
        container.invokeAction(DropContainerAction(9, false))
        assertNull(container.floatingItem)
        assertEquals(container[9], null)
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 9, 4, 0, 0, slotsOf(9 to null), null))
    }

    fun testDropStack() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[9] = ItemStack(AppleTestO.item, count = 12)
        container.invokeAction(DropContainerAction(9, true))
        assertNull(container.floatingItem)
        assertEquals(container[9], null)
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 9, 4, 1, 0, slotsOf(9 to null), null))
    }

    fun testSingleRevert() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[8] = ItemStack(EggTestO.item, count = 9)
        val action = DropContainerAction(8, false)
        container.invokeAction(action)
        container.revertAction(action)
        assertEquals(container[8], ItemStack(EggTestO.item, count = 9))
    }

    fun testStackRevert() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[8] = ItemStack(EggTestO.item, count = 9)
        val action = DropContainerAction(8, true)
        container.invokeAction(action)
        container.revertAction(action)
        assertEquals(container[8], ItemStack(EggTestO.item, count = 9))
    }
}
