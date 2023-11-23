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

package de.bixilon.minosoft.gui.rendering.chunk.util

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.update.AbstractWorldUpdate
import de.bixilon.minosoft.data.world.chunk.update.WorldUpdateEvent
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.chunk.update.block.SingleBlockDataUpdate
import de.bixilon.minosoft.data.world.chunk.update.block.SingleBlockUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkCreateUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkLightUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourChangeUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.prototype.PrototypeChangeUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.inChunkSectionPosition
import de.bixilon.minosoft.data.world.positions.ChunkPositionUtil.sectionHeight
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.modding.event.events.DimensionChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

object ChunkRendererChangeListener {

    private fun ChunkRenderer.handle(update: SingleBlockUpdate) {
        val neighbours = update.chunk.neighbours.get() ?: return
        val sectionHeight = update.position.sectionHeight

        master.tryQueue(update.chunk, sectionHeight)
        val inPosition = update.position.inChunkSectionPosition

        if (inPosition.y == 0) {
            master.tryQueue(update.chunk, sectionHeight - 1)
        } else if (inPosition.y == ProtocolDefinition.SECTION_MAX_Y) {
            master.tryQueue(update.chunk, sectionHeight + 1)
        }
        if (inPosition.z == 0) {
            master.tryQueue(chunk = neighbours[3], sectionHeight)
        } else if (inPosition.z == ProtocolDefinition.SECTION_MAX_Z) {
            master.tryQueue(chunk = neighbours[4], sectionHeight)
        }
        if (inPosition.x == 0) {
            master.tryQueue(chunk = neighbours[1], sectionHeight)
        } else if (inPosition.x == ProtocolDefinition.SECTION_MAX_X) {
            master.tryQueue(chunk = neighbours[6], sectionHeight)
        }
    }

    private fun ChunkRenderer.handle(update: SingleBlockDataUpdate) {
        master.tryQueue(update.chunk, update.position.sectionHeight)
    }

    private fun ChunkRenderer.handle(update: ChunkLocalBlockUpdate) {
        val neighbours = update.chunk.neighbours.get() ?: return
        val sectionHeights: Int2ObjectOpenHashMap<BooleanArray> = Int2ObjectOpenHashMap()
        for ((position, state) in update.updates) {
            val neighbours = sectionHeights.getOrPut(position.sectionHeight) { BooleanArray(Directions.SIZE) }
            val inSectionHeight = position.y.inSectionHeight
            if (inSectionHeight == 0) {
                neighbours[0] = true
            } else if (inSectionHeight == ProtocolDefinition.SECTION_MAX_Y) {
                neighbours[1] = true
            }
            if (position.z == 0) {
                neighbours[2] = true
            } else if (position.z == ProtocolDefinition.SECTION_MAX_Z) {
                neighbours[3] = true
            }
            if (position.x == 0) {
                neighbours[4] = true
            } else if (position.x == ProtocolDefinition.SECTION_MAX_X) {
                neighbours[5] = true
            }
        }
        for ((sectionHeight, neighbourUpdates) in sectionHeights) {
            master.tryQueue(update.chunk, sectionHeight)

            if (neighbourUpdates[0]) {
                master.tryQueue(update.chunk, sectionHeight - 1)
            }
            if (neighbourUpdates[1]) {
                master.tryQueue(update.chunk, sectionHeight + 1)
            }
            if (neighbourUpdates[2]) {
                master.tryQueue(neighbours[3], sectionHeight)
            }
            if (neighbourUpdates[3]) {
                master.tryQueue(neighbours[4], sectionHeight)
            }
            if (neighbourUpdates[4]) {
                master.tryQueue(neighbours[1], sectionHeight)
            }
            if (neighbourUpdates[5]) {
                master.tryQueue(neighbours[6], sectionHeight)
            }
        }
    }

    private fun ChunkRenderer.handle(update: ChunkLightUpdate) {
        if (update.blockChange) return // change is already covered
        master.tryQueue(update.chunk, update.sectionHeight)
    }

    private fun ChunkRenderer.handle(update: ChunkCreateUpdate) {
        master.tryQueue(update.chunk)
    }

    private fun ChunkRenderer.handle(update: ChunkUnloadUpdate) {
        unloadChunk(update.chunkPosition)
    }

    private fun ChunkRenderer.handle(update: NeighbourChangeUpdate) {
        master.tryQueue(update.chunk)
    }

    private fun ChunkRenderer.handle(update: PrototypeChangeUpdate) {
        for (height in update.affected.intIterator()) {
            master.tryQueue(update.chunk, height)
        }
    }


    private fun ChunkRenderer.handle(update: AbstractWorldUpdate) {
        if (context.state == RenderingStates.PAUSED) return
        when (update) {
            is SingleBlockUpdate -> handle(update)
            is SingleBlockDataUpdate -> handle(update)
            is ChunkLocalBlockUpdate -> handle(update)
            is ChunkLightUpdate -> handle(update)
            is ChunkCreateUpdate -> handle(update)
            is ChunkUnloadUpdate -> handle(update)
            is NeighbourChangeUpdate -> handle(update)
            is PrototypeChangeUpdate -> handle(update)
            else -> Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Unknown world update happened: $update" }
        }
    }

    fun register(renderer: ChunkRenderer) {
        val events = renderer.connection.events

        events.listen<DimensionChangeEvent> { renderer.unloadWorld() }
        events.listen<WorldUpdateEvent> { renderer.handle(it.update) }

        renderer.connection::state.observe(this) { if (it == PlayConnectionStates.DISCONNECTED) renderer.unloadWorld() }
    }
}
