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
import de.bixilon.minosoft.data.container.StackableTest1
import de.bixilon.minosoft.data.container.StackableTest2
import de.bixilon.minosoft.data.container.actions.types.PickAllContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["container"], dependsOnGroups = ["block", "item", "item_stack"])
class PickAllContainerActionTest {

    fun testEmpty() {
        val session = createSession()
        val container = createContainer(session)
        container.execute(PickAllContainerAction(0))
        assertEquals(container.items.slots, slotsOf())
        assertNull(container.floating)
        session.assertNoPacket()
    }

    fun testSingle() {
        val session = createSession()
        val container = createContainer(session)
        container.floating = ItemStack(StackableTest1, count = 7)
        container.execute(PickAllContainerAction(6))
        assertEquals(container.items.slots, slotsOf())
        // session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 6, 0, 0, slotsOf(), ItemStack(StackableTest1, count = 7)))
    }

    fun test2Single() {
        val session = createSession()
        val container = createContainer(session)
        container.floating = ItemStack(StackableTest2, 1)
        container.items[1] = ItemStack(StackableTest2, 2)
        container.execute(PickAllContainerAction(0))
        assertEquals(container.items.slots, slotsOf())
        assertEquals(container.floating, ItemStack(StackableTest2, count = 3))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 6, 0, 0, slotsOf(1 to null), ItemStack(StackableTest2, count = 3)))
    }

    fun testNotTaking() {
        val session = createSession()
        val container = createContainer(session)
        container.floating = ItemStack(StackableTest2, 1)
        container.items[1] = ItemStack(StackableTest1, 1)
        container.items[2] = ItemStack(StackableTest2, 2)
        container.execute(PickAllContainerAction(0))

        assertEquals(container.items.slots, slotsOf(1 to ItemStack(StackableTest1, 1)))
        assertEquals(container.floating, ItemStack(StackableTest2, count = 3))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 6, 0, 0, slotsOf(2 to null), ItemStack(StackableTest2, count = 3)))
    }

    fun testStack() {
        val session = createSession()
        val container = createContainer(session)
        container.floating = ItemStack(StackableTest2, 64)
        container.items[5] = ItemStack(StackableTest2, 2)
        container.execute(PickAllContainerAction(0))
        assertEquals(container.items.slots, slotsOf(5 to ItemStack(StackableTest2, 2)))
        assertEquals(container.floating, ItemStack(StackableTest2, count = 64))
        session.assertNoPacket()
    }

    fun testExceeds() {
        val session = createSession()
        val container = createContainer(session)
        container.items[0] = ItemStack(StackableTest2, 63)
        container.floating = ItemStack(StackableTest2, 3)
        container.execute(PickAllContainerAction(5))
        assertEquals(container.items.slots, slotsOf(0 to ItemStack(StackableTest2, 2)))
        assertEquals(container.floating, ItemStack(StackableTest2, count = 64))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 5, 6, 0, 0, slotsOf(0 to ItemStack(StackableTest2, count = 2)), ItemStack(StackableTest2, count = 64)))
    }
}
