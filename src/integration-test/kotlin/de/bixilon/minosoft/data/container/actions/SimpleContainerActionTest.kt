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

package de.bixilon.minosoft.data.container.actions

import de.bixilon.minosoft.data.container.ContainerTestUtil.createContainer
import de.bixilon.minosoft.data.container.ContainerUtil.slotsOf
import de.bixilon.minosoft.data.container.actions.types.SimpleContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["container"], dependsOnGroups = ["block", "item", "item_stack"])
class SimpleContainerActionTest {

    fun testEmpty() {
        val session = createSession()
        val container = createContainer(session)
        container.actions.invoke(SimpleContainerAction(0, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf())
        assertNull(container.floating)
        // session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 0, 0, 0, slotsOf(), null))
        session.assertNoPacket()
    }

    fun testPutAll() {
        val session = createSession()
        val container = createContainer(session)
        container.floating = ItemStack(TestItem2, count = 7)
        container.actions.invoke(SimpleContainerAction(0, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf(0 to ItemStack(TestItem2, count = 7)))
        assertNull(container.floating)
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 0, 0, 0, slotsOf(0 to ItemStack(TestItem2, count = 7)), null))
    }

    fun testPutOne() {
        val session = createSession()
        val container = createContainer(session)
        container.floating = ItemStack(TestItem2, count = 7)
        container.actions.invoke(SimpleContainerAction(0, SimpleContainerAction.ContainerCounts.PART))
        assertEquals(container.slots, slotsOf(0 to ItemStack(TestItem2, count = 1)))
        assertEquals(container.floating, ItemStack(TestItem2, count = 6))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 0, 1, 0, slotsOf(0 to ItemStack(TestItem2, count = 1)), ItemStack(TestItem2, count = 6)))
    }

    fun testPutAlreadyAll() {
        val session = createSession()
        val container = createContainer(session)
        container.floating = ItemStack(TestItem2, count = 7)
        container[8] = ItemStack(TestItem2, count = 2)
        container.actions.invoke(SimpleContainerAction(8, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf(8 to ItemStack(TestItem2, count = 9)))
        assertNull(container.floating)
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 8, 0, 0, 0, slotsOf(8 to ItemStack(TestItem2, count = 9)), null))
    }

    fun testPutAlreadySingle() {
        val session = createSession()
        val container = createContainer(session)
        container.floating = ItemStack(TestItem2, count = 7)
        container[12] = ItemStack(TestItem2, count = 3)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.PART))
        assertEquals(container.slots, slotsOf(12 to ItemStack(TestItem2, count = 4)))
        assertEquals(container.floating, ItemStack(TestItem2, count = 6))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 1, 0, slotsOf(12 to ItemStack(TestItem2, count = 4)), ItemStack(TestItem2, count = 6)))
    }

    fun testPutExceeds() {
        val session = createSession()
        val container = createContainer(session)
        container.floating = ItemStack(TestItem1, count = 14)
        container[12] = ItemStack(TestItem1, count = 15)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf(12 to ItemStack(TestItem1, count = 16)))
        assertEquals(container.floating, ItemStack(TestItem1, 13))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 0, 0, slotsOf(12 to ItemStack(TestItem1, count = 16)), ItemStack(TestItem1, count = 13)))
    }

    fun testRemoveAll() {
        val session = createSession()
        val container = createContainer(session)
        container[12] = ItemStack(TestItem2, count = 3)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.ALL))
        assertEquals(container.slots, slotsOf())
        assertEquals(container.floating, ItemStack(TestItem2, count = 3))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 0, 0, slotsOf(12 to null), ItemStack(TestItem2, count = 3)))
    }

    fun testRemoveHalf() {
        val session = createSession()
        val container = createContainer(session)
        container[12] = ItemStack(TestItem2, count = 8)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.PART))
        assertEquals(container.slots, slotsOf(12 to ItemStack(TestItem2, 4)))
        assertEquals(container.floating, ItemStack(TestItem2, count = 4))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 1, 0, slotsOf(12 to ItemStack(TestItem2, 4)), ItemStack(TestItem2, count = 4)))
    }

    fun testRemoveHalfOdd() {
        val session = createSession()
        val container = createContainer(session)
        container[12] = ItemStack(TestItem2, count = 9)
        container.actions.invoke(SimpleContainerAction(12, SimpleContainerAction.ContainerCounts.PART))
        assertEquals(container.slots, slotsOf(12 to ItemStack(TestItem2, 4)))
        assertEquals(container.floating, ItemStack(TestItem2, count = 5))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 12, 0, 1, 0, slotsOf(12 to ItemStack(TestItem2, 4)), ItemStack(TestItem2, count = 5)))
    }

    // TODO: mixing types, revert, creative
}
