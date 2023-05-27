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

package de.bixilon.minosoft.gui.rendering.world.preparer.cull

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.direction.Directions.Companion.O_DOWN
import de.bixilon.minosoft.data.direction.Directions.Companion.O_EAST
import de.bixilon.minosoft.data.direction.Directions.Companion.O_NORTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_SOUTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_UP
import de.bixilon.minosoft.data.direction.Directions.Companion.O_WEST
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidBlock
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.light.SectionLight
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.state.render.BlockRender
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.SolidSectionPreparer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.*

class SolidCullSectionPreparer(
    val context: RenderContext,
) : SolidSectionPreparer {
    private val profile = context.connection.profiles.block.rendering
    private val bedrock = context.connection.registries.block[MinecraftBlocks.BEDROCK]?.defaultState
    private val someFullBlock = context.connection.registries.block[MinecraftBlocks.COMMAND_BLOCK]?.defaultState
    private val tintColorCalculator = context.tintManager
    private var fastBedrock = false

    init {
        val profile = context.connection.profiles.rendering
        profile.performance::fastBedrock.observe(this, true) { this.fastBedrock = it }
    }

    override fun prepareSolid(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, neighbourChunks: Array<Chunk>, neighbours: Array<ChunkSection?>, mesh: WorldMesh) {
        val random = if (profile.antiMoirePattern) Random(0L) else null


        val isLowestSection = sectionHeight == chunk.minSection
        val isHighestSection = sectionHeight == chunk.maxSection
        val blocks = section.blocks
        val sectionLight = section.light
        val entities: MutableList<BlockEntityRenderer<*>> = ArrayList()

        val position = BlockPosition()
        val neighbourBlocks: Array<BlockState?> = arrayOfNulls(Directions.SIZE)
        val light = ByteArray(Directions.SIZE + 1) // last index (6) for the current block

        val cameraOffset = context.camera.offset.offset

        val offsetX = chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X
        val offsetY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
        val offsetZ = chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z

        val floatOffset = FloatArray(3)

        for (y in blocks.minPosition.y..blocks.maxPosition.y) {
            position.y = offsetY + y
            floatOffset[1] = (position.y - cameraOffset.y).toFloat()
            val fastBedrock = y == 0 && isLowestSection && fastBedrock
            for (x in blocks.minPosition.x..blocks.maxPosition.x) {
                position.x = offsetX + x
                floatOffset[0] = (position.x - cameraOffset.x).toFloat()
                for (z in blocks.minPosition.z..blocks.maxPosition.z) {
                    val baseIndex = (z shl 4) or x
                    val index = (y shl 8) or baseIndex
                    val state = blocks[index] ?: continue
                    if (state.block is FluidBlock) continue // fluids are rendered in a different renderer

                    light[SELF_LIGHT_INDEX] = sectionLight[index]
                    position.z = offsetZ + z
                    floatOffset[2] = (position.z - cameraOffset.z).toFloat()

                    val maxHeight = chunk.light.heightmap[baseIndex]
                    if (position.y >= maxHeight) {
                        light[SELF_LIGHT_INDEX] = (light[SELF_LIGHT_INDEX].toInt() or 0xF0).toByte()
                    }

                    val entity = section.blockEntities[index]?.getRenderer(context, state, position, light[SELF_LIGHT_INDEX].toInt())
                    if (entity != null && entity.enabled) {
                        entities += entity
                        mesh.addBlock(x, y, z)
                    }
                    val blockModel = state.block.model ?: state.model
                    val model = blockModel ?: entity.nullCast<BlockRender>() ?: continue

                    if (y == 0) {
                        if (fastBedrock && state === bedrock) {
                            neighbourBlocks[O_DOWN] = someFullBlock
                        } else {
                            neighbourBlocks[O_DOWN] = neighbours[O_DOWN]?.blocks?.let { it[x, ProtocolDefinition.SECTION_MAX_Y, z] }
                            light[O_DOWN] = (if (isLowestSection) chunk.light.bottom else neighbours[O_DOWN]?.light)?.get(ProtocolDefinition.SECTION_MAX_Y shl 8 or baseIndex) ?: 0x00
                        }
                    } else {
                        neighbourBlocks[O_DOWN] = blocks[(y - 1) shl 8 or baseIndex]
                        light[O_DOWN] = sectionLight[(y - 1) shl 8 or baseIndex]
                    }
                    if (y == ProtocolDefinition.SECTION_MAX_Y) {
                        neighbourBlocks[O_UP] = neighbours[O_UP]?.blocks?.let { it[x, 0, z] }
                        light[O_UP] = (if (isHighestSection) chunk.light.top else neighbours[O_UP]?.light)?.get((z shl 4) or x) ?: 0x00
                    } else {
                        neighbourBlocks[O_UP] = blocks[(y + 1) shl 8 or baseIndex]
                        light[O_UP] = sectionLight[(y + 1) shl 8 or baseIndex]
                    }

                    checkNorth(neighbourBlocks, neighbours, x, y, z, light, position, neighbourChunks, section, chunk)
                    checkSouth(neighbourBlocks, neighbours, x, y, z, light, position, neighbourChunks, section, chunk)
                    checkWest(neighbourBlocks, neighbours, x, y, z, light, position, neighbourChunks, section, chunk)
                    checkEast(neighbourBlocks, neighbours, x, y, z, light, position, neighbourChunks, section, chunk)

                    if (position.y - 1 >= maxHeight) {
                        light[O_UP] = (light[O_UP].toInt() or 0xF0).toByte()
                        light[O_DOWN] = (light[O_DOWN].toInt() or 0xF0).toByte()
                    } else if (position.y + 1 >= maxHeight) {
                        light[O_UP] = (light[O_UP].toInt() or 0xF0).toByte()
                    }

                    val tints = tintColorCalculator.getAverageBlockTint(chunk, neighbourChunks, state, x, y, z)
                    var rendered = model.render(position, floatOffset, mesh, random, state, neighbourBlocks, light, tints)

                    if (blockModel == null && entity is BlockRender) {
                        rendered = entity.render(position, floatOffset, mesh, random, state, neighbourBlocks, light, tints) || rendered
                    }


                    if (rendered) {
                        mesh.addBlock(x, y, z)
                    }
                    if (Thread.interrupted()) throw InterruptedException()
                }
            }
        }
        mesh.blockEntities = entities
    }

    private inline fun checkNorth(neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, neighbourChunks: Array<Chunk>, section: ChunkSection, chunk: Chunk) {
        if (z == 0) {
            setNeighbour(neighbourBlocks, x, y, ProtocolDefinition.SECTION_MAX_Z, light, position, neighbours[O_NORTH], neighbourChunks[ChunkNeighbours.NORTH], O_NORTH)
        } else {
            setNeighbour(neighbourBlocks, x, y, z - 1, light, position, section, chunk, O_NORTH)
        }
    }

    private inline fun checkSouth(neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, neighbourChunks: Array<Chunk>, section: ChunkSection, chunk: Chunk) {
        if (z == ProtocolDefinition.SECTION_MAX_Z) {
            setNeighbour(neighbourBlocks, x, y, 0, light, position, neighbours[O_SOUTH], neighbourChunks[ChunkNeighbours.SOUTH], O_SOUTH)
        } else {
            setNeighbour(neighbourBlocks, x, y, z + 1, light, position, section, chunk, O_SOUTH)
        }
    }

    private inline fun checkWest(neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, neighbourChunks: Array<Chunk>, section: ChunkSection, chunk: Chunk) {
        if (x == 0) {
            setNeighbour(neighbourBlocks, ProtocolDefinition.SECTION_MAX_X, y, z, light, position, neighbours[O_WEST], neighbourChunks[ChunkNeighbours.WEST], O_WEST)
        } else {
            setNeighbour(neighbourBlocks, x - 1, y, z, light, position, section, chunk, O_WEST)
        }
    }

    private inline fun checkEast(neighbourBlocks: Array<BlockState?>, neighbours: Array<ChunkSection?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, neighbourChunks: Array<Chunk>, section: ChunkSection, chunk: Chunk) {
        if (x == ProtocolDefinition.SECTION_MAX_X) {
            setNeighbour(neighbourBlocks, 0, y, z, light, position, neighbours[O_EAST], neighbourChunks[ChunkNeighbours.EAST], O_EAST)
        } else {
            setNeighbour(neighbourBlocks, x + 1, y, z, light, position, section, chunk, O_EAST)
        }
    }

    private inline fun setNeighbour(neighbourBlocks: Array<BlockState?>, x: Int, y: Int, z: Int, light: ByteArray, position: Vec3i, section: ChunkSection?, chunk: Chunk, ordinal: Int) {
        val heightmapIndex = (z shl 4) or x
        val neighbourIndex = y shl 8 or heightmapIndex
        neighbourBlocks[ordinal] = section?.blocks?.let { it[neighbourIndex] }
        light[ordinal] = section?.light?.get(neighbourIndex) ?: 0x00
        if (position.y >= chunk.light.heightmap[heightmapIndex]) {
            light[ordinal] = (light[ordinal].toInt() or SectionLight.SKY_LIGHT_MASK).toByte() // set sky light to 0x0F
        }
    }

    companion object {
        const val SELF_LIGHT_INDEX = 6 // after all directions
    }
}
