/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.world.preparer.cull

import de.bixilon.kotlinglm.func.cos
import de.bixilon.kotlinglm.func.sin
import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.blocks.types.FluidFillable
import de.bixilon.minosoft.data.registries.fluid.DefaultFluids
import de.bixilon.minosoft.data.registries.fluid.FlowableFluid
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.CullUtil.canCull
import de.bixilon.minosoft.gui.rendering.models.properties.FaceProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.getMesh
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotate
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkPosition
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.FluidSectionPreparer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.isTrue
import de.bixilon.minosoft.util.chunk.ChunkUtil.acquire
import de.bixilon.minosoft.util.chunk.ChunkUtil.release
import java.util.*
import kotlin.math.atan2

class FluidCullSectionPreparer(
    val renderWindow: RenderWindow,
) : FluidSectionPreparer {
    private val world: World = renderWindow.connection.world
    private val water = renderWindow.connection.registries.fluidRegistry[DefaultFluids.WATER]
    private val tintManager = renderWindow.tintManager


    // ToDo: Should this be combined with the solid renderer (but we'd need to render faces twice, because of cullface)
    override fun prepareFluid(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, neighbours: Array<ChunkSection?>, neighbourChunks: Array<Chunk>, mesh: WorldMesh) {
        val blocks = section.blocks
        section.acquire()
        neighbours.acquire()

        val random = Random(0L)
        var blockState: BlockState
        var position: Vec3i
        var rendered = false
        var tints: IntArray?

        val offsetX = chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X
        val offsetY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
        val offsetZ = chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z

        for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
            for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
                for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                    blockState = blocks.unsafeGet(x, y, z) ?: continue
                    val block = blockState.block
                    val fluid = when {
                        block is FluidBlock -> (blockState.block as FluidBlock).fluid
                        blockState.properties[BlockProperties.WATERLOGGED] == true && water != null -> water
                        block is FluidFillable -> block.fluid
                        else -> continue
                    }
                    val stillTexture = fluid.stillTexture ?: continue
                    val flowingTexture = fluid.flowingTexture ?: continue

                    position = Vec3i(offsetX + x, offsetY + y, offsetZ + z)
                    tints = tintManager.getAverageTint(chunk, neighbourChunks, blockState, fluid, position.x, position.y, position.z)


                    fun isSideCovered(direction: Directions): Boolean {
                        val neighbourPosition = position + direction
                        val neighbour = direction.getBlock(x, y, z, section, neighbours) ?: return false

                        if (fluid.matches(neighbour)) {
                            return true
                        }
                        val model = neighbour.blockModel ?: return false
                        random.setSeed(VecUtil.generatePositionHash(neighbourPosition.x, neighbourPosition.y, neighbourPosition.z))
                        val size = model.getTouchingFaceProperties(random, direction.inverted)
                        return size?.canCull(FLUID_FACE_PROPERTY, false) ?: false
                    }

                    val topBlock = if (y == ProtocolDefinition.SECTION_MAX_Y) {
                        neighbours[Directions.O_UP]?.blocks?.unsafeGet(x, 0, z)
                    } else {
                        section.blocks.unsafeGet(x, y + 1, z)
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

                    if (!skip[Directions.O_UP]) {
                        val velocity = if (fluid is FlowableFluid) fluid.getVelocity(renderWindow.connection, blockState, position, section, neighbours) else null
                        val still = velocity == null || velocity.x == 0.0 && velocity.z == 0.0
                        val texture: AbstractTexture
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

                            val atan = atan2(velocity!!.x, velocity.z).toFloat()
                            val sin = atan.sin
                            val cos = atan.cos

                            for (i in 0 until 4) {
                                texturePositions[i] = (rotate(texturePositions[i].x - TEXTURE_CENTER, texturePositions[i].y - TEXTURE_CENTER, sin, cos, false) + TEXTURE_CENTER)
                            }
                        }

                        val meshToUse = texture.transparency.getMesh(mesh)

                        val positions = arrayOf(
                            Vec3(position.x, position.y + cornerHeights[0], position.z),
                            Vec3(position.x + 1, position.y + cornerHeights[1], position.z),
                            Vec3(position.x + 1, position.y + cornerHeights[2], position.z + 1),
                            Vec3(position.x, position.y + cornerHeights[3], position.z + 1),
                        )


                        for ((positionIndex, textureIndex) in meshToUse.order) {
                            meshToUse.addVertex(positions[positionIndex].array, texturePositions[textureIndex], texture, tints?.get(0) ?: 0xFFFFFF, chunk.getLight(position))
                        }
                        // ToDo: Sides that are connecting with non full cubes (e.g. air) also have cullface disabled
                        for ((positionIndex, textureIndex) in meshToUse.reversedOrder) {
                            meshToUse.addVertex(positions[positionIndex].array, texturePositions[textureIndex], texture, tints?.get(0) ?: 0xFFFFFF, chunk.getLight(position))
                        }
                        rendered = true
                    }
                    // ToDo: Sides: Minecraft uses (for water) an overlay texture (with cullface) that is used, when the face fits to a non opaque block


                    for (direction in 0 until Directions.SIZE_SIDES) {
                        if (skip[Directions.SIDE_OFFSET + direction]) {
                            continue
                        }
                        var faceX = 0.0f
                        var faceXEnd = 0.0f
                        var faceZ = 0.0f
                        var faceZEnd = 0.0f
                        var v1 = 0.0f
                        var v2 = 0.0f

                        when (direction) {
                            0 -> {
                                faceXEnd = 1.0f
                                v1 = cornerHeights[0]
                                v2 = cornerHeights[1]
                            }
                            1 -> {
                                faceX = 1.0f
                                faceZ = 1.0f
                                faceZEnd = 1.0f
                                v1 = cornerHeights[2]
                                v2 = cornerHeights[3]
                            }
                            2 -> {
                                faceZ = 1.0f
                                v1 = cornerHeights[3]
                                v2 = cornerHeights[0]
                            }
                            3 -> {
                                faceX = 1.0f
                                faceXEnd = 1.0f
                                faceZEnd = 1.0f
                                v1 = cornerHeights[1]
                                v2 = cornerHeights[2]
                            }
                        }
                        // ToDo: Prevent face fighting with transparent neighbours

                        val positions = arrayOf(
                            Vec3(position.x + faceX, position.y + v1, position.z + faceZ),
                            Vec3(position.x + faceX, position.y, position.z + faceZ),
                            Vec3(position.x + faceXEnd, position.y, position.z + faceZEnd),
                            Vec3(position.x + faceXEnd, position.y + v2, position.z + faceZEnd),
                        )
                        val texturePositions = arrayOf(
                            Vec2(0.0f, 0.5f),
                            Vec2(0.0f, (1 - v1) / 2),
                            Vec2(0.5f, (1 - v2) / 2),
                            Vec2(0.5f, 0.5f),
                        )

                        val meshToUse = flowingTexture.transparency.getMesh(mesh)
                        val fluidTint = tints?.get(0) ?: 0xFFFFFF
                        val fluidLight = chunk.getLight(x, offsetY + y, z)
                        for ((positionIndex, textureIndex) in meshToUse.order) {
                            meshToUse.addVertex(positions[positionIndex].array, texturePositions[textureIndex], flowingTexture, fluidTint, fluidLight)
                        }
                        for ((positionIndex, textureIndex) in meshToUse.reversedOrder) {
                            meshToUse.addVertex(positions[positionIndex].array, texturePositions[textureIndex], flowingTexture, fluidTint, fluidLight)
                        }
                        rendered = true
                    }


                    if (rendered) {
                        mesh.addBlock(x, y, z)
                    }
                }
            }
        }
        section.release()
        neighbours.release()
    }

    private fun getCornerHeight(providedChunk: Chunk, providedChunkPosition: Vec2i, position: Vec3i, fluid: Fluid): Float {
        // ToDo: Optimize more
        var totalHeight = 0.0f
        var count = 0

        var lastChunkPosition = providedChunkPosition
        var lastChunk: Chunk? = providedChunk

        for (side in 0 until 4) {
            val blockPosition = position + Vec3i(-(side and 0x01), 0, -(side shr 1 and 0x01))
            val chunkPosition = blockPosition.chunkPosition
            if (chunkPosition != lastChunkPosition) {
                lastChunkPosition = chunkPosition
                lastChunk = world[chunkPosition]
            }
            if (lastChunk == null) {
                count++
                continue
            }
            val inChunkPosition = blockPosition.inChunkPosition
            if (fluid.matches(lastChunk.unsafeGet(inChunkPosition + Directions.UP))) {
                return 1.0f
            }

            val blockState = lastChunk.unsafeGet(inChunkPosition)
            if (blockState == null) {
                count++
                continue
            }

            if (!fluid.matches(blockState)) {
                if (!blockState.material.solid) {
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
        private val FLUID_FACE_PROPERTY = FaceProperties(
            Vec2.EMPTY,
            Vec2(1.0f, 1.0f),
            TextureTransparencies.OPAQUE,
        )
    }
}
