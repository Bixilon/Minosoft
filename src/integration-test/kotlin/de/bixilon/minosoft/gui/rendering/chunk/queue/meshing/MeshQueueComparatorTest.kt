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

package de.bixilon.minosoft.gui.rendering.chunk.queue.meshing

import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.AssertJUnit.assertEquals
import org.testng.annotations.Test


@Test(groups = ["rendering"])
class MeshQueueComparatorTest {

    private fun SectionPosition.item(): MeshQueueItem {
        val section = ChunkSection::class.java.allocate()
        val chunk = Chunk::class.java.allocate()
        chunk::position.forceSet(this.chunkPosition.raw)
        section::chunk.forceSet(chunk)
        section::height.forceSet(this.sectionHeight)

        return MeshQueueItem(section)
    }

    fun `sort initially by distance`() {
        val comparator = MeshQueueComparator()
        comparator.update(BlockPosition(100, 200, 300))

        val a = SectionPosition(6, 12, 18).item()
        val b = SectionPosition(-6, -12, -18).item()
        val c = SectionPosition(-1, -2, -3).item()
        val d = SectionPosition(3, 6, 8).item()

        val list = mutableListOf(a, b, c, d)
        list.sortWith(comparator)

        assertEquals(list, listOf(a, d, c, b))
    }

    fun `update distance correctly`() {
        val comparator = MeshQueueComparator()
        comparator.update(BlockPosition(100, 200, 300))

        val a = SectionPosition(6, 12, 18).item()
        val b = SectionPosition(-6, -12, -18).item()
        val c = SectionPosition(-1, -2, -3).item()
        val d = SectionPosition(3, 6, 8).item()

        val list = mutableListOf(a, b, c, d)
        list.sortWith(comparator)

        comparator.update(BlockPosition(-100, -200, -300))
        list.sortWith(comparator)

        assertEquals(list, listOf(b, c, d, a))
    }
}
