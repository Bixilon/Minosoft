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

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.items.EggTestO
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["container"])
class CloneContainerActionTest {

    fun testEmpty() {
        val container = ContainerTestUtil.createContainer()
        container.invokeAction(CloneContainerAction(0))
        assertNull(container.floatingItem)
    }

    fun testAlready() {
        val container = ContainerTestUtil.createContainer()
        container.floatingItem = ItemStack(EggTest0.item, count = 7)
        container.invokeAction(CloneContainerAction(6))
        assertEquals(container.floatingItem, ItemStack(EggTest0.item, count = 7))
    }

    fun testTaking() {
        val container = ContainerTestUtil.createContainer()
        container[1] = ItemStack(AppleTest0.item)
        container.invokeAction(CloneContainerAction(1))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 64))
    }

    fun taskTalking2() {
        val container = ContainerTestUtil.createContainer()
        container[3] = ItemStack(AppleTest0.item, count = 8)
        container.invokeAction(CloneContainerAction(3))
        assertEquals(container.floatingItem, ItemStack(AppleTest0.item, count = 64))
    }

    fun testStackLimit() {
        val container = ContainerTestUtil.createContainer()
        container[8] = ItemStack(EggTestO.item, count = 9)
        container.invokeAction(CloneContainerAction(8))
        assertEquals(container.floatingItem, ItemStack(EggTestO.item, count = 16))
    }
}
