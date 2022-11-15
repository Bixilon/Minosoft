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
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["container"])
@Deprecated("Verify with minecraft")
class CloneContainerActionTest {

    fun testEmpty() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.invokeAction(CloneContainerAction(0))
        assertNull(container.floatingItem)
        connection.assertNoPacket()
    }

    fun testAlready() {
        val connection = createConnection()
        val container = createContainer(connection)
        container.floatingItem = ItemStack(EggTestO.item, count = 7)
        container.invokeAction(CloneContainerAction(6))
        assertEquals(container.floatingItem, ItemStack(EggTestO.item, count = 7))
        assertNull(container[6])
        connection.assertNoPacket()
    }

    fun testTaking() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[1] = ItemStack(AppleTestO.item)
        container.invokeAction(CloneContainerAction(1))
        assertEquals(container.floatingItem, ItemStack(AppleTestO.item, count = 64))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 1, 3, 0, 0, emptyMap(), ItemStack(AppleTestO.item)))
    }

    fun taskTalking2() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[3] = ItemStack(AppleTestO.item, count = 8)
        container.invokeAction(CloneContainerAction(3))
        assertEquals(container.floatingItem, ItemStack(AppleTestO.item, count = 64))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 3, 3, 0, 0, emptyMap(), ItemStack(AppleTestO.item, count = 8)))
    }

    fun testStackLimit() {
        val connection = createConnection()
        val container = createContainer(connection)
        container[8] = ItemStack(EggTestO.item, count = 9)
        container.invokeAction(CloneContainerAction(8))
        assertEquals(container.floatingItem, ItemStack(EggTestO.item, count = 16))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 8, 3, 0, 0, emptyMap(), ItemStack(EggTestO.item, count = 9)))
    }
}
