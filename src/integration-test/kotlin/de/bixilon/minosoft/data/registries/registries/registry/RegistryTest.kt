/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.registries.registry

import de.bixilon.kutil.collections.spliterator.SpliteratorUtil.collect
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["registry"])
class RegistryTest {
    private val a = Entry(minosoft("a"))
    private val a_ = Entry(minosoft("a"))
    private val b = Entry(minosoft("b"))
    private val c = Entry(minosoft("c"))
    private val d = Entry(minosoft("d"))

    private fun create(): Registry<Entry> {
        val registry = Registry<Entry>()
        registry.add(0, a)
        registry.add(1, b)
        registry.add(2, c)
        registry.add(3, d)

        return registry
    }

    fun `get item by id`() {
        val registry = create()
        assertSame(registry[0], a)
        assertSame(registry[1], b)
        assertSame(registry[2], c)
        assertSame(registry[3], d)
        assertNull(registry.getOrNull(4))
        assertThrows { registry[4] }
    }

    fun `parent get item by id`() {
        val registry = Registry(parent = create())
        assertSame(registry[0], a)
        assertSame(registry[1], b)
        assertSame(registry[2], c)
        assertSame(registry[3], d)
        assertNull(registry.getOrNull(4))
        assertThrows { registry[4] }
    }

    fun `get item by identifier`() {
        val registry = create()
        assertSame(registry[minosoft("a")], a)
        assertSame(registry[minosoft("b")], b)
        assertSame(registry[minosoft("c")], c)
        assertSame(registry[minosoft("d")], d)
        assertNull(registry[minosoft("e")])
    }

    fun `parent get by identifier`() {
        val registry = Registry(parent = create())
        assertSame(registry[minosoft("a")], a)
        assertSame(registry[minosoft("b")], b)
        assertSame(registry[minosoft("c")], c)
        assertSame(registry[minosoft("d")], d)
        assertNull(registry[minosoft("e")])
    }

    fun `parent override by identifier`() {
        val registry = Registry(parent = create())
        registry.add(0, a_)
        assertSame(registry[minosoft("a")], a_)
        assertSame(registry[minosoft("b")], b)
    }

    fun `parent override by id`() {
        val registry = Registry(parent = create())
        registry.add(0, a_)
        assertSame(registry[0], a_)
        assertSame(registry[minosoft("b")], b)
    }

    fun `iterate no parent`() {
        val registry = create()
        val entries = registry.toSet()
        assertEquals(entries, setOf(a, b, c, d))
    }

    fun `iterate with parent`() {
        val registry = Registry(parent = create())
        registry.add(0, a_)
        val entries = registry.toSet()
        assertEquals(entries, setOf(a_, a, b, c, d))
    }

    @Test(enabled = false) // they don't support splitting
    fun `split without parent`() {
        val registry = create()
        val first = registry.spliterator()
        val second = first.trySplit()

        val firstList = first.collect()
        val secondList = second.collect()
        assertEquals(firstList.toSet(), setOf(a, b))
        assertEquals(secondList.toSet(), setOf(c, d))
    }

    // TODO: codec, spliterating

    private class Entry(override val identifier: ResourceLocation) : RegistryItem() {

        override fun equals(other: Any?): Boolean {
            return other === this
        }
    }
}
