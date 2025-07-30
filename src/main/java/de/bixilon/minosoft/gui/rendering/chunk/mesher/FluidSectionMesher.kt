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

import glm_.func.cos
import glm_.func.sin
import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EmptyCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.fluid.Fluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid.Companion.isWaterlogged
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.types.LightLevel
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.FaceCulling
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.invoke
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3fUtil.rotate
import de.bixilon.minosoft.util.KUtil.isTrue
import kotlin.math.atan2

class FluidSectionMesher(
    val context: RenderContext,
) {
    private val water = context.session.registries.fluid[WaterFluid]
    private val tints = context.tints

    private fun BlockState.getFluid(): Fluid? {
        val block = block
        return when {
            block is FluidHolder -> block.fluid
            water != null && isWaterlogged() -> water
            else -> null
        }
    }


    // ToDo: Should this be combined with the solid renderer (but we'd need to render faces twice, because of cullface)
    fun mesh(sectionPosition: SectionPosition, chunk: Chunk, section: ChunkSection, mesh: ChunkMeshes) {
        val blocks = section.blocks

        var position: BlockPosition
        var tint: RGBColor

        val cameraOffset = context.camera.offset.offset

        val offsetX = sectionPosition.x * ChunkSize.SECTION_WIDTH_X
        val offsetY = sectionPosition.y * ChunkSize.SECTION_HEIGHT_Y
        val offsetZ = sectionPosition.z * ChunkSize.SECTION_WIDTH_Z

        for (y in blocks.minPosition.y..blocks.maxPosition.y) {
            for (z in blocks.minPosition.z..blocks.maxPosition.z) {
                for (x in blocks.minPosition.x..blocks.maxPosition.x) {
                    val inSection = InSectionPosition(x, y, z)
                    val state = blocks[inSection] ?: continue
                    val fluid = state.getFluid() ?: continue

                    val model = fluid.model ?: continue


                    val height = fluid.getHeight(state)

                    fun isSideCovered(direction: Directions): Boolean {
                        val neighbour = section.traceBlock(inSection, direction) ?: return false

                        if (fluid.matches(neighbour)) {
                            return true
                        }
                        if (direction == Directions.UP && height < 0.99f) return false

                        return FaceCulling.canCull(state, model.properties, direction, neighbour)
                    }

                    val skip = booleanArrayOf(
                        isSideCovered(Directions.DOWN), /* ToDo */
                        isSideCovered(Directions.UP),
                        isSideCovered(Directions.NORTH),
                        isSideCovered(Directions.SOUTH),
                        isSideCovered(Directions.WEST),
                        isSideCovered(Directions.EAST),
                    )

                    if (skip.isTrue) continue

                    var rendered = false
                    position = BlockPosition(x = offsetX + x, y = offsetY + y, z = offsetZ + z)

                    tint = tints.getFluidTint(chunk, fluid, height, position) ?: Colors.WHITE_RGB
                    val cornerHeights = floatArrayOf(
                        getCornerHeight(chunk, position, fluid),
                        getCornerHeight(chunk, position + Directions.EAST, fluid),
                        getCornerHeight(chunk, position + Directions.EAST + Directions.SOUTH, fluid),
                        getCornerHeight(chunk, position + Directions.SOUTH, fluid),
                    )

                    val offsetPosition = Vec3f(position - cameraOffset)

                    if (cornerHeights[0] <= 1.0f && !skip[Directions.O_UP]) {
                        val velocity = fluid.getVelocity(state, position, chunk)
                        val still = velocity == null || velocity.x == 0.0 && velocity.z == 0.0
                        val texture: Texture
                        val minUV = Vec2f.EMPTY
                        val maxUV = Vec2f(if (still) 1.0f else 0.5f) // Minecraft just uses half of the sprite

                        val texturePositions = arrayOf(
                            minUV,
                            Vec2f(maxUV.x, minUV.y),
                            maxUV,
                            Vec2f(minUV.x, maxUV.y),
                        )


                        if (still) {
                            texture = model.still
                        } else {
                            texture = model.flowing
                            maxUV.x = 0.5f

                            val atan = atan2(velocity!!.x, velocity.z).toFloat()
                            val sin = atan.sin
                            val cos = atan.cos

                            for (i in 0 until 4) {
                                texturePositions[i] = (rotate(texturePositions[i].x - TEXTURE_CENTER, texturePositions[i].y - TEXTURE_CENTER, sin, cos, false) + TEXTURE_CENTER)
                            }
                        }

                        val meshToUse = mesh[texture.transparency]

                        val positions = arrayOf(
                            Vec3f(offsetPosition.x, offsetPosition.y + cornerHeights[0], offsetPosition.z),
                            Vec3f(offsetPosition.x + 1, offsetPosition.y + cornerHeights[1], offsetPosition.z),
                            Vec3f(offsetPosition.x + 1, offsetPosition.y + cornerHeights[2], offsetPosition.z + 1),
                            Vec3f(offsetPosition.x, offsetPosition.y + cornerHeights[3], offsetPosition.z + 1),
                        )


                        val light = chunk.light[InChunkPosition(x, position.y, z)]
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
                            Vec3f(faceX, offsetPosition.y + v1, faceZ),
                            Vec3f(faceX, offsetPosition.y, faceZ),
                            Vec3f(faceXEnd, offsetPosition.y, faceZEnd),
                            Vec3f(faceXEnd, offsetPosition.y + v2, faceZEnd),
                        )
                        val texturePositions = arrayOf(
                            Vec2f(0.0f, (1 - v1) / 2),
                            TEXTURE_1,
                            TEXTURE_2,
                            Vec2f(0.5f, (1 - v2) / 2),
                        )

                        val meshToUse = mesh[model.flowing.transparency]
                        val fluidLight = chunk.light[InChunkPosition(x, offsetY + y, z)]
                        addFluidVertices(meshToUse, positions, texturePositions, model.flowing, tint, fluidLight)
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

    private inline fun addFluidVertices(mesh: ChunkMesh, positions: Array<Vec3f>, texturePositions: Array<Vec2f>, flowingTexture: Texture, fluidTint: RGBColor, fluidLight: LightLevel) {
        val lightIndex = fluidLight.index
        mesh.order.iterate { position, uv -> mesh.addVertex(positions[position].array, texturePositions[uv], flowingTexture, fluidTint, lightIndex) }
        mesh.order.iterateReverse { position, uv -> mesh.addVertex(positions[position].array, texturePositions[uv], flowingTexture, fluidTint, lightIndex) }
    }

    private fun getCornerHeight(providedChunk: Chunk, position: BlockPosition, fluid: Fluid): Float {
        var totalHeight = 0.0f
        var count = 0

        val neighbours = providedChunk.neighbours

        for (side in 0 until 4) {
            val now = BlockPosition(x = position.x - (side and 0x01), y = position.y, z = position.z - (side shr 1 and 0x01))
            val offset = position.chunkPosition - providedChunk.position
            val chunk = neighbours[offset] ?: continue

            val inChunk = now.inChunkPosition

            if (fluid.matches(chunk[inChunk + Directions.UP])) {
                return 1.0f
            }

            val state = chunk[inChunk]
            if (state == null) {
                count++
                continue
            }

            if (!fluid.matches(state)) {
                // TODO: this was !blockState.material.solid
                if (state.block !is CollidableBlock || state.block.getCollisionShape(context.session, EmptyCollisionContext, now, state, null) == null) {
                    count++
                }
                continue
            }

            val height = fluid.getHeight(state)

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

        private val TEXTURE_1 = Vec2f(0.0f, 0.5f)
        private val TEXTURE_2 = Vec2f(0.5f, 0.5f)
    }
}
