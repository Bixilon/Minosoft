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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid.Companion.isWaterlogged
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel.Companion.MAX_LEVEL
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder
import de.bixilon.minosoft.gui.rendering.models.fluid.FluidModel
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.rotate
import de.bixilon.minosoft.util.KUtil.cos
import de.bixilon.minosoft.util.KUtil.sin
import kotlin.math.atan2

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


    private fun renderUp(section: ChunkSection, position: InSectionPosition, model: FluidModel, velocity: Vec3d, heights: FloatArray, offset: Vec3f, meshes: ChunkMeshesBuilder, lightTint: Float) {
        var texture: Texture


        val textureUV: PackedUV
        if (velocity.x == 0.0 && velocity.z == 0.0) {
            texture = model.still
            textureUV = STILL_UV_TOP
            // still
        } else {
            // flowing
            texture = model.flowing
            val atan = atan2(velocity.x, velocity.z).toFloat()
            val sin = atan.sin
            val cos = atan.cos

            val TEXTURE_CENTER = 1.0f / 2.0f

            val raw = floatArrayOf(
                0.0f, 0.0f,
                1.0f, 0.0f,
                1.0f, 1.0f,
                0.0f, 1.0f,
            )


            for (i in 0 until PackedUV.SIZE) {
                rotate(raw[i * Vec2f.LENGTH + 0] - TEXTURE_CENTER, raw[i * Vec2f.LENGTH + 1] - TEXTURE_CENTER, sin, cos, false) { x, y -> raw[i * Vec2f.LENGTH + 0] = x + TEXTURE_CENTER; raw[i * Vec2f.LENGTH + 1] = y + TEXTURE_CENTER }
            }

            textureUV = UnpackedUV(raw).pack()
        }
        val textureId = texture.shaderId.buffer()


        val mesh = meshes[model.still.transparency]

        mesh.order.iterate { position, uv ->
            val packed = texture.transformUVPacked(textureUV.raw[uv])
            mesh.addVertex(offset.x + POSITIONS_TOP[position * Vec2f.LENGTH + 0], offset.y + heights[position], offset.z + POSITIONS_TOP[position * Vec2f.LENGTH + 1], packed, textureId, lightTint)
        }
        mesh.order.iterateReverse { position, uv ->
            val packed = texture.transformUVPacked(textureUV.raw[uv])
            mesh.addVertex(offset.x + POSITIONS_TOP[position * Vec2f.LENGTH + 0], offset.y + heights[position], offset.z + POSITIONS_TOP[position * Vec2f.LENGTH + 1], packed, textureId, lightTint)
        }
    }

    private fun canCull(section: ChunkSection, position: InSectionPosition, direction: Directions, fluid: Fluid): FluidCull {
        val state = section.traceBlock(position, direction) ?: return FluidCull.VISIBLE
        if (fluid.matches(state)) return FluidCull.CULLED


        // copy from FaceCulling::canCull
        val model = state.model ?: state.block.model ?: return FluidCull.VISIBLE
        val properties = model.getProperties(direction) ?: return FluidCull.VISIBLE // not touching side

        // TODO: check neighbour size

        when {
            properties.transparency == TextureTransparencies.OPAQUE -> return FluidCull.CULLED // impossible to see that face
            // TODO: other transparency values
        }

        // TODO: CustomBlockCulling

        return FluidCull.VISIBLE
    }

    // ToDo: Should this be combined with the solid renderer (but we'd need to render faces twice, because of cullface)
    fun mesh(chunk: Chunk, section: ChunkSection, mesh: ChunkMeshesBuilder) {
        val blocks = section.blocks

        context.camera.offset.offset

        val cameraOffset = context.camera.offset.offset


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
                    val up = !fluid.matches(section.traceBlock(inSection, Directions.UP))
                    val down = canCull(section, inSection, Directions.DOWN, fluid)
                    val north = canCull(section, inSection, Directions.NORTH, fluid)
                    val south = canCull(section, inSection, Directions.SOUTH, fluid)
                    val west = canCull(section, inSection, Directions.WEST, fluid)
                    val east = canCull(section, inSection, Directions.EAST, fluid)

                    if (!up && down == FluidCull.CULLED && north == FluidCull.CULLED && south == FluidCull.CULLED && west == FluidCull.CULLED && east == FluidCull.CULLED) {
                        continue
                    }


                    updateFluidHeights(section, inSection, fluid, heights)
                    updateCornerHeights(heights, corners)
                    val height = fluid.getHeight(state) // TODO: remove
                    val position = BlockPosition.of(chunk.position, section.height, inSection)
                    val offsetPosition = Vec3f(position - cameraOffset)


                    val tint = tints.getFluidTint(chunk, fluid, height, position)
                    var light = section.light[inSection]
                    if (position.y >= chunk.light.heightmap[inSection.xz]) {
                        light = light.with(sky = MAX_LEVEL)
                    }

                    val lightTint = (((light.index shl 24) or tint.rgb).buffer())


                    if (up) {
                        fluid.updateVelocity(state, position, chunk, velocity)
                        renderUp(section, inSection, model, velocity.unsafe, corners, offsetPosition, mesh, lightTint)
                    }

                    // TODO: sides, down

                    mesh.addBlock(x, y, z)

                    if (Thread.interrupted()) throw InterruptedException()
                }
            }
        }
    }

    private fun ChunkSection.getFluidHeight(fluid: Fluid, position: InSectionPosition, offset: BlockPosition): Float {
        val offset = offset + position
        val up = traceBlock(offset + Directions.UP)
        if (fluid.matches(up)) return 1.0f

        // TODO: check if block has collisions

        return fluid.getHeight(traceBlock(offset))
    }

    private fun updateFluidHeights(section: ChunkSection, position: InSectionPosition, fluid: Fluid, heights: FloatArray) {
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

    private fun updateCornerHeights(heights: FloatArray, corners: FloatArray) {
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
        var count = 0

        if (a > 0.0f) count++
        if (b > 0.0f) count++
        if (c > 0.0f) count++
        if (d > 0.0f) count++

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

    enum class FluidCull {
        VISIBLE,
        OVERLAY,
        CULLED,
    }

    companion object {
        val POSITIONS_TOP = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
        )
        val STILL_UV_TOP = UnpackedUV(POSITIONS_TOP).pack()
    }
}
