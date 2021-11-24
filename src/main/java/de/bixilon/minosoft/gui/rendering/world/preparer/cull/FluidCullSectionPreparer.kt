package de.bixilon.minosoft.gui.rendering.world.preparer.cull

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
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.getMesh
import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotate
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.FluidSectionPreparer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.chunk.ChunkUtil.acquire
import de.bixilon.minosoft.util.chunk.ChunkUtil.release
import glm_.func.cos
import glm_.func.sin
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i
import kotlin.math.atan2

class FluidCullSectionPreparer(
    val renderWindow: RenderWindow,
) : FluidSectionPreparer {
    private val world: World = renderWindow.connection.world
    private val water = renderWindow.connection.registries.fluidRegistry[DefaultFluids.WATER]
    private val tintManager = renderWindow.tintManager


    // ToDo: Should this be combined with the solid renderer (but we'd need to render faces twice, because of cullface)
    override fun prepareFluid(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, neighbours: Array<ChunkSection?>, neighbourChunks: Array<Chunk>): WorldMesh? {
        val mesh = WorldMesh(renderWindow, chunkPosition, sectionHeight, smallMesh = true)

        val isLowestSection = sectionHeight == chunk.lowestSection
        val isHighestSection = sectionHeight == chunk.highestSection
        val blocks = section.blocks
        val sectionLight = section.light
        section.acquire()
        neighbours.acquire()

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
                    position = Vec3i(offsetX + x, offsetY + y, offsetZ + z)
                    tints = tintManager.getAverageTint(chunk, neighbourChunks, blockState, fluid, position.x, position.y, position.z)

                    val skipTop = fluid.matches(chunk.get(x, offsetY + y + 1, z))
                    val skipBottom = !shouldRenderSide(position, Directions.DOWN)
                    val skipNorth = !shouldRenderSide(position, Directions.NORTH)
                    val skipSouth = !shouldRenderSide(position, Directions.SOUTH)
                    val skipWest = !shouldRenderSide(position, Directions.WEST)
                    val skipEast = !shouldRenderSide(position, Directions.EAST)

                    if (skipTop && skipBottom && skipNorth && skipSouth && skipWest && skipEast) {
                        continue
                    }
                    val cornerHeights = floatArrayOf(
                        getCornerHeight(position, fluid),
                        getCornerHeight(position + Directions.EAST, fluid),
                        getCornerHeight(position + Directions.EAST + Directions.SOUTH, fluid),
                        getCornerHeight(position + Directions.SOUTH, fluid),
                    )
                    val floatPosition = position.toVec3()
                    floatPosition.y += cornerHeights[0]
                    if (!skipTop) {
                        val velocity = if (fluid is FlowableFluid) fluid.getVelocity(renderWindow.connection, blockState, position) else null
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
                            texture = fluid.stillTexture!!
                        } else {
                            texture = (fluid as FlowableFluid).flowingTexture!!
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
                        rendered = true
                    }
                    // ToDo: Sides: Minecraft uses (for water) a overlay texture (with cullface) that is used, when the face fits to a non opaque block
                    // ToDo: Sides that are connecting with non full cubes (e.g. air) also have cullface disabled


                    for (direction in 0 until 4) {
                        var faceX = 0.0f
                        var faceXEnd = 0.0f
                        var faceZ = 0.0f
                        var faceZEnd = 0.0f
                        var v1 = 0.0f
                        var v2 = 0.0f

                        when (direction) {
                            0 -> {
                                if (skipNorth) {
                                    continue
                                }

                                faceXEnd = 1.0f
                                v1 = cornerHeights[0]
                                v2 = cornerHeights[1]
                            }
                            1 -> {
                                if (skipSouth) {
                                    continue
                                }
                                faceX = 1.0f
                                faceZ = 1.0f
                                faceZEnd = 1.0f
                                v1 = cornerHeights[2]
                                v2 = cornerHeights[3]
                            }
                            2 -> {
                                if (skipWest) {
                                    continue
                                }
                                faceZ = 1.0f
                                v1 = cornerHeights[3]
                                v2 = cornerHeights[0]
                            }
                            3 -> {
                                if (skipEast) {
                                    continue
                                }

                                faceX = 1.0f
                                faceXEnd = 1.0f
                                faceZEnd = 1.0f
                                v1 = cornerHeights[1]
                                v2 = cornerHeights[2]
                            }
                        }

                        val positions = arrayOf(
                            Vec3(position.x + faceX, position.y + v1, position.z + faceZ),
                            Vec3(position.x + faceX, position.y, position.z + faceZ),
                            Vec3(position.x + faceXEnd, position.y, position.z + faceZEnd),
                            Vec3(position.x + faceXEnd, position.y + v2, position.z + faceZEnd),
                        )
                        val texturePositions = arrayOf(
                            Vec2(0.5f, v2 / 2),
                            Vec2(0.5f, 0.0f),
                            Vec2(0.0f, 0.0f),
                            Vec2(0.0f, v1 / 2),
                        )

                        val texture = (fluid as FlowableFluid).flowingTexture!!

                        val meshToUse = texture.transparency.getMesh(mesh)
                        for ((positionIndex, textureIndex) in meshToUse.order) {
                            meshToUse.addVertex(positions[positionIndex].array, texturePositions[textureIndex], texture, tints?.get(0) ?: 0xFFFFFF, 0xFF)
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

        if (mesh.clearEmpty() == 0) {
            return null
        }

        return mesh
    }

    private fun isSideCovered(position: Vec3i, direction: Directions, height: Float): Boolean {
        return world[position + direction] != null
    }

    private fun shouldRenderSide(position: Vec3i, direction: Directions, height: Float = 1.0f): Boolean {
        return !isSideCovered(position, direction, height) /* && fluid.matches(other) */
    }

    private fun getCornerHeight(position: Vec3i, fluid: Fluid): Float {
        // ToDo: Optimize
        var totalHeight = 0.0f
        var count = 0

        for (side in 0 until 4) {
            val blockPosition = position + Vec3i(-(side and 0x01), 0, -(side shr 1 and 0x01))
            if (fluid.matches(world[blockPosition + Directions.UP])) {
                return 1.0f
            }

            val blockState = world[blockPosition]

            if (blockState == null || !fluid.matches(blockState)) {
                if (blockState?.material?.solid != true) {
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
    }

}
