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

import de.bixilon.minosoft.data.container.ContainerTestUtil.createChest
import de.bixilon.minosoft.data.container.ContainerTestUtil.createFurnace
import de.bixilon.minosoft.data.container.ContainerUtil.slotsOf
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.items.AppleTestO
import de.bixilon.minosoft.data.registries.items.CoalTest0
import de.bixilon.minosoft.data.registries.items.EggTestO
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertNoPacket
import de.bixilon.minosoft.protocol.network.connection.play.PacketTestUtil.assertOnlyPacket
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionUtil.createConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import org.testng.Assert.assertEquals
import org.testng.Assert.assertNull
import org.testng.annotations.Test

@Test(groups = ["container"], dependsOnGroups = ["block", "item"])
class FastMoveContainerActionTest {

    fun empty() {
        val connection = createConnection()
        val container = createChest(connection)
        container.invokeAction(FastMoveContainerAction(0))
        assertNull(container.floatingItem)
        assertEquals(container.slots.size, 0)
        connection.assertNoPacket()
    }

    fun hotbarToChest() {
        val connection = createConnection()
        val container = createChest(connection)
        container[54] = ItemStack(AppleTestO.item, 9)
        container.invokeAction(FastMoveContainerAction(54))
        assertNull(container.floatingItem)
        assertNull(container[54])
        assertEquals(container[0], ItemStack(AppleTestO.item, 9))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 53, 1, 0, 0, slotsOf(54 to null, 0 to ItemStack(AppleTestO.item, count = 9)), null))
    }

    fun chestToHotbar() {
        val connection = createConnection()
        val container = createChest(connection)
        container[0] = ItemStack(AppleTestO.item, 9)
        container.invokeAction(FastMoveContainerAction(0))
        assertNull(container.floatingItem)
        assertNull(container[0])
        assertEquals(container[62], ItemStack(AppleTestO.item, 9))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 1, 0, 0, slotsOf(62 to null, 0 to ItemStack(AppleTestO.item, count = 9)), null))
    }

    fun fullHotbarChestToHotbar() {
        val connection = createConnection()
        val container = createChest(connection)

        container[54] = ItemStack(EggTestO.item, 9)
        container[55] = ItemStack(EggTestO.item, 9)
        container[56] = ItemStack(EggTestO.item, 9)
        container[57] = ItemStack(EggTestO.item, 9)
        container[58] = ItemStack(EggTestO.item, 9)
        container[59] = ItemStack(EggTestO.item, 9)
        container[60] = ItemStack(EggTestO.item, 9)
        container[61] = ItemStack(EggTestO.item, 9)
        container[62] = ItemStack(EggTestO.item, 9)

        container[0] = ItemStack(AppleTestO.item, 9)

        container.invokeAction(FastMoveContainerAction(0))
        assertNull(container.floatingItem)
        assertNull(container[0])
        assertEquals(container[53], ItemStack(AppleTestO.item, 9))
        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 1, 0, 0, slotsOf(0 to null, 53 to ItemStack(AppleTestO.item, count = 9)), null))
    }

    fun mergeItems() {
        val connection = createConnection()
        val container = createChest(connection)

        container[54] = ItemStack(CoalTest0.item, 60)
        container[55] = ItemStack(AppleTestO.item, 61)
        container[56] = ItemStack(CoalTest0.item, 60)
        container[57] = ItemStack(CoalTest0.item, 56)
        container[58] = ItemStack(CoalTest0.item, 21)

        container[0] = ItemStack(CoalTest0.item, 63)

        container.invokeAction(FastMoveContainerAction(0))
        assertNull(container.floatingItem)
        assertNull(container[0])
        assertEquals(container[54], ItemStack(CoalTest0.item, 64))
        assertEquals(container[55], ItemStack(AppleTestO.item, 61))
        assertEquals(container[56], ItemStack(CoalTest0.item, 64))
        assertEquals(container[57], ItemStack(CoalTest0.item, 64))
        assertEquals(container[58], ItemStack(CoalTest0.item, 64))
        assertEquals(container[62], ItemStack(CoalTest0.item, 4))

        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 1, 0, 0, slotsOf(0 to null, 58 to ItemStack(AppleTestO.item, count = 64), 56 to ItemStack(AppleTestO.item, count = 64), 54 to ItemStack(AppleTestO.item, count = 64), 57 to ItemStack(AppleTestO.item, count = 64), 62 to ItemStack(AppleTestO.item, count = 4)), null)) // TODO: respect order of changes
    }

    fun fuelSlot1() {
        val connection = createConnection()
        val container = createFurnace(connection)

        container[30] = ItemStack(EggTestO.item, 12)

        container.invokeAction(FastMoveContainerAction(30))
        assertNull(container.floatingItem)
        assertNull(container[0])
        assertNull(container[1])
        assertNull(container[2])
        assertNull(container[30])
        assertEquals(container[3], ItemStack(EggTestO.item, 12))

        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 1, 0, 0, slotsOf(30 to null, 3 to ItemStack(AppleTestO.item, count = 8)), null))
    }

    fun fuelSlot2() {
        val connection = createConnection()
        val container = createFurnace(connection)

        container[30] = ItemStack(CoalTest0.item, 12)

        container.invokeAction(FastMoveContainerAction(30))
        assertNull(container.floatingItem)
        assertNull(container[0])
        assertEquals(container[1], ItemStack(CoalTest0.item, 12))
        assertNull(container[2])

        connection.assertOnlyPacket(ContainerClickC2SP(9, container.serverRevision, 0, 1, 0, 0, slotsOf(30 to null, 1 to ItemStack(AppleTestO.item, count = 8)), null))
    }

    // TODO: revert, full container
}
