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
import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.primitive.FloatUtil.cos
import de.bixilon.kutil.primitive.FloatUtil.sin
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid.Companion.isWaterlogged
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshBuilder
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshesBuilder
import de.bixilon.minosoft.gui.rendering.chunk.mesh.details.ChunkMeshDetails
import de.bixilon.minosoft.gui.rendering.chunk.mesher.fluid.FluidCornerHeightUtil.updateCornerHeights
import de.bixilon.minosoft.gui.rendering.chunk.mesher.fluid.FluidCornerHeightUtil.updateFluidHeights
import de.bixilon.minosoft.gui.rendering.chunk.mesher.fluid.FluidCulling.canFluidCull
import de.bixilon.minosoft.gui.rendering.models.fluid.FluidModel
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadConsumer.Companion.iterate
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.PackedUVArray
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.array.UnpackedUVArray
import kotlin.math.atan2

class FluidSectionMesher(
    val context: RenderContext,
) {
    private val water = context.session.registries.fluid[WaterFluid.Companion]
    private val tints = context.tints

    private fun BlockState.getFluid() = when {
        BlockStateFlags.FLUID !in flags -> null
        water != null && isWaterlogged() -> water
        block is FluidHolder -> block.fluid
        else -> null
    }

    private fun renderUp(model: FluidModel, velocity: Vec3d, heights: FloatArray, offset: Vec3f, meshes: ChunkMeshesBuilder, lightTint: Int, packedUV: PackedUVArray) {
        val up = ChunkMeshDetails.SIDE_UP in meshes.details
        val down = ChunkMeshDetails.SIDE_DOWN in meshes.details

        if (!up && !down) return

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

            packedUV[0] = PackedUV(center + -cos + sin, center + -cos - sin)
            packedUV[1] = PackedUV(center + +cos + sin, center + -cos + sin)
            packedUV[2] = PackedUV(center + +cos - sin, center + +cos + sin)
            packedUV[3] = PackedUV(center + -cos - sin, center + +cos - sin)
        }

        val mesh = meshes[texture.transparency]

        mesh.iterate { mesh.addVertex(offset.x + POSITIONS_TOP_STILL[it * Vec2f.LENGTH + 0], offset.y + heights[it], offset.z + POSITIONS_TOP_STILL[it * Vec2f.LENGTH + 1], packedUV[it].raw, texture, lightTint) }

        mesh.addIndexQuad(up, down)
    }

    private fun renderDown(model: FluidModel, offset: Vec3f, meshes: ChunkMeshesBuilder, lightTint: Int) {
        val texture = model.still
        val packedUV = STILL_UV_TOP

        val mesh = meshes[texture.transparency]

        mesh.iterate { mesh.addVertex(offset.x + POSITIONS_TOP_STILL[it * Vec2f.LENGTH + 0], offset.y, offset.z + POSITIONS_TOP_STILL[it * Vec2f.LENGTH + 1], texture.transformUV(packedUV[it]).raw, texture, lightTint) }
        mesh.addIndexQuad(false, true)
    }

    inline fun renderSide(offset: Vec3f, x1: Float, x2: Float, z1: Float, z2: Float, height1: Float, height2: Float, cull: FluidCull, texture: Texture, overlay: Texture?, mesh: ChunkMeshBuilder, lightTint: Int, positions: FloatArray, packedUV: PackedUVArray) {
        if (cull == FluidCull.CULLED) return
        packedUV[2] = PackedUV(0.5f, (1.0f - height2) * 0.5f)
        packedUV[3] = PackedUV(0.0f, (1.0f - height1) * 0.5f)


        positions[0] = x1; positions[1] = 0.0f; positions[2] = z1
        positions[3] = x2; positions[4] = 0.0f; positions[5] = z2
        positions[6] = x2; positions[7] = height2; positions[8] = z2
        positions[9] = x1; positions[10] = height1; positions[11] = z1

        var backface = true
        var texture = texture
        if (cull == FluidCull.OVERLAY && overlay != null) {
            backface = false
            texture = overlay
        }

        mesh.iterate { mesh.addVertex(offset.x + positions[it * Vec3f.LENGTH + 0], offset.y + positions[it * Vec3f.LENGTH + 1], offset.z + positions[it * Vec3f.LENGTH + 2], texture.transformUV(packedUV[it]).raw, texture, lightTint) }
        mesh.addIndexQuad(true, backface)
    }

    fun mesh(section: ChunkSection, builder: ChunkMeshesBuilder) {
        val blocks = section.blocks
        val chunk = section.chunk

        context.camera.offset.offset

        val cameraOffset = context.camera.offset.offset


        val heights = FloatArray(3 * 3)
        val corners = FloatArray(4)
        val velocity = MVec3d()
        val packedUV = PackedUVArray()
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

                    val position = BlockPosition.of(chunk.position, section.height, inSection)

                    var light = section.light[inSection]
                    if (position.y >= chunk.light.heightmap[inSection.xz]) {
                        light = light.with(sky = LightLevel.MAX_LEVEL)
                    }
                    if (BlockStateFlags.CAVE_SURFACE in state.flags && ChunkMeshDetails.DARK_CAVE_SURFACE !in builder.details && light == LightLevel.EMPTY) continue // TODO: only check sky light?


                    val up = !fluid.matches(section.traceBlock(inSection, Directions.UP))
                    // TODO: height depends on the corners. Do the culling twice?
                    val down = canFluidCull(section, inSection, Directions.DOWN, fluid, 1.0f)
                    val north = canFluidCull(section, inSection, Directions.NORTH, fluid, 1.0f)
                    val south = canFluidCull(section, inSection, Directions.SOUTH, fluid, 1.0f)
                    val west = canFluidCull(section, inSection, Directions.WEST, fluid, 1.0f)
                    val east = canFluidCull(section, inSection, Directions.EAST, fluid, 1.0f)


                    val sides = north != FluidCull.CULLED || south != FluidCull.CULLED || west != FluidCull.CULLED || east == FluidCull.CULLED

                    if (!up && down == FluidCull.CULLED && !sides) {
                        continue
                    }

                    if (ChunkMeshDetails.FLUID_HEIGHTS in builder.details) {
                        updateFluidHeights(section, inSection, fluid, heights)
                        updateCornerHeights(heights, corners)
                    } else {
                        val height = if (up) 0.88888896f else 1.0f
                        corners.fill(height)
                    }
                    offsetPosition.x = (position.x - cameraOffset.x).toFloat()
                    offsetPosition.y = (position.y - cameraOffset.y).toFloat()
                    offsetPosition.z = (position.z - cameraOffset.z).toFloat()


                    val tint = tints.getFluidTint(chunk, fluid, height, position)
                    val lightTint = (light.index shl 24) or tint.rgb


                    if (up) {
                        if (ChunkMeshDetails.FLOWING_FLUID in builder.details) {
                            fluid.updateVelocity(state, position, chunk, velocity)
                        }
                        renderUp(model, velocity.unsafe, corners, offsetPosition.unsafe, builder, lightTint, packedUV)
                    }
                    if (down != FluidCull.CULLED && ChunkMeshDetails.SIDE_DOWN in builder.details) {
                        renderDown(model, offsetPosition.unsafe, builder, lightTint)
                    }
                    if (sides) {
                        val flowing = model.flowing
                        packedUV[0] = PackedUV(0.0f, 0.5f)
                        packedUV[1] = PackedUV(0.5f, 0.5f)
                        val mesh = builder[flowing.transparency]
                        val overlay = model.overlay

                        if (ChunkMeshDetails.SIDE_NORTH in builder.details) renderSide(offsetPosition.unsafe, 0.0f, 1.0f, 0.0f, 0.0f, corners[0], corners[1], north, flowing, overlay, mesh, lightTint, positions, packedUV)
                        if (ChunkMeshDetails.SIDE_SOUTH in builder.details) renderSide(offsetPosition.unsafe, 1.0f, 0.0f, 1.0f, 1.0f, corners[2], corners[3], south, flowing, overlay, mesh, lightTint, positions, packedUV)
                        if (ChunkMeshDetails.SIDE_WEST in builder.details) renderSide(offsetPosition.unsafe, 0.0f, 0.0f, 1.0f, 0.0f, corners[3], corners[0], west, flowing, overlay, mesh, lightTint, positions, packedUV)
                        if (ChunkMeshDetails.SIDE_EAST in builder.details) renderSide(offsetPosition.unsafe, 1.0f, 1.0f, 0.0f, 1.0f, corners[1], corners[2], east, flowing, overlay, mesh, lightTint, positions, packedUV)
                    }

                    builder.addBlock(x, y, z)

                    if (Thread.interrupted()) throw InterruptedException()
                }
            }
        }
    }


    companion object {
        val POSITIONS_TOP_STILL = floatArrayOf(
            0.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
        )
        val STILL_UV_TOP = UnpackedUVArray(POSITIONS_TOP_STILL).pack()
    }
}
