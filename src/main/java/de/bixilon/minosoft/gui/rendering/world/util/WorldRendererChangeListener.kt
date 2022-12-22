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

package de.bixilon.minosoft.gui.rendering.world.util

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.chunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import de.bixilon.minosoft.modding.event.events.RespawnEvent
import de.bixilon.minosoft.modding.event.events.blocks.BlockDataChangeEvent
import de.bixilon.minosoft.modding.event.events.blocks.BlockSetEvent
import de.bixilon.minosoft.modding.event.events.blocks.BlocksSetEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkDataChangeEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.ChunkUnloadEvent
import de.bixilon.minosoft.modding.event.events.blocks.chunk.LightChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

object WorldRendererChangeListener {

    private fun listenBlockSet(renderer: WorldRenderer) {
        renderer.connection.events.listen<BlockSetEvent> {
            val chunkPosition = it.blockPosition.chunkPosition
            val sectionHeight = it.blockPosition.sectionHeight
            val chunk = renderer.world[chunkPosition] ?: return@listen
            val neighbours = chunk.neighbours.get() ?: return@listen
            renderer.queueSection(chunkPosition, sectionHeight, chunk, neighbours = neighbours)
            val inChunkSectionPosition = it.blockPosition.inChunkSectionPosition

            if (inChunkSectionPosition.y == 0) {
                renderer.queueSection(chunkPosition, sectionHeight - 1, chunk, neighbours = neighbours)
            } else if (inChunkSectionPosition.y == ProtocolDefinition.SECTION_MAX_Y) {
                renderer.queueSection(chunkPosition, sectionHeight + 1, chunk, neighbours = neighbours)
            }
            if (inChunkSectionPosition.z == 0) {
                renderer.queueSection(Vec2i(chunkPosition.x, chunkPosition.y - 1), sectionHeight, chunk = neighbours[3])
            } else if (inChunkSectionPosition.z == ProtocolDefinition.SECTION_MAX_Z) {
                renderer.queueSection(Vec2i(chunkPosition.x, chunkPosition.y + 1), sectionHeight, chunk = neighbours[4])
            }
            if (inChunkSectionPosition.x == 0) {
                renderer.queueSection(Vec2i(chunkPosition.x - 1, chunkPosition.y), sectionHeight, chunk = neighbours[1])
            } else if (inChunkSectionPosition.x == ProtocolDefinition.SECTION_MAX_X) {
                renderer.queueSection(Vec2i(chunkPosition.x + 1, chunkPosition.y), sectionHeight, chunk = neighbours[6])
            }
        }
    }

    fun listenBlocksSet(renderer: WorldRenderer) {
        renderer.connection.events.listen<BlocksSetEvent> {
            val chunk = renderer.world[it.chunkPosition] ?: return@listen // should not happen
            if (!chunk.isFullyLoaded) {
                return@listen
            }
            val sectionHeights: Int2ObjectOpenHashMap<BooleanArray> = Int2ObjectOpenHashMap()
            for (blockPosition in it.blocks.keys) {
                val neighbours = sectionHeights.getOrPut(blockPosition.sectionHeight) { BooleanArray(Directions.SIZE) }
                val inSectionHeight = blockPosition.y.inSectionHeight
                if (inSectionHeight == 0) {
                    neighbours[0] = true
                } else if (inSectionHeight == ProtocolDefinition.SECTION_MAX_Y) {
                    neighbours[1] = true
                }
                if (blockPosition.z == 0) {
                    neighbours[2] = true
                } else if (blockPosition.z == ProtocolDefinition.SECTION_MAX_Z) {
                    neighbours[3] = true
                }
                if (blockPosition.x == 0) {
                    neighbours[4] = true
                } else if (blockPosition.x == ProtocolDefinition.SECTION_MAX_X) {
                    neighbours[5] = true
                }
            }
            val neighbours = chunk.neighbours.get() ?: return@listen
            for ((sectionHeight, neighbourUpdates) in sectionHeights) {
                renderer.queueSection(it.chunkPosition, sectionHeight, chunk, neighbours = neighbours)

                if (neighbourUpdates[0]) {
                    renderer.queueSection(it.chunkPosition, sectionHeight - 1, chunk, neighbours = neighbours)
                }
                if (neighbourUpdates[1]) {
                    renderer.queueSection(it.chunkPosition, sectionHeight + 1, chunk, neighbours = neighbours)
                }
                if (neighbourUpdates[2]) {
                    renderer.queueSection(Vec2i(it.chunkPosition.x, it.chunkPosition.y - 1), sectionHeight, chunk = neighbours[3])
                }
                if (neighbourUpdates[3]) {
                    renderer.queueSection(Vec2i(it.chunkPosition.x, it.chunkPosition.y + 1), sectionHeight, chunk = neighbours[4])
                }
                if (neighbourUpdates[4]) {
                    renderer.queueSection(Vec2i(it.chunkPosition.x - 1, it.chunkPosition.y), sectionHeight, chunk = neighbours[1])
                }
                if (neighbourUpdates[5]) {
                    renderer.queueSection(Vec2i(it.chunkPosition.x + 1, it.chunkPosition.y), sectionHeight, chunk = neighbours[6])
                }
            }
        }
    }

    fun register(renderer: WorldRenderer) {
        val events = renderer.connection.events

        listenBlockSet(renderer)
        listenBlocksSet(renderer)

        events.listen<RespawnEvent> { if (it.dimensionChange) renderer.unloadWorld() }
        events.listen<ChunkDataChangeEvent> { renderer.queueChunk(it.chunkPosition, it.chunk) }

        events.listen<LightChangeEvent> {
            if (it.blockChange) {
                // change is already covered
                return@listen
            }
            renderer.queueSection(it.chunkPosition, it.sectionHeight, it.chunk)
        }


        events.listen<ChunkUnloadEvent> { renderer.unloadChunk(it.chunkPosition) }
        renderer.connection::state.observe(this) { if (it == PlayConnectionStates.DISCONNECTED) renderer.unloadWorld() }
        events.listen<BlockDataChangeEvent> { renderer.queueSection(it.blockPosition.chunkPosition, it.blockPosition.sectionHeight) }
    }
}
