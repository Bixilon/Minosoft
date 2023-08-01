/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.func.cos
import de.bixilon.kotlinglm.func.sin
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EmptyCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid.Companion.isWaterlogged
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.SingleChunkMesh
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.FaceCulling
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.getMesh
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotate
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.isTrue
import java.util.*
import kotlin.math.atan2

class FluidSectionMesher(
    val context: RenderContext,
) {
    private val water = context.connection.registries.fluid[WaterFluid]
    private val tints = context.tints


    // ToDo: Should this be combined with the solid renderer (but we'd need to render faces twice, because of cullface)
    fun mesh(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, neighbourChunks: Array<Chunk>, neighbours: Array<ChunkSection?>, mesh: ChunkMesh) {
        val blocks = section.blocks

        val random = Random(0L)
        var blockState: BlockState
        var position: Vec3i
        var rendered = false
        var tint: Int

        val cameraOffset = context.camera.offset.offset

        val offsetX = chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X
        val offsetY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
        val offsetZ = chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z

        for (y in blocks.minPosition.y..blocks.maxPosition.y) {
            for (z in blocks.minPosition.z..blocks.maxPosition.z) {
                for (x in blocks.minPosition.x..blocks.maxPosition.x) {
                    blockState = blocks[x, y, z] ?: continue
                    val block = blockState.block
                    val fluid = when {
                        block is FluidHolder -> block.fluid
                        water != null && blockState.isWaterlogged() -> water
                        else -> continue
                    }
                    val model = fluid.model ?: continue
                    val stillTexture = model.still ?: continue
                    val flowingTexture = model.flowing ?: continue
                    val height = fluid.getHeight(blockState)

                    position = Vec3i(offsetX + x, offsetY + y, offsetZ + z)
                    tint = tints.getFluidTint(chunk, fluid, height, position.x, position.y, position.z) ?: Colors.WHITE


                    fun isSideCovered(direction: Directions): Boolean {
                        val neighbourPosition = position + direction
                        val neighbour = direction.getBlock(x, y, z, section, neighbours) ?: return false

                        if (fluid.matches(neighbour)) {
                            return true
                        }

                        return FaceCulling.canCull(blockState, model.properties, direction, neighbour)
                    }

                    val topBlock = if (y == ProtocolDefinition.SECTION_MAX_Y) {
                        neighbours[Directions.O_UP]?.blocks?.let { it[x, 0, z] }
                    } else {
                        section.blocks[x, y + 1, z]
                    }

                    val skip = booleanArrayOf(
                        isSideCovered(Directions.DOWN), /* ToDo */
                        fluid.matches(topBlock),
                        isSideCovered(Directions.NORTH),
                        isSideCovered(Directions.SOUTH),
                        isSideCovered(Directions.WEST),
                        isSideCovered(Directions.EAST),
                    )

                    if (skip.isTrue) {
                        continue
                    }

                    val cornerHeights = floatArrayOf(
                        getCornerHeight(chunk, chunkPosition, position, fluid),
                        getCornerHeight(chunk, chunkPosition, position + Directions.EAST, fluid),
                        getCornerHeight(chunk, chunkPosition, position + Directions.EAST + Directions.SOUTH, fluid),
                        getCornerHeight(chunk, chunkPosition, position + Directions.SOUTH, fluid),
                    )

                    val offsetPosition = Vec3(position - cameraOffset)

                    if (!skip[Directions.O_UP]) {
                        val velocity = fluid.getVelocity(blockState, position, chunk)
                        val still = velocity.x == 0.0 && velocity.z == 0.0
                        val texture: Texture
                        val minUV = Vec2.EMPTY
                        val maxUV = Vec2(if (still) 1.0f else 0.5f) // Minecraft just uses half of the sprite


                        val texturePositions = arrayOf(
                            Vec2(maxUV.x, minUV.y),
                            minUV,
                            Vec2(minUV.x, maxUV.y),
                            maxUV,
                        )


                        if (still) {
                            texture = stillTexture
                        } else {
                            texture = flowingTexture
                            maxUV.x = 0.5f

                            val atan = atan2(velocity.x, velocity.z).toFloat()
                            val sin = atan.sin
                            val cos = atan.cos

                            for (i in 0 until 4) {
                                texturePositions[i] = (rotate(texturePositions[i].x - TEXTURE_CENTER, texturePositions[i].y - TEXTURE_CENTER, sin, cos, false) + TEXTURE_CENTER)
                            }
                        }

                        val meshToUse = texture.transparency.getMesh(mesh)

                        val positions = arrayOf(
                            Vec3(offsetPosition.x, offsetPosition.y + cornerHeights[0], offsetPosition.z),
                            Vec3(offsetPosition.x + 1, offsetPosition.y + cornerHeights[1], offsetPosition.z),
                            Vec3(offsetPosition.x + 1, offsetPosition.y + cornerHeights[2], offsetPosition.z + 1),
                            Vec3(offsetPosition.x, offsetPosition.y + cornerHeights[3], offsetPosition.z + 1),
                        )


                        val light = chunk.light[x, position.y, z]
                        addFluidVertices(meshToUse, positions, texturePositions, texture, tint, light)
                        rendered = true
                    }
                    // ToDo: Sides: Minecraft uses (for water) an overlay texture (with cullface) that is used, when the face fits to a non opaque block


                    for (direction in 0 until Directions.SIZE_SIDES) {
                        if (skip[Directions.SIDE_OFFSET + direction]) {
                            continue
                        }
                        var faceX = offsetPosition.x
                        var faceXEnd = faceX
                        var faceZ = offsetPosition.z
                        var faceZEnd = faceZ
                        var v1 = 0.0f
                        var v2 = 0.0f

                        when (direction) {
                            0 -> {
                                faceXEnd += 1.0f
                                v1 = cornerHeights[0]
                                v2 = cornerHeights[1]
                            }

                            1 -> {
                                faceX += 1.0f
                                faceZ += +1.0f
                                faceZEnd += 1.0f
                                v1 = cornerHeights[2]
                                v2 = cornerHeights[3]
                            }

                            2 -> {
                                faceZ += 1.0f
                                v1 = cornerHeights[3]
                                v2 = cornerHeights[0]
                            }

                            3 -> {
                                faceX += 1.0f
                                faceXEnd += 1.0f
                                faceZEnd += 1.0f
                                v1 = cornerHeights[1]
                                v2 = cornerHeights[2]
                            }
                        }
                        // ToDo: Prevent face fighting with transparent neighbours


                        val positions = arrayOf(
                            Vec3(faceX, offsetPosition.y + v1, faceZ),
                            Vec3(faceX, offsetPosition.y, faceZ),
                            Vec3(faceXEnd, offsetPosition.y, faceZEnd),
                            Vec3(faceXEnd, offsetPosition.y + v2, faceZEnd),
                        )
                        val texturePositions = arrayOf(
                            TEXTURE_1,
                            Vec2(0.0f, (1 - v1) / 2),
                            Vec2(0.5f, (1 - v2) / 2),
                            TEXTURE_2,
                        )

                        val meshToUse = flowingTexture.transparency.getMesh(mesh)
                        val fluidLight = chunk.light[x, offsetY + y, z]
                        addFluidVertices(meshToUse, positions, texturePositions, flowingTexture, tint, fluidLight)
                        rendered = true
                    }


                    if (rendered) {
                        mesh.addBlock(x, y, z)
                    }
                    if (Thread.interrupted()) throw InterruptedException()
                }
            }
        }
    }

    private inline fun addFluidVertices(meshToUse: SingleChunkMesh, positions: Array<Vec3>, texturePositions: Array<Vec2>, flowingTexture: Texture, fluidTint: Int, fluidLight: Int) {
        for (index in 0 until meshToUse.order.size step 2) {
            meshToUse.addVertex(positions[meshToUse.order[index]].array, texturePositions[meshToUse.order[index + 1]], flowingTexture, fluidTint, fluidLight)
        }
        for (index in (meshToUse.order.size - 2) downTo 0 step 2) {
            meshToUse.addVertex(positions[meshToUse.order[index]].array, texturePositions[meshToUse.order[index + 1]], flowingTexture, fluidTint, fluidLight)
        }
    }

    private fun getCornerHeight(providedChunk: Chunk, providedChunkPosition: Vec2i, position: Vec3i, fluid: Fluid): Float {
        var totalHeight = 0.0f
        var count = 0

        val neighbours = providedChunk.neighbours

        for (side in 0 until 4) {
            val blockPosition = position + Vec3i(-(side and 0x01), 0, -(side shr 1 and 0x01))
            val offset = blockPosition.chunkPosition - providedChunkPosition
            val chunk = neighbours[offset] ?: continue

            val inChunkPosition = blockPosition.inChunkPosition
            if (fluid.matches(chunk[inChunkPosition + Directions.UP])) {
                return 1.0f
            }

            val blockState = chunk[inChunkPosition]
            if (blockState == null) {
                count++
                continue
            }

            if (!fluid.matches(blockState)) {
                // TODO: this was !blockState.material.solid
                if (blockState.block !is CollidableBlock || blockState.block.getCollisionShape(EmptyCollisionContext, blockPosition, blockState, null) == AbstractVoxelShape.EMPTY) {
                    count++
                }
                continue
            }

            val height = fluid.getHeight(blockState)

            if (height >= 0.8f) {
                totalHeight += height * 10.0f
                count += 10
            } else {
                totalHeight += height
                count++
            }
        }


        return totalHeight / count
    }

    private companion object {
        private const val TEXTURE_CENTER = 1.0f / 2.0f

        private val TEXTURE_1 = Vec2(0.0f, 0.5f)
        private val TEXTURE_2 = Vec2(0.5f, 0.5f)

        /*
        private val FLUID_FACE_PROPERTY = FaceProperties(
            Vec2.EMPTY,
            Vec2(1.0f, 1.0f),
            TextureTransparencies.OPAQUE,
        )
         */
    }
}
