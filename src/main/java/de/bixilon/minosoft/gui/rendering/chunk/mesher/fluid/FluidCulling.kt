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

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.SideProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies

object FluidCulling {

    // TODO: merge with DirectedProperty
    private fun SideProperties.getSideArea(targetEndY: Float): Float {
        // overlapping is broken, see https://stackoverflow.com/questions/7342935/algorithm-to-compute-total-area-covered-by-a-set-of-overlapping-segments
        var area = 0.0f

        for (quad in this.faces) {
            area += quad.getSideArea(targetEndY)
        }

        return area
    }

    private fun FaceProperties.getSideArea(targetEndY: Float): Float {
        val width = end.x - start.x
        val height = minOf(targetEndY, end.y) - start.y

        return width * height
    }

    fun canFluidCull(state: BlockState, direction: Directions, fluid: Fluid, height: Float): FluidCull {
        if (fluid.matches(state)) return FluidCull.CULLED


        // copy from FaceCulling::canCull
        val model = state.model ?: state.block.model ?: return FluidCull.VISIBLE
        val properties = model.getProperties(direction) ?: return FluidCull.VISIBLE // not touching side

        val area = properties.getSideArea(height)

        if (height > area) return FluidCull.VISIBLE // height == surface -> isCovered

        // TODO: CustomBlockCulling

        if (properties.transparency == TextureTransparencies.OPAQUE) {
            return FluidCull.CULLED // impossible to see that face
        }

        return FluidCull.OVERLAY
    }

    fun canFluidCull(section: ChunkSection, position: InSectionPosition, direction: Directions, fluid: Fluid, height: Float): FluidCull {
        val state = section.traceBlock(position, direction) ?: return FluidCull.VISIBLE

        return canFluidCull(state, direction.inverted, fluid, height)
    }
}
