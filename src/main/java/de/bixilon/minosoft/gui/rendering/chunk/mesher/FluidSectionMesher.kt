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
import de.bixilon.kmath.vec.vec3.f.MVec3f
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
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.FaceCulling.getSideArea
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.side.FaceProperties
import de.bixilon.minosoft.gui.rendering.models.fluid.FluidModel
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV
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

    private fun renderUp(model: FluidModel, velocity: Vec3d, heights: FloatArray, offset: Vec3f, meshes: ChunkMeshesBuilder, lightTint: Float, packedUV: FloatArray) {
        val texture: Texture
        var packedUV = packedUV


        if (velocity.x == 0.0 && velocity.z == 0.0) {
            texture = model.still
            packedUV = STILL_UV_TOP
        } else {
            texture = model.flowing
            val atan = atan2(velocity.x, velocity.z).toFloat() // TODO: cache atan2? velocity is normalized
            val sin = atan.sin * (1.0f / 4.0f)
            val cos = atan.cos * (1.0f / 4.0f)


            val center = 1.0f / 2.0f

            packedUV[0] = PackedUV.pack(center + -cos + sin, center + -cos - sin)
            packedUV[1] = PackedUV.pack(center + +cos + sin, center + -cos + sin)
            packedUV[2] = PackedUV.pack(center + +cos - sin, center + +cos + sin)
            packedUV[3] = PackedUV.pack(center + -cos - sin, center + +cos - sin)
        }
        val textureId = texture.shaderId.buffer()


        val mesh = meshes[texture.transparency]

        mesh.order.iterate { position, uv -> mesh.addVertex(offset.x + POSITIONS_TOP_STILL[position * Vec2f.LENGTH + 0], offset.y + heights[position], offset.z + POSITIONS_TOP_STILL[position * Vec2f.LENGTH + 1], texture.transformUVPacked(packedUV[uv]), textureId, lightTint) }
        mesh.order.iterateReverse { position, uv -> mesh.addVertex(offset.x + POSITIONS_TOP_STILL[position * Vec2f.LENGTH + 0], offset.y + heights[position], offset.z + POSITIONS_TOP_STILL[position * Vec2f.LENGTH + 1], texture.transformUVPacked(packedUV[uv]), textureId, lightTint) }
    }

    private fun renderDown(model: FluidModel, offset: Vec3f, meshes: ChunkMeshesBuilder, lightTint: Float) {
        val texture = model.still
        val packedUV = STILL_UV_TOP

        val textureId = texture.shaderId.buffer()
        val mesh = meshes[texture.transparency]

        mesh.order.iterateReverse { position, uv -> mesh.addVertex(offset.x + POSITIONS_TOP_STILL[position * Vec2f.LENGTH + 0], offset.y, offset.z + POSITIONS_TOP_STILL[position * Vec2f.LENGTH + 1], texture.transformUVPacked(packedUV[uv]), textureId, lightTint) }
    }

    inline fun renderSide(offset: Vec3f, x1: Float, x2: Float, z1: Float, z2: Float, height1: Float, height2: Float, cull: FluidCull, overlay: Texture?, textureId: Float, mesh: ChunkMesh, lightTint: Float, positions: FloatArray, packedUV: FloatArray) {
        if (cull == FluidCull.CULLED) return
        packedUV[2] = PackedUV.pack(0.5f, (1.0f - height1) * 0.5f) // TODO: transformUV
        packedUV[3] = PackedUV.pack(0.0f, (1.0f - height2) * 0.5f)


        positions[0] = x1; positions[1] = 0.0f; positions[2] = z1
        positions[3] = x2; positions[4] = 0.0f; positions[5] = z2
        positions[6] = x2; positions[7] = height2; positions[8] = z2
        positions[9] = x1; positions[10] = height1; positions[11] = z1

        if (cull == FluidCull.OVERLAY && overlay != null) {
            val textureId = overlay.renderData.shaderTextureId.buffer()
            mesh.order.iterate { position, uv -> mesh.addVertex(offset.x + positions[position * Vec3f.LENGTH + 0], offset.y + positions[position * Vec3f.LENGTH + 1], offset.z + positions[position * Vec3f.LENGTH + 2], packedUV[uv], textureId, lightTint) }
        } else {
            mesh.order.iterate { position, uv -> mesh.addVertex(offset.x + positions[position * Vec3f.LENGTH + 0], offset.y + positions[position * Vec3f.LENGTH + 1], offset.z + positions[position * Vec3f.LENGTH + 2], packedUV[uv], textureId, lightTint) }
            mesh.order.iterateReverse { position, uv -> mesh.addVertex(offset.x + positions[position * Vec3f.LENGTH + 0], offset.y + positions[position * Vec3f.LENGTH + 1], offset.z + positions[position * Vec3f.LENGTH + 2], packedUV[uv], textureId, lightTint) }
        }
    }

    private fun canCull(section: ChunkSection, position: InSectionPosition, direction: Directions, fluid: Fluid, height: Float): FluidCull {
        val state = section.traceBlock(position, direction) ?: return FluidCull.VISIBLE
        if (fluid.matches(state)) return FluidCull.CULLED


        // copy from FaceCulling::canCull
        val model = state.model ?: state.block.model ?: return FluidCull.VISIBLE
        val properties = model.getProperties(direction) ?: return FluidCull.VISIBLE // not touching side

        val side = FaceProperties(Vec2f(0.0f, 0.0f), Vec2f(1.0f, height), transparency = TextureTransparencies.TRANSPARENT) // TODO: remove allocation

        val surface = (1.0f - 0.0f) * (height - 0.0f) // TODO: simplify

        val area = properties.getSideArea(side)
        val covered = surface <= area

        if (!covered) return FluidCull.VISIBLE

        // TODO: CustomBlockCulling

        if (properties.transparency == TextureTransparencies.OPAQUE) {
            return FluidCull.CULLED // impossible to see that face
        }

        return FluidCull.OVERLAY
    }

    fun mesh(chunk: Chunk, section: ChunkSection, mesh: ChunkMeshesBuilder) {
        val blocks = section.blocks

        context.camera.offset.offset

        val cameraOffset = context.camera.offset.offset


        val heights = FloatArray(3 * 3)
        val corners = FloatArray(4)
        val velocity = MVec3d()
        val packedUV = FloatArray(PackedUV.SIZE)
        val positions = FloatArray(4 * Vec3f.LENGTH)
        val offsetPosition = MVec3f()

        for (y in blocks.minPosition.y..blocks.maxPosition.y) {
            for (z in blocks.minPosition.z..blocks.maxPosition.z) {
                for (x in blocks.minPosition.x..blocks.maxPosition.x) {
                    val inSection = InSectionPosition(x, y, z)
                    val state = blocks[inSection] ?: continue
                    val fluid = state.getFluid() ?: continue

                    val model = fluid.model ?: continue

                    val height = fluid.getHeight(state)
                    if (height <= 0.0f) continue

                    val up = !fluid.matches(section.traceBlock(inSection, Directions.UP))
                    val down = canCull(section, inSection, Directions.DOWN, fluid, height)
                    val north = canCull(section, inSection, Directions.NORTH, fluid, height)
                    val south = canCull(section, inSection, Directions.SOUTH, fluid, height)
                    val west = canCull(section, inSection, Directions.WEST, fluid, height)
                    val east = canCull(section, inSection, Directions.EAST, fluid, height)


                    val sides = down != FluidCull.CULLED || north != FluidCull.CULLED || south != FluidCull.CULLED || west != FluidCull.CULLED && east == FluidCull.CULLED

                    if (!up && !sides) {
                        continue
                    }


                    updateFluidHeights(section, inSection, fluid, heights)
                    updateCornerHeights(heights, corners)
                    val position = BlockPosition.of(chunk.position, section.height, inSection)

                    offsetPosition.x = (position.x - cameraOffset.x).toFloat()
                    offsetPosition.y = (position.y - cameraOffset.y).toFloat()
                    offsetPosition.z = (position.z - cameraOffset.z).toFloat()


                    val tint = tints.getFluidTint(chunk, fluid, height, position)
                    var light = section.light[inSection]
                    if (position.y >= chunk.light.heightmap[inSection.xz]) {
                        light = light.with(sky = MAX_LEVEL)
                    }

                    val lightTint = (((light.index shl 24) or tint.rgb).buffer())


                    if (up) {
                        fluid.updateVelocity(state, position, chunk, velocity)
                        renderUp(model, velocity.unsafe, corners, offsetPosition.unsafe, mesh, lightTint, packedUV)
                    }
                    if (down != FluidCull.CULLED) {
                        renderDown(model, offsetPosition.unsafe, mesh, lightTint)
                    }
                    if (sides) {
                        val flowing = model.flowing
                        packedUV[0] = PackedUV.pack(0.0f, 0.5f)
                        packedUV[1] = PackedUV.pack(0.5f, 0.5f)
                        val mesh = mesh[flowing.transparency]
                        val textureId = flowing.renderData.shaderTextureId.buffer()
                        val overlay = model.overlay

                        renderSide(offsetPosition.unsafe, 0.0f, 1.0f, 0.0f, 0.0f, corners[0], corners[1], north, overlay, textureId, mesh, lightTint, positions, packedUV)
                        renderSide(offsetPosition.unsafe, 1.0f, 0.0f, 1.0f, 1.0f, corners[2], corners[3], south, overlay, textureId, mesh, lightTint, positions, packedUV)
                        renderSide(offsetPosition.unsafe, 0.0f, 0.0f, 1.0f, 0.0f, corners[3], corners[0], west, overlay, textureId, mesh, lightTint, positions, packedUV)
                        renderSide(offsetPosition.unsafe, 1.0f, 1.0f, 0.0f, 1.0f, corners[1], corners[2], east, overlay, textureId, mesh, lightTint, positions, packedUV)
                    }

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

    enum class FluidCull {
        VISIBLE,
        OVERLAY,
        CULLED,
    }

    companion object {
        val POSITIONS_TOP_STILL = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
        )
        val STILL_UV_TOP = UnpackedUV(POSITIONS_TOP_STILL).pack().raw
    }
}
