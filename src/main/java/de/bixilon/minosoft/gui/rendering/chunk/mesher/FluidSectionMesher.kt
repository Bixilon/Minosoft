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

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
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
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder
import de.bixilon.minosoft.gui.rendering.models.fluid.FluidModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies

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


    private fun renderUp(section: ChunkSection, position: InSectionPosition, model: FluidModel, velocity: Vec3d, heights: FloatArray) {
        if (velocity.x == 0.0 && velocity.z == 0.0) {
            // still
        } else {
            // flowing
        }
    }

    private fun canCull(section: ChunkSection, position: InSectionPosition, direction: Directions, fluid: Fluid): Boolean {
        val state = section.traceBlock(position, direction) ?: return false
        if (fluid.matches(state)) return false


        // copy from FaceCulling::canCull
        val model = state.model ?: state.block.model ?: return false
        val properties = model.getProperties(direction) ?: return false // not touching side


        when {
            properties.transparency == TextureTransparencies.OPAQUE -> return true // impossible to see that face
            // TODO: other transparency values
        }

        // TODO: cullface changing (only visible form one side)

        // TODO: CustomBlockCulling

        return true
    }

    // ToDo: Should this be combined with the solid renderer (but we'd need to render faces twice, because of cullface)
    fun mesh(chunk: Chunk, section: ChunkSection, mesh: ChunkMeshesBuilder) {
        val blocks = section.blocks

        context.camera.offset.offset

        chunk.position.x * ChunkSize.SECTION_WIDTH_X
        section.height * ChunkSize.SECTION_HEIGHT_Y
        chunk.position.z * ChunkSize.SECTION_WIDTH_Z


        val heights = FloatArray(3 * 3)
        val corners = FloatArray(4)
        val velocity = MVec3d()

        for (y in blocks.minPosition.y..blocks.maxPosition.y) {
            for (z in blocks.minPosition.z..blocks.maxPosition.z) {
                for (x in blocks.minPosition.x..blocks.maxPosition.x) {
                    val inSection = InSectionPosition(x, y, z)
                    val state = blocks[inSection] ?: continue
                    val fluid = state.getFluid() ?: continue

                    val model = fluid.model ?: continue

                    // TODO: down (only rendered when no block or non opaque block)
                    val up = fluid.matches(section.traceBlock(inSection, Directions.UP))
                    val north = canCull(section, inSection, Directions.NORTH, fluid)
                    val south = canCull(section, inSection, Directions.SOUTH, fluid)
                    val west = canCull(section, inSection, Directions.WEST, fluid)
                    val east = canCull(section, inSection, Directions.EAST, fluid)

                    if (up && north && south && west && east) {
                        continue
                    }

                    updateFluidHeights(section, inSection, fluid, heights)
                    updateCornerHeights(heights, corners)
                    val height = fluid.getHeight(state) // TODO: remove
                    val position = BlockPosition.of(chunk.position, section.height, inSection)

                    tints.getFluidTint(chunk, fluid, height, position)


                    if (up) {
                        fluid.getVelocity(state, position, chunk, velocity)
                        renderUp(section, inSection, model, velocity.unsafe, heights)
                    }

                    // TODO: sides, down

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

    private fun updateFluidHeights(section: ChunkSection, position: InSectionPosition, fluid: Fluid, heights: FloatArray) {
        heights[0] = section.getFluidHeight(fluid, position, BlockPosition(0, 0, 0)) // TODO
        // TODO
    }

    private fun updateCornerHeights(heights: FloatArray, corners: FloatArray) {
        corners[0] = averageHeight(heights[0], heights[1], heights[2], heights[3]) // TODO
        // TODO
    }

    private fun averageHeight(a: Float, b: Float, c: Float, d: Float): Float {
        if (a >= 1.0f || b >= 1.0f || c >= 1.0f || d >= 1.0f) {
            return 1.0f
        }

        var total = 0.0f
        var count = 0

        if (a > 0.0f) {
            val multiplier = (if (a >= 0.8f) 10 else 1)
            total += a * multiplier
            count += 1 * multiplier
        }
        if (b > 0.0f) {
            val multiplier = (if (b >= 0.8f) 10 else 1)
            total += b * multiplier
            count += 1 * multiplier
        }
        if (c > 0.0f) {
            val multiplier = (if (c >= 0.8f) 10 else 1)
            total += c * multiplier
            count += 1 * multiplier
        }
        if (d > 0.0f) {
            val multiplier = (if (d >= 0.8f) 10 else 1)
            total += d * multiplier
            count += 1 * multiplier
        }

        return total / count
    }
}
