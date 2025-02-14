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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.data.registries.blocks.GlassTest0
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.stone.StoneTest0
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import kotlin.random.Random
import kotlin.time.measureTime

@Test(groups = ["occlusion"], dependsOnGroups = ["block"])
class SectionOcclusionTest {
    private val OCCLUSION = SectionOcclusion::class.java.getFieldOrNull("occlusion")!!
    private val CALCULATE = SectionOcclusion::class.java.getFieldOrNull("calculate")!!
    private val opaque by lazy { StoneTest0.block.states.default }
    private val transparent by lazy { GlassTest0.block.states.default }

    private fun create(): SectionOcclusion {
        val blocks = BlockSectionDataProvider::class.java.allocate()
        val occlusion = SectionOcclusion(blocks)
        blocks::occlusion.forceSet(occlusion)
        return occlusion
    }

    private operator fun SectionOcclusion.set(minX: Int, minY: Int, minZ: Int, maxX: Int, maxY: Int, maxZ: Int, state: BlockState?) {
        CALCULATE.setBoolean(this, false)
        for (y in minY..maxY) {
            for (z in minZ..maxZ) {
                for (x in minX..maxX) {
                    provider[x, y, z] = state
                }
            }
        }
        CALCULATE.setBoolean(this, true)
    }

    private val SectionOcclusion.occlusion: BooleanArray
        get() {
            recalculate(false)
            return OCCLUSION[this].unsafeCast()
        }

    fun `empty section`() {
        val occlusion = create()
        assertEquals(occlusion.occlusion, BooleanArray(15) { false })
    }

    fun `full opaque section`() {
        val occlusion = create()
        occlusion[0, 0, 0, 15, 15, 15] = opaque
        assertEquals(occlusion.occlusion, BooleanArray(15) { true })
    }

    fun `full transparent section`() {
        val occlusion = create()
        occlusion[0, 0, 0, 15, 15, 15] = transparent
        assertEquals(occlusion.occlusion, BooleanArray(15) { false })
    }

    fun `bottom line filled opaque`() {
        val occlusion = create()
        occlusion[0, 0, 0, 15, 0, 15] = opaque
        assertEquals(occlusion.occlusion, BooleanArray(15) { if (it <= 4) true else false })
    }

    fun `y=1 line filled opaque`() {
        val occlusion = create()
        occlusion[0, 1, 0, 15, 1, 15] = opaque
        assertEquals(occlusion.occlusion, BooleanArray(15) { if (it == 0) true else false })
    }

    fun `opaque bottom line filled opaque with one transparent`() {
        val occlusion = create()
        occlusion[0, 0, 0, 15, 0, 15] = opaque
        occlusion[4, 0, 4, 4, 0, 4] = transparent
        assertEquals(occlusion.occlusion, BooleanArray(15) { false })
    }

    @Test(enabled = false)
    fun benchmark() {
        val occlusion = create()
        val stone = StoneTest0.state
        val random = Random(12)
        for (i in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            if (random.nextBoolean()) {
                occlusion.provider[i] = stone
            }
        }
        CALCULATE.setBoolean(occlusion, true)
        val time = measureTime {
            for (i in 0 until 500_000) {
                occlusion.recalculate(false)
            }
        }
        println("Took: ${time.inWholeNanoseconds.formatNanos()}")
    }

    // TODO: Test more possible cases
}
