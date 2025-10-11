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

import de.bixilon.minosoft.data.container.ContainerTestUtil.createChest
import de.bixilon.minosoft.data.container.ContainerTestUtil.createFurnace
import de.bixilon.minosoft.data.container.ContainerTestUtil.createInventory
import de.bixilon.minosoft.data.container.ContainerUtil.slotsOf
import de.bixilon.minosoft.data.container.TestItem1
import de.bixilon.minosoft.data.container.TestItem2
import de.bixilon.minosoft.data.container.TestItem3
import de.bixilon.minosoft.data.container.actions.types.FastMoveContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.session.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.session.play.SessionTestUtil.createSession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["container"], dependsOnGroups = ["block", "item", "item_stack"])
class FastMoveContainerActionTest {

    fun empty() {
        val session = createSession()
        val container = createChest(session)
        container.execute(FastMoveContainerAction(0))
        assertNull(container.floating)
        assertEquals(container.items.slots, slotsOf())
        session.assertNoPacket()
    }

    fun hotbarToChest() {
        val session = createSession()
        val container = createChest(session)
        container.items[54] = ItemStack(TestItem2, 9)
        container.execute(FastMoveContainerAction(54))
        assertNull(container.floating)
        assertEquals(container.items.slots, slotsOf(0 to ItemStack(TestItem2, 9)))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 54, 1, 0, 0, slotsOf(54 to null, 0 to ItemStack(TestItem2, count = 9)), null))
    }

    fun chestToHotbar() {
        val session = createSession()
        val container = createChest(session)
        container.items[0] = ItemStack(TestItem2, 9)
        container.execute(FastMoveContainerAction(0))
        assertNull(container.floating)
        assertEquals(container.items.slots, slotsOf(62 to ItemStack(TestItem2, 9)))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 1, 0, 0, slotsOf(0 to null, 62 to ItemStack(TestItem2, count = 9)), null))
    }

    fun fullHotbarChestToHotbar() {
        val session = createSession()
        val container = createChest(session)

        container.items[54] = ItemStack(TestItem1, 9)
        container.items[55] = ItemStack(TestItem1, 9)
        container.items[56] = ItemStack(TestItem1, 9)
        container.items[57] = ItemStack(TestItem1, 9)
        container.items[58] = ItemStack(TestItem1, 9)
        container.items[59] = ItemStack(TestItem1, 9)
        container.items[60] = ItemStack(TestItem1, 9)
        container.items[61] = ItemStack(TestItem1, 9)
        container.items[62] = ItemStack(TestItem1, 9)

        container.items[0] = ItemStack(TestItem2, 9)

        container.execute(FastMoveContainerAction(0))
        assertNull(container.floating)
        assertNull(container.items[0])
        assertEquals(container.items[53], ItemStack(TestItem2, 9))
        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 1, 0, 0, slotsOf(0 to null, 53 to ItemStack(TestItem2, count = 9)), null))
    }

    fun mergeItems() {
        val session = createSession()
        val container = createChest(session)

        container.items[54] = ItemStack(TestItem3, 60)
        container.items[55] = ItemStack(TestItem2, 61)
        container.items[56] = ItemStack(TestItem3, 60)
        container.items[57] = ItemStack(TestItem3, 56)
        container.items[58] = ItemStack(TestItem3, 21)

        container.items[0] = ItemStack(TestItem3, 63)

        container.execute(FastMoveContainerAction(0))
        assertNull(container.floating)
        assertEquals(
            container.items.slots, slotsOf(
                54 to ItemStack(TestItem3, 64),
                55 to ItemStack(TestItem2, 61),
                56 to ItemStack(TestItem3, 64),
                57 to ItemStack(TestItem3, 64),
                58 to ItemStack(TestItem3, 64),
                62 to ItemStack(TestItem3, 4),
            )
        )

        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 1, 0, 0, slotsOf(0 to null, 58 to ItemStack(TestItem3, count = 64), 56 to ItemStack(TestItem3, count = 64), 54 to ItemStack(TestItem3, count = 64), 57 to ItemStack(TestItem3, count = 64), 62 to ItemStack(TestItem3, count = 4)), null))
    }

    @Test(enabled = false)
    fun fuelSlot1() {
        // TODO: enable test
        val session = createSession()
        val container = createFurnace(session)

        container.items[30] = ItemStack(TestItem1, 12)

        container.execute(FastMoveContainerAction(30))
        assertNull(container.floating)
        assertEquals(container.items.slots, slotsOf(3 to ItemStack(TestItem1, 12)))

        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 1, 0, 0, slotsOf(30 to null, 3 to ItemStack(TestItem2, count = 8)), null))
    }

    fun fuelSlot2() {
        val session = createSession()
        val container = createFurnace(session)

        container.items[30] = ItemStack(TestItem3, 12)

        container.execute(FastMoveContainerAction(30))
        assertNull(container.floating)
        assertEquals(container.items.slots, slotsOf(1 to ItemStack(TestItem3, 12)))

        session.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 30, 1, 0, 0, slotsOf(30 to null, 1 to ItemStack(TestItem3, count = 12)), null))
    }


    fun playerPassiveToHotbar() {
        val session = createSession()
        val container = createInventory(session)
        container.items[9] = ItemStack(TestItem2)
        container.execute(FastMoveContainerAction(9))
        assertNull(container.floating)
        assertEquals(container.items.slots, slotsOf(36 to ItemStack(TestItem2)))
    }

    fun craftingToPassive() {
        val session = createSession()
        val container = createInventory(session)
        container.items[1] = ItemStack(TestItem2)
        container.execute(FastMoveContainerAction(1))
        assertNull(container.floating)
        assertEquals(container.items.slots, slotsOf(9 to ItemStack(TestItem2)))
    }

    fun hotbarToPassive() {
        val session = createSession()
        val container = createInventory(session)
        container.items[36] = ItemStack(TestItem2)
        container.execute(FastMoveContainerAction(36))
        assertNull(container.floating)
        assertEquals(container.items.slots, slotsOf(9 to ItemStack(TestItem2)))
    }

    fun passiveToChest() {
        val session = createSession()
        val container = createChest(session)
        container.items[49] = ItemStack(TestItem2, count = 19)
        container.execute(FastMoveContainerAction(49))
        assertNull(container.floating)
        assertEquals(container.items.slots, slotsOf(0 to ItemStack(TestItem2, 19)))
    }

    // TODO: revert, full container
}
