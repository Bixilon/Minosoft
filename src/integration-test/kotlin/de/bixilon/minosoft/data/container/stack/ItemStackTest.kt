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

package de.bixilon.minosoft.data.container.stack

import de.bixilon.minosoft.data.container.DurableTestItem1
import de.bixilon.minosoft.data.container.TestItem1
import de.bixilon.minosoft.data.container.TestItem2
import de.bixilon.minosoft.data.container.TestItem3
import de.bixilon.minosoft.data.container.stack.properties.DurabilityProperty
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["item_stack"], dependsOnGroups = ["item"])
class ItemStackTest {

    fun `matches mixed count`() {
        val a = ItemStack(TestItem2, count = 1)
        val b = ItemStack(TestItem2, count = 2)
        assertTrue(a.matches(b))
    }

    fun `matches mixed count 2`() {
        val a = ItemStack(TestItem2, count = 1)
        val b = ItemStack(TestItem2, count = 19)
        assertTrue(a.matches(b))
    }

    fun `matches different type`() {
        val a = ItemStack(TestItem2, count = 1)
        val b = ItemStack(TestItem1, count = 19)
        assertFalse(a.matches(b))
    }

    fun `matches different type 2`() {
        val a = ItemStack(TestItem3, count = 1)
        val b = ItemStack(TestItem1, count = 19)
        assertFalse(a.matches(b))
    }

    fun `matches different durability`() {
        val a = ItemStack(DurableTestItem1, count = 7, durability = DurabilityProperty(durability = 1, unbreakable = true))
        val b = ItemStack(DurableTestItem1, count = 19)
        assertFalse(a.matches(b))
    }

    fun `equals same`() {
        val a = ItemStack(TestItem3, count = 1)
        val b = ItemStack(TestItem3, count = 1)
        assertEquals(a, b)
    }

    fun `equals different count`() {
        val a = ItemStack(TestItem3, count = 1)
        val b = ItemStack(TestItem3, count = 2)
        assertNotEquals(a, b)
    }

    fun `forbid empty`() {
        assertThrows { ItemStack(TestItem1, 0) }
    }

    fun `forbid negative count`() {
        assertThrows { ItemStack(TestItem1, -1) }
    }

    fun `forbid durability with not durable item`() {
        assertThrows { ItemStack(TestItem1, 1, durability = DurabilityProperty(1)) }
    }
}
