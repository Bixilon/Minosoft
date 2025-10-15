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

package de.bixilon.minosoft.gui.rendering.chunk.mesher.fluid

import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.minosoft.data.registries.blocks.types.building.plants.FernBlock
import de.bixilon.minosoft.data.registries.blocks.types.building.stone.StoneBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.WaterFluidBlock
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesher.fluid.FluidCornerHeightUtil.updateCornerHeights
import de.bixilon.minosoft.gui.rendering.chunk.mesher.fluid.FluidCornerHeightUtil.updateFluidHeights
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["mesher"], dependsOnGroups = ["block", "fluid"])
class FluidCornerHeightUtilTest {
    private val solid by lazy { IT.REGISTRIES.block[StoneBlock.Block]!!.states.default }
    private val other by lazy { IT.REGISTRIES.block[FernBlock.Grass]!!.states.default }
    private val water by lazy { IT.REGISTRIES.block[WaterFluidBlock]!!.states.default.withProperties(FluidBlock.LEVEL to 0) }
    private val water2 by lazy { IT.REGISTRIES.block[WaterFluidBlock]!!.states.default.withProperties(FluidBlock.LEVEL to 2) }
    private val water7 by lazy { IT.REGISTRIES.block[WaterFluidBlock]!!.states.default.withProperties(FluidBlock.LEVEL to 7) }
    private val fluid by lazy { IT.REGISTRIES.fluid[WaterFluid]!! }


    private fun create(): BlockSectionDataProvider {
        val chunk = Chunk::class.java.allocate()
        chunk::lock.forceSet(RWLock.rwlock())
        val section = ChunkSection(0, chunk)

        return section.blocks
    }

    private fun BlockSectionDataProvider.cornerHeight(): FloatArray {
        val heights = FloatArray(3 * 3)
        val corners = FloatArray(4)

        updateFluidHeights(section, InSectionPosition(1, 0, 1), fluid, heights)
        updateCornerHeights(heights, corners)

        return corners
    }


    fun `single surrounded block`() {
        val section = create()

        section[0, 0, 1] = solid
        section[1, 0, 0] = solid
        section[2, 0, 1] = solid
        section[1, 0, 2] = solid

        section[1, 0, 1] = water


        val corners = section.cornerHeight()
        assertEquals(corners, floatArrayOf(0.80808085f, 0.80808085f, 0.80808085f, 0.80808085f))
    }

    fun `water no surroundings`() {
        val section = create()

        section[1, 0, 1] = water


        val corners = section.cornerHeight()
        assertEquals(corners, floatArrayOf(0.6837607f, 0.6837607f, 0.6837607f, 0.6837607f))
    }

    fun `water other block surrounding`() {
        val section = create()

        section[0, 0, 1] = other
        section[1, 0, 0] = other
        section[2, 0, 1] = other
        section[1, 0, 2] = other

        section[1, 0, 1] = water


        val corners = section.cornerHeight()
        assertEquals(corners, floatArrayOf(0.6837607f, 0.6837607f, 0.6837607f, 0.6837607f))
    }

    fun `water above`() {
        val section = create()

        section[1, 0, 1] = water
        section[1, 1, 1] = water


        val corners = section.cornerHeight()
        assertEquals(corners, floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f))
    }

    fun `water next to level 2 south`() {
        val section = create()

        section[1, 0, 1] = water2
        section[1, 0, 2] = water7


        val corners = section.cornerHeight()
        assertEquals(corners, floatArrayOf(NORMAL, NORMAL, LOWER, LOWER))
    }

    fun `water next to level 2 north`() {
        val section = create()

        section[1, 0, 1] = water2
        section[1, 0, 0] = water7


        val corners = section.cornerHeight()
        assertEquals(corners, floatArrayOf(LOWER, LOWER, NORMAL, NORMAL))
    }

    fun `water next to level 2 west`() {
        val section = create()

        section[1, 0, 1] = water2
        section[0, 0, 1] = water7


        val corners = section.cornerHeight()
        assertEquals(corners, floatArrayOf(LOWER, NORMAL, NORMAL, LOWER))
    }

    fun `water next to level 2 east`() {
        val section = create()

        section[1, 0, 1] = water2
        section[2, 0, 1] = water7


        val corners = section.cornerHeight()
        assertEquals(corners, floatArrayOf(NORMAL, LOWER, LOWER, NORMAL))
    }


    companion object {
        // They are so low, because there are no opaque blocks next here
        val NORMAL = 0.16666667f
        val LOWER = 0.19444445f
    }
}
