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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.settings.BlockSettings
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.fluid.water.WaterFluidBlock
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakedModel
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["mesher"], dependsOnGroups = ["block", "fluid"])
class FluidCullingTest {
    private val water by lazy { IT.REGISTRIES.fluid[WaterFluid]!! }
    private val stone by lazy { createBlock() }
    private val half by lazy { createBlock(properties = FaceProperties(Vec2f(0.0f), Vec2f(1.0f, 0.5f), TextureTransparencies.OPAQUE)) }
    private val glass by lazy { createBlock(transparency = TextureTransparencies.TRANSPARENT) }

    private fun getCulled(state: BlockState, height: Float = 0.8f): FluidCull {
        return FluidCulling.canFluidCull(state, Directions.NORTH, this.water, height)
    }

    /*
    fun `cull side air`() {
        assertEquals(getCulled(null), FluidCull.VISIBLE)
    }
     */


    private fun createBlock(transparency: TextureTransparencies = TextureTransparencies.OPAQUE, properties: FaceProperties? = FaceProperties(Vec2f(0), Vec2f(1), transparency)): BlockState {
        val block = object : Block(minosoft("dummy"), BlockSettings(IT.VERSION)) {
            override val hardness: Float get() = Broken()
        }
        val state = BlockState(block, 0)

        val properties = properties?.let { SideProperties(arrayOf(it), it.transparency) }

        state.model = BakedModel(Array(Directions.SIZE) { emptyArray() }, arrayOf(properties, properties, properties, properties, properties, properties), null, null)

        return state
    }

    fun `cull side same fluid`() {
        assertEquals(getCulled(IT.REGISTRIES.block[WaterFluidBlock]!!.states.default), FluidCull.CULLED)
    }

    fun `cull side full opaque`() {
        assertEquals(getCulled(stone), FluidCull.CULLED)
    }

    fun `cull side below height`() {
        assertEquals(getCulled(half, 0.3f), FluidCull.CULLED)
    }

    fun `cull side above height`() {
        assertEquals(getCulled(half, 0.6f), FluidCull.VISIBLE)
    }

    fun `cull side full transparent`() {
        assertEquals(getCulled(glass), FluidCull.OVERLAY)
    }

    // TODO: culling up/down
}
