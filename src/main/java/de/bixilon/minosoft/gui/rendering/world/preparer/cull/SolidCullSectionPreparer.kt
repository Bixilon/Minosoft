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

import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatch
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.direction.Directions.Companion.O_DOWN
import de.bixilon.minosoft.data.direction.Directions.Companion.O_EAST
import de.bixilon.minosoft.data.direction.Directions.Companion.O_NORTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_SOUTH
import de.bixilon.minosoft.data.direction.Directions.Companion.O_UP
import de.bixilon.minosoft.data.direction.Directions.Companion.O_WEST
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.MinecraftBlocks
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.models.baked.block.BakedBlockModel
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.preparer.SolidSectionPreparer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.chunk.ChunkUtil.acquire
import de.bixilon.minosoft.util.chunk.ChunkUtil.release
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import java.util.*

class SolidCullSectionPreparer(
    val renderWindow: RenderWindow,
) : SolidSectionPreparer {
    private val profile = renderWindow.connection.profiles.block.rendering
    private val bedrock = renderWindow.connection.registries.blockRegistry[MinecraftBlocks.BEDROCK]?.defaultState
    private val someFullBlock = renderWindow.connection.registries.blockRegistry[MinecraftBlocks.COMMAND_BLOCK]?.defaultState
    private val tintColorCalculator = renderWindow.tintManager
    private val ambientLight = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)
    private var fastBedrock = false

    init {
        val profile = renderWindow.connection.profiles.rendering
        profile.performance::fastBedrock.profileWatch(this, true, profile) { this.fastBedrock = it }
    }

    override fun prepareSolid(chunkPosition: Vec2i, sectionHeight: Int, chunk: Chunk, section: ChunkSection, neighbours: Array<ChunkSection?>, neighbourChunks: Array<Chunk>, mesh: WorldMesh) {
        val random = Random(0L)

        val randomBlockModels = profile.antiMoirePattern
        val isLowestSection = sectionHeight == chunk.lowestSection
        val isHighestSection = sectionHeight == chunk.highestSection
        val blocks = section.blocks
        val sectionLight = section.light
        val blockEntities: MutableSet<BlockEntityRenderer<*>> = mutableSetOf()
        section.acquire()
        neighbours.acquire()
        var blockEntity: BlockEntity? = null
        var model: BakedBlockModel
        var blockState: BlockState
        var position: Vec3i
        var rendered: Boolean
        var tints: IntArray?
        val neighbourBlocks: Array<BlockState?> = arrayOfNulls(Directions.SIZE)
        val light = ByteArray(Directions.SIZE + 1) // last index (6) for the current block

        val offsetX = chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X
        val offsetY = sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y
        val offsetZ = chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z

        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            val fastBedrock = y == 0 && isLowestSection && fastBedrock
            for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                    blockState = blocks.unsafeGet(x, y, z) ?: continue
                    if (blockState.block is FluidBlock) {
                        continue
                    }
                    position = Vec3i(offsetX + x, offsetY + y, offsetZ + z)
                    blockEntity = section.blockEntities.unsafeGet(x, y, z)
                    val blockEntityModel = blockEntity?.createModel()
                    if (blockEntityModel != null) {
                        blockEntityModel.init(renderWindow, blockState, position)
                        blockEntities += blockEntityModel
                    }
                    model = blockState.blockModel ?: continue

                    light[6] = sectionLight[y shl 8 or (z shl 4) or x]

                    if (y == 0) {
                        if (fastBedrock && blockState === bedrock) {
                            neighbourBlocks[O_DOWN] = someFullBlock
                        } else {
                            neighbourBlocks[O_DOWN] = neighbours[O_DOWN]?.blocks?.unsafeGet(x, ProtocolDefinition.SECTION_MAX_Y, z)
                            light[O_DOWN] = if (isLowestSection) {
                                chunk.bottomLight
                            } else {
                                neighbours[O_DOWN]?.light
                            }?.get(ProtocolDefinition.SECTION_MAX_Y shl 8 or (z shl 4) or x) ?: 0x00
                        }
                    } else {
                        neighbourBlocks[O_DOWN] = blocks.unsafeGet(x, y - 1, z)
                        light[O_DOWN] = sectionLight[(y - 1) shl 8 or (z shl 4) or x]
                    }
                    if (y == ProtocolDefinition.SECTION_MAX_Y) {
                        neighbourBlocks[O_UP] = neighbours[O_UP]?.blocks?.unsafeGet(x, 0, z)
                        light[O_UP] = if (isHighestSection) {
                            chunk.topLight
                        } else {
                            neighbours[O_UP]?.light
                        }?.get((z shl 4) or x) ?: 0x00
                    } else {
                        neighbourBlocks[O_UP] = blocks.unsafeGet(x, y + 1, z)
                        light[O_UP] = sectionLight[(y + 1) shl 8 or (z shl 4) or x]
                    }

                    if (z == 0) {
                        neighbourBlocks[O_NORTH] = neighbours[O_NORTH]?.blocks?.unsafeGet(x, y, ProtocolDefinition.SECTION_MAX_Z)
                        light[O_NORTH] = neighbours[O_NORTH]?.light?.get(y shl 8 or (ProtocolDefinition.SECTION_MAX_Z shl 4) or x) ?: 0x00
                    } else {
                        neighbourBlocks[O_NORTH] = blocks.unsafeGet(x, y, z - 1)
                        light[O_NORTH] = sectionLight[y shl 8 or ((z - 1) shl 4) or x]
                    }
                    if (z == ProtocolDefinition.SECTION_MAX_Z) {
                        neighbourBlocks[O_SOUTH] = neighbours[O_SOUTH]?.blocks?.unsafeGet(x, y, 0)
                        light[O_SOUTH] = neighbours[O_SOUTH]?.light?.get(y shl 8 or x) ?: 0x00
                    } else {
                        neighbourBlocks[O_SOUTH] = blocks.unsafeGet(x, y, z + 1)
                        light[O_SOUTH] = sectionLight[y shl 8 or ((z + 1) shl 4) or x]
                    }

                    if (x == 0) {
                        neighbourBlocks[O_WEST] = neighbours[O_WEST]?.blocks?.unsafeGet(ProtocolDefinition.SECTION_MAX_X, y, z)
                        light[O_WEST] = neighbours[O_WEST]?.light?.get(y shl 8 or (z shl 4) or ProtocolDefinition.SECTION_MAX_X) ?: 0x00
                    } else {
                        neighbourBlocks[O_WEST] = blocks.unsafeGet(x - 1, y, z)
                        light[O_WEST] = sectionLight[y shl 8 or (z shl 4) or (x - 1)]
                    }
                    if (x == ProtocolDefinition.SECTION_MAX_X) {
                        neighbourBlocks[O_EAST] = neighbours[O_EAST]?.blocks?.unsafeGet(0, y, z)
                        light[O_EAST] = neighbours[O_EAST]?.light?.get(y shl 8 or (z shl 4)) ?: 0x00
                    } else {
                        neighbourBlocks[O_EAST] = blocks.unsafeGet(x + 1, y, z)
                        light[O_EAST] = sectionLight[y shl 8 or (z shl 4) or (x + 1)]
                    }

                    if (randomBlockModels) {
                        random.setSeed(VecUtil.generatePositionHash(position.x, position.y, position.z))
                    } else {
                        random.setSeed(0L)
                    }
                    tints = tintColorCalculator.getAverageTint(chunk, neighbourChunks, blockState, x, y, z)
                    rendered = model.singleRender(position, mesh, random, blockState, neighbourBlocks, light, ambientLight, tints)

                    if (rendered) {
                        mesh.addBlock(x, y, z)
                    }
                }
            }
        }
        section.release()
        neighbours.release()
        mesh.blockEntities = blockEntities
    }
}
