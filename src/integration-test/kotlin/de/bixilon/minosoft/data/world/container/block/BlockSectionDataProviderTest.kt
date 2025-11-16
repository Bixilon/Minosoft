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

import de.bixilon.kutil.benchmark.BenchmarkUtil
import de.bixilon.minosoft.data.registries.blocks.WaterTest0
import de.bixilon.minosoft.data.registries.blocks.state.TestBlockStates
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.*
import org.testng.annotations.Test
import kotlin.random.Random

@Test(groups = ["chunk"], dependsOnGroups = ["block"])
class BlockSectionDataProviderTest {

    private fun create(): BlockSectionDataProvider {
        val section = ChunkSection::class.java.allocate()
        return BlockSectionDataProvider(null, section)
    }

    fun `initial empty`() {
        val blocks = create()
        assertTrue(blocks.isEmpty)
        assertEquals(blocks.fluidCount, 0)
        assertEquals(blocks.fullOpaqueCount, 0)
        assertEquals(blocks.count, 0)
    }

    fun `single block set and removed`() {
        val blocks = create()
        blocks[InSectionPosition(0, 0, 0)] = IT.BLOCK_1
        blocks[InSectionPosition(0, 0, 0)] = null
        assertTrue(blocks.isEmpty)
        assertEquals(blocks.fluidCount, 0)
        assertEquals(blocks.count, 0)
    }

    fun `full opaque is set`() {
        val blocks = create()
        blocks[InSectionPosition(3, 2, 1)] = TestBlockStates.OPAQUE1
        assertTrue(blocks.fullOpaque[InSectionPosition(3, 2, 1).index])
    }

    fun `full opaque is removed null`() {
        val blocks = create()
        blocks[InSectionPosition(3, 2, 1)] = TestBlockStates.OPAQUE1
        blocks[InSectionPosition(3, 2, 1)] = null
        assertFalse(blocks.fullOpaque[InSectionPosition(3, 2, 1).index])
    }

    fun `full opaque is removed non opaque`() {
        val blocks = create()
        blocks[InSectionPosition(3, 2, 1)] = TestBlockStates.OPAQUE1
        blocks[InSectionPosition(3, 2, 1)] = TestBlockStates.TEST1
        assertFalse(blocks.fullOpaque[InSectionPosition(3, 2, 1).index])
    }

    // TODO: full opaque initially

    fun `single block set`() {
        val blocks = create()
        blocks[InSectionPosition(0, 0, 0)] = TestBlockStates.TEST1
        assertFalse(blocks.isEmpty)
        assertEquals(blocks.fullOpaqueCount, 0)
        assertEquals(blocks.count, 1)
    }

    fun `single full opaque set`() {
        val blocks = create()
        blocks[InSectionPosition(0, 0, 0)] = TestBlockStates.OPAQUE1
        assertFalse(blocks.isEmpty)
        assertEquals(blocks.fullOpaqueCount, 1)
        assertEquals(blocks.count, 1)
    }

    fun `single water set`() {
        val blocks = create()
        blocks[InSectionPosition(0, 0, 0)] = WaterTest0.state
        assertFalse(blocks.isEmpty)
        assertEquals(blocks.fluidCount, 1)
        assertEquals(blocks.count, 1)
    }

    fun `initial min max position`() {
        val blocks = create()
        assertEquals(blocks.minPosition, InSectionPosition(15, 15, 15))
        assertEquals(blocks.maxPosition, InSectionPosition(0, 0, 0))
    }

    fun `set min max position`() {
        val blocks = create()
        blocks[InSectionPosition(0, 0, 0)] = IT.BLOCK_1
        assertEquals(blocks.minPosition, InSectionPosition(0, 0, 0))
        assertEquals(blocks.maxPosition, InSectionPosition(0, 0, 0))
    }

    fun `set min max position but block not on edge`() {
        val blocks = create()
        blocks[3, 5, 8] = IT.BLOCK_1
        assertEquals(blocks.minPosition, InSectionPosition(3, 5, 8))
        assertEquals(blocks.maxPosition, InSectionPosition(3, 5, 8))
    }

    fun `set min max position but multiple blocks set`() {
        val blocks = create()
        blocks[3, 5, 8] = IT.BLOCK_1
        blocks[1, 2, 12] = IT.BLOCK_1
        assertEquals(blocks.minPosition, InSectionPosition(1, 2, 8))
        assertEquals(blocks.maxPosition, InSectionPosition(3, 5, 12))
    }

    fun `remove one min max position but multiple blocks set`() {
        val blocks = create()
        blocks[3, 5, 8] = IT.BLOCK_1
        blocks[1, 2, 12] = IT.BLOCK_1
        blocks[15, 14, 13] = IT.BLOCK_1
        assertEquals(blocks.minPosition, InSectionPosition(1, 2, 8))
        assertEquals(blocks.maxPosition, InSectionPosition(15, 14, 13))
        blocks[15, 14, 13] = null
        assertEquals(blocks.maxPosition, InSectionPosition(3, 5, 12))
    }


    @Test(enabled = false)
    fun benchmark() {
        val water = WaterTest0.state
        val stone = IT.BLOCK_1
        val random = Random(12)

        val data = create()
        for (i in 0 until ChunkSize.BLOCKS_PER_SECTION) {
            val positon = InSectionPosition(i)
            if (random.nextBoolean()) {
                data[positon] = water
            } else if (random.nextBoolean()) {
                data[positon] = stone
            }
        }

        BenchmarkUtil.benchmark(iterations = 199_999) {
            data.recalculate(false)
        }.println()
    }

    // TODO: test initial block set
}
