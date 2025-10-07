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
import de.bixilon.minosoft.data.container.actions.types.DropSlotContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.items.AppleTest0
import de.bixilon.minosoft.data.registries.items.EggTest0
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["container"], dependsOnGroups = ["block", "item", "item_stack"])
class DropContainerActionTest {

    fun dropEmptySingle() {
        val session = createSession()
        val container = createContainer(session)
        container.actions.invoke(DropSlotContainerAction(7, false))
        assertNull(container.floating)
        session.assertNoPacket()
    }

    fun dropEmptyStack() {
        val session = createSession()
        val container = createContainer(session)
        container.actions.invoke(DropSlotContainerAction(9, true))
        assertNull(container.floating)
        session.assertNoPacket()
    }

    fun testDropSingle() {
        val session = createSession()
        val container = createContainer(session)
        container[9] = ItemStack(AppleTest0.item, count = 8)
        container.actions.invoke(DropSlotContainerAction(9, false))
        assertNull(container.floating)
        assertEquals(container[9], ItemStack(AppleTest0.item, count = 7))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 9, 4, 0, 0, slotsOf(9 to ItemStack(AppleTest0.item, count = 7)), null))
    }

    fun testDropSingleEmpty() {
        val session = createSession()
        val container = createContainer(session)
        container[9] = ItemStack(AppleTest0.item, count = 1)
        container.actions.invoke(DropSlotContainerAction(9, false))
        assertNull(container.floating)
        assertEquals(container[9], null)
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 9, 4, 0, 0, slotsOf(9 to null), null))
    }

    fun testDropStack() {
        val session = createSession()
        val container = createContainer(session)
        container[9] = ItemStack(AppleTest0.item, count = 12)
        container.actions.invoke(DropSlotContainerAction(9, true))
        assertNull(container.floating)
        assertEquals(container[9], null)
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 9, 4, 1, 0, slotsOf(9 to null), null))
    }

    fun testSingleRevert() {
        val session = createSession()
        val container = createContainer(session)
        container[8] = ItemStack(EggTest0.item, count = 9)
        val action = DropSlotContainerAction(8, false)
        container.actions.invoke(action)
        container.actions.revert(action)
        assertEquals(container[8], ItemStack(EggTest0.item, count = 9))
    }

    fun testStackRevert() {
        val session = createSession()
        val container = createContainer(session)
        container[8] = ItemStack(EggTest0.item, count = 9)
        val action = DropSlotContainerAction(8, true)
        container.actions.invoke(action)
        container.actions.revert(action)
        assertEquals(container[8], ItemStack(EggTest0.item, count = 9))
    }
}
