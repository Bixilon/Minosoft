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

package de.bixilon.minosoft.data.world.chunk.chunk

import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.biome.WorldBiomes
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["chunk"])
class ChunkSectionManagementTest {

    private fun create(minSection: Int = 0): ChunkSectionManagement {
        val world = World::class.java.allocate()
        world::dimension.forceSet(DataObserver(DimensionProperties(minY = minSection shl 4)))
        world::biomes.forceSet(WorldBiomes(world))

        world::session.forceSet(PlaySession::class.java.allocate())

        val chunk = Chunk(world, ChunkPosition(0, 0))
        return chunk.sections
    }


    fun `get out of bounds`() {
        val sections = create()
        assertNull(sections[-100])
        assertNull(sections[100])
    }

    fun `create out of bounds`() {
        val sections = create()
        assertNull(sections.create(-100))
        assertNull(sections.create(100))
    }

    fun `get when empty`() {
        val sections = create()
        assertNull(sections[1])
    }

    fun `create section with correct height`() {
        val sections = create()
        val section = sections.create(1)!!

        assertEquals(section.height, 1)
        assertSame(sections[1], section)
    }

    fun `create section negative min y`() {
        val sections = create(minSection = -5)
        val section = sections.create(1)!!

        assertEquals(section.height, 1)
        assertSame(sections[1], section)
    }

    fun `create section positive min y`() {
        val sections = create(minSection = 5)
        val section = sections.create(7)!!

        assertEquals(section.height, 7)
        assertSame(sections[7], section)
    }

    fun `iteration empty`() {
        val sections = create()
        val heights: MutableList<Int> = mutableListOf()

        sections.forEach { heights += it.height }

        assertEquals(heights, listOf<Int>())
    }

    fun `iteration with space`() {
        val sections = create()
        sections.create(3); sections.create(6)
        val heights: MutableList<Int> = mutableListOf()

        sections.forEach { heights += it.height }

        assertEquals(heights, listOf(3, 6))
    }

    fun `neighbour set correctly`() {
        val sections = create()

        val below = sections.create(2)!!
        val above = sections.create(3)!!

        assertSame(below.neighbours[Directions.O_UP], above)
        assertSame(above.neighbours[Directions.O_DOWN], below)
    }
}
