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
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition

object FluidCornerHeightUtil {
    private fun ChunkSection.getFluidHeight(fluid: Fluid, position: InSectionPosition, offset: BlockPosition): Float {
        val offset = offset + position
        val up = traceBlock(offset + Directions.UP)
        if (fluid.matches(up)) return 1.0f

        // TODO: check if block has collisions

        return fluid.getHeight(traceBlock(offset))
    }

    fun updateFluidHeights(section: ChunkSection, position: InSectionPosition, fluid: Fluid, heights: FloatArray) {
        heights[0] = section.getFluidHeight(fluid, position, BlockPosition(-1, 0, -1))
        heights[1] = section.getFluidHeight(fluid, position, BlockPosition(+0, 0, -1))
        heights[2] = section.getFluidHeight(fluid, position, BlockPosition(+1, 0, -1))
        heights[3] = section.getFluidHeight(fluid, position, BlockPosition(-1, 0, +0))
        heights[4] = section.getFluidHeight(fluid, position, BlockPosition(+0, 0, +0))
        heights[5] = section.getFluidHeight(fluid, position, BlockPosition(+1, 0, +0))
        heights[6] = section.getFluidHeight(fluid, position, BlockPosition(-1, 0, +1))
        heights[7] = section.getFluidHeight(fluid, position, BlockPosition(+0, 0, +1))
        heights[8] = section.getFluidHeight(fluid, position, BlockPosition(+1, 0, +1))
    }

    fun updateCornerHeights(heights: FloatArray, corners: FloatArray) {
        corners[0] = averageHeight(heights[0], heights[1], heights[3], heights[4])
        corners[1] = averageHeight(heights[1], heights[2], heights[4], heights[5])
        corners[2] = averageHeight(heights[4], heights[5], heights[7], heights[8])
        corners[3] = averageHeight(heights[3], heights[4], heights[6], heights[7])
    }

    private fun averageHeight(a: Float, b: Float, c: Float, d: Float): Float {
        if (a >= 1.0f || b >= 1.0f || c >= 1.0f || d >= 1.0f) {
            return 1.0f
        }

        // TODO

        var total = a + b + c + d
        var count = 4

        if (a >= 0.8f) {
            total += a * 9; count += 9
        }
        if (b >= 0.8f) {
            total += b * 9; count += 9
        }
        if (c >= 0.8f) {
            total += c * 9; count += 9
        }
        if (d >= 0.8f) {
            total += d * 9; count += 9
        }

        return total / count
    }
}
