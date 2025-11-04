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

package de.bixilon.minosoft.data.registries.item

import de.bixilon.minosoft.data.container.TestItem1
import de.bixilon.minosoft.data.container.TestItem2
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["registry"])
class ItemRegistryTest {

    fun `replace flattened item id 0 with null`() {
        val registry = ItemRegistry()
        registry.add(0, TestItem1)
        assertEquals(registry.getOrNull(0), null)
    }

    fun `get flattened item with id 1`() {
        val registry = ItemRegistry()
        registry.add(0, TestItem1)
        registry.add(1, TestItem2)
        assertEquals(registry.getOrNull(1), TestItem2)
    }

    fun `replace legacy item id 0 with null`() {
        val registry = ItemRegistry(flattened = false)
        registry.add(0, TestItem1)
        assertEquals(registry.getOrNull(0), null)
    }

    fun `replace legacy item id 0x0FFFF with null`() {
        val registry = ItemRegistry(flattened = false)
        registry.add(0x0_0000, TestItem1)
        registry.add(0x0_FFFF, TestItem1)
        assertEquals(registry.getOrNull(0x0_FFFF), null)
    }

    fun `get legacy item without meta`() {
        val registry = ItemRegistry(flattened = false)
        registry.add(0x1_0000, TestItem1)
        assertEquals(registry.getOrNull(0x1_0000), TestItem1)
    }

    fun `get legacy item with meta`() {
        val registry = ItemRegistry(flattened = false)
        registry.add(0x1_0000, TestItem1)
        registry.add(0x1_0005, TestItem2)
        assertEquals(registry.getOrNull(0x1_0005), TestItem2)
    }

    fun `get legacy item with absent meta`() {
        val registry = ItemRegistry(flattened = false)
        registry.add(0x1_0000, TestItem1)
        registry.add(0x1_0005, TestItem2)
        assertEquals(registry.getOrNull(0x1_0006), TestItem1)
    }
}
