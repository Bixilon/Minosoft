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

package de.bixilon.minosoft.data.world.container.block

import de.bixilon.minosoft.data.registries.blocks.WaterTest0
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["chunk"], dependsOnGroups = ["block"])
class BlockSectionDataProviderTest {

    private fun create(): BlockSectionDataProvider {
        val section = ChunkSection::class.java.allocate()
        return BlockSectionDataProvider(null, section)
    }

    fun `initial empty`() {
        val blocks = create()
        assertTrue(blocks.isEmpty)
        assertFalse(blocks.hasFluid)
        assertEquals(blocks.count, 0)
    }

    fun `single block set and removed`() {
        val blocks = create()
        blocks[InSectionPosition(0, 0, 0)] = StoneTest0.state
        blocks[InSectionPosition(0, 0, 0)] = null
        assertTrue(blocks.isEmpty)
        assertFalse(blocks.hasFluid)
        assertEquals(blocks.count, 0)
    }

    fun `single block set`() {
        val blocks = create()
        blocks[InSectionPosition(0, 0, 0)] = StoneTest0.state
        assertFalse(blocks.isEmpty)
        assertFalse(blocks.hasFluid)
        assertEquals(blocks.count, 1)
    }

    fun `single water set`() {
        val blocks = create()
        blocks[InSectionPosition(0, 0, 0)] = WaterTest0.state
        assertFalse(blocks.isEmpty)
        assertTrue(blocks.hasFluid)
        assertEquals(blocks.count, 1)
    }

    fun `initial min max position`() {
        val blocks = create()
        assertEquals(blocks.minPosition, InSectionPosition(15, 15, 15))
        assertEquals(blocks.maxPosition, InSectionPosition(0, 0, 0))
    }

    fun `set min max position`() {
        val blocks = create()
        blocks[InSectionPosition(0, 0, 0)] = StoneTest0.state
        assertEquals(blocks.minPosition, InSectionPosition(0, 0, 0))
        assertEquals(blocks.maxPosition, InSectionPosition(0, 0, 0))
    }

    fun `set min max position but block not on edge`() {
        val blocks = create()
        blocks[3, 5, 8] = StoneTest0.state
        assertEquals(blocks.minPosition, InSectionPosition(3, 5, 8))
        assertEquals(blocks.maxPosition, InSectionPosition(3, 5, 8))
    }

    fun `set min max position but multiple blocks set`() {
        val blocks = create()
        blocks[3, 5, 8] = StoneTest0.state
        blocks[1, 2, 12] = StoneTest0.state
        assertEquals(blocks.minPosition, InSectionPosition(1, 2, 8))
        assertEquals(blocks.maxPosition, InSectionPosition(3, 5, 12))
    }

    fun `remove one min max position but multiple blocks set`() {
        val blocks = create()
        blocks[3, 5, 8] = StoneTest0.state
        blocks[1, 2, 12] = StoneTest0.state
        blocks[15, 14, 13] = StoneTest0.state
        assertEquals(blocks.minPosition, InSectionPosition(1, 2, 8))
        assertEquals(blocks.maxPosition, InSectionPosition(15, 14, 13))
        blocks[15, 14, 13] = null
        assertEquals(blocks.maxPosition, InSectionPosition(3, 5, 12))
    }


    /*
    fun benchmark() {
        val water = WaterTest0.state
        val stone = StoneTest0.state
        val random = Random(12)

        val data = create()
        for (i in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            if (random.nextBoolean()) {
                data[i] = water
            } else if (random.nextBoolean()) {
                data[i] = stone
            }
        }

        val time = measureTime {
            for (i in 0 until 1999_999) {
                data.recalculate(false)
            }
        }
        println("Took: ${time.inWholeNanoseconds.formatNanos()}")
    }
     */

    // TODO: test initial block set
}
