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
import de.bixilon.kutil.bit.set.ArrayBitSet
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.stream.InputStreamUtil.readAsString
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.TestBlockStates
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.container.block.occlusion.SectionOcclusion
import de.bixilon.minosoft.data.world.container.block.occlusion.SectionOcclusionTracer
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import kotlin.random.Random

@Test(groups = ["occlusion"])
class SectionOcclusionTest {

    private fun create(): SectionOcclusion {
        val blocks = BlockSectionDataProvider::class.java.allocate()
        val occlusion = SectionOcclusion(blocks)
        blocks::occlusion.forceSet(occlusion)
        blocks::fullOpaque.forceSet(ArrayBitSet(ChunkSize.BLOCKS_PER_SECTION))
        return occlusion
    }

    private operator fun SectionOcclusion.set(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, state: BlockState?) {
        for (y in minY..maxY) {
            for (z in minZ..maxZ) {
                for (x in minX..maxX) {
                    provider[x, y, z] = state
                }
            }
        }
    }

    private val SectionOcclusion.occlusion get() = SectionOcclusionTracer.calculate(provider)

    fun `empty section`() {
        val occlusion = create()
        assertEquals(occlusion.occlusion, BooleanArray(15) { false })
    }

    fun `one block set section`() {
        val occlusion = create()
        occlusion.provider[0, 0, 0] = TestBlockStates.OPAQUE1
        assertEquals(occlusion.occlusion, BooleanArray(15) { false })
    }

    fun `full opaque section`() {
        val occlusion = create()
        occlusion[0, 0, 0, 15, 15, 15] = TestBlockStates.OPAQUE1
        assertEquals(occlusion.occlusion, BooleanArray(15) { true })
    }

    fun `full transparent section`() {
        val occlusion = create()
        occlusion[0, 0, 0, 15, 15, 15] = TestBlockStates.TEST1
        assertEquals(occlusion.occlusion, BooleanArray(15) { false })
    }

    fun `bottom line filled opaque`() {
        val occlusion = create()
        occlusion[0, 0, 0, 15, 0, 15] = TestBlockStates.OPAQUE1
        assertEquals(occlusion.occlusion, BooleanArray(15) { if (it <= 4) true else false })
    }

    fun `everything except bottom line filled opaque`() {
        val occlusion = create()
        occlusion[0, 1, 0, 15, 15, 15] = TestBlockStates.OPAQUE1
        assertEquals(occlusion.occlusion, booleanArrayOf(true, false, false, false, false, true, true, true, true, false, false, false, false, false, false))
    }

    fun `y=1 line filled opaque`() {
        val occlusion = create()
        occlusion[0, 1, 0, 15, 1, 15] = TestBlockStates.OPAQUE1
        assertEquals(occlusion.occlusion, BooleanArray(15) { if (it == 0) true else false })
    }

    fun `opaque bottom line filled opaque with one transparent`() {
        val occlusion = create()
        occlusion[0, 0, 0, 15, 0, 15] = TestBlockStates.OPAQUE1
        occlusion[4, 0, 4, 4, 0, 4] = TestBlockStates.TEST1
        assertEquals(occlusion.occlusion, BooleanArray(15) { false })
    }

    fun `stack overflow error`() {
        val occlusion = create()
        val data = SectionOcclusionTest::class.java.getResourceAsStream("/chunk/occlusion_stack_overflow.txt")!!.readAsString().split(';').map { IT.REGISTRIES.block[it]?.states?.default }
        for ((index, state) in data.withIndex()) {
            occlusion.provider[InSectionPosition(index)] = state
        }
        assertEquals(occlusion.occlusion, BooleanArray(15) { false })
    }

    @Test
    fun benchmark() {
        val occlusion = create()
        val random = Random(12)
        for (i in 0 until ChunkSize.BLOCKS_PER_SECTION) {
            if (random.nextBoolean()) {
                occlusion.provider[InSectionPosition(i)] = TestBlockStates.OPAQUE1
            }
        }
        BenchmarkUtil.benchmark(1000) {
            for (i in 0 until 100) {
                SectionOcclusionTracer.calculate(occlusion.provider)
            }
        }.println()
    }

    // TODO: Test more possible cases
}
