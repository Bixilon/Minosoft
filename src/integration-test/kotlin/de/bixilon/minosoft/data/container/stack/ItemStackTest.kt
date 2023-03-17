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

package de.bixilon.minosoft.data.container.stack

import de.bixilon.minosoft.data.registries.items.AppleTest0
import de.bixilon.minosoft.data.registries.items.CoalTest0
import de.bixilon.minosoft.data.registries.items.EggTest0
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["item_stack"], dependsOnGroups = ["item"])
class ItemStackTest {

    fun matches1() {
        val a = ItemStack(AppleTest0.item, count = 1)
        val b = ItemStack(AppleTest0.item, count = 2)
        assertTrue(a.matches(b))
    }

    fun matches2() {
        val a = ItemStack(AppleTest0.item, count = 1)
        val b = ItemStack(AppleTest0.item, count = 19)
        assertTrue(a.matches(b))
    }

    fun matches3() {
        val a = ItemStack(AppleTest0.item, count = 1)
        val b = ItemStack(EggTest0.item, count = 19)
        assertFalse(a.matches(b))
    }

    fun matches4() {
        val a = ItemStack(CoalTest0.item, count = 1)
        val b = ItemStack(EggTest0.item, count = 19)
        assertFalse(a.matches(b))
    }

    fun matches5() {
        val a = ItemStack(CoalTest0.item, count = 7)
        a.durability.unbreakable = true
        val b = ItemStack(CoalTest0.item, count = 19)
        assertFalse(a.matches(b))
    }

    fun equals1() {
        val a = ItemStack(CoalTest0.item, count = 1)
        val b = ItemStack(CoalTest0.item, count = 1)
        assertEquals(a, b)
    }

    fun equals2() {
        val a = ItemStack(CoalTest0.item, count = 1)
        val b = ItemStack(CoalTest0.item, count = 2)
        assertNotEquals(a, b)
    }
}
