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

package de.bixilon.minosoft.gui.rendering.chunk.mesher

import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid.Companion.isWaterlogged
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder

class FluidSectionMesher(
    val context: RenderContext,
) {
    private val water = context.session.registries.fluid[WaterFluid]
    private val tints = context.tints

    private fun BlockState.getFluid() = when {
        BlockStateFlags.FLUID !in flags -> null
        water != null && isWaterlogged() -> water
        block is FluidHolder -> block.fluid
        else -> null
    }


    // ToDo: Should this be combined with the solid renderer (but we'd need to render faces twice, because of cullface)
    fun mesh(chunk: Chunk, section: ChunkSection, mesh: ChunkMeshesBuilder) {
        val blocks = section.blocks

        context.camera.offset.offset

        val offsetX = chunk.position.x * ChunkSize.SECTION_WIDTH_X
        val offsetY = section.height * ChunkSize.SECTION_HEIGHT_Y
        val offsetZ = chunk.position.z * ChunkSize.SECTION_WIDTH_Z

        BooleanArray(Directions.VALUES.size)

        for (y in blocks.minPosition.y..blocks.maxPosition.y) {
            for (z in blocks.minPosition.z..blocks.maxPosition.z) {
                for (x in blocks.minPosition.x..blocks.maxPosition.x) {
                    val inSection = InSectionPosition(x, y, z)
                    val state = blocks[inSection] ?: continue
                    val fluid = state.getFluid() ?: continue

                    fluid.model ?: continue
                    fluid.getHeight(state)


                    mesh.addBlock(x, y, z)
                    if (Thread.interrupted()) throw InterruptedException()
                }
            }
        }
    }
    private fun ChunkSection.getFluidHeight(fluid: Fluid, position: InSectionPosition, offset: BlockPosition): Float {
        val offset = offset - position
        val up = traceBlock(offset + Directions.UP)
        if (fluid.matches(up)) return 1.0f

        return fluid.getHeight(traceBlock(offset))
    }

    private fun getFluidHeights(section: ChunkSection, position: InSectionPosition, fluid: Fluid) {
        val heights = FloatArray(9)

        heights[0] = section.getFluidHeight(fluid, position, BlockPosition(0, 0, 0)) // TODO

    }

    private fun getCornerHeights(heights: FloatArray) {
        FloatArray(4)


    }
}
