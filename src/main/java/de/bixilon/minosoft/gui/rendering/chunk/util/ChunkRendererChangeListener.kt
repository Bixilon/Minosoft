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

package de.bixilon.minosoft.gui.rendering.chunk.util

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.ChunkSize
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
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.modding.event.events.DimensionChangeEvent
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

object ChunkRendererChangeListener {

    private fun ChunkRenderer.handle(update: SingleBlockUpdate) {
        if (!update.chunk.neighbours.complete) return
        val neighbours = update.chunk.neighbours.neighbours
        val sectionHeight = update.position.sectionHeight

        master.tryQueue(update.chunk, sectionHeight)
        val inPosition = update.position.inSectionPosition

        if (inPosition.y == 0) {
            master.tryQueue(update.chunk, sectionHeight - 1)
        } else if (inPosition.y == ChunkSize.SECTION_MAX_Y) {
            master.tryQueue(update.chunk, sectionHeight + 1)
        }
        if (inPosition.z == 0) {
            master.tryQueue(chunk = neighbours[Directions.NORTH], sectionHeight)
        } else if (inPosition.z == ChunkSize.SECTION_MAX_Z) {
            master.tryQueue(chunk = neighbours[Directions.SOUTH], sectionHeight)
        }
        if (inPosition.x == 0) {
            master.tryQueue(chunk = neighbours[Directions.WEST], sectionHeight)
        } else if (inPosition.x == ChunkSize.SECTION_MAX_X) {
            master.tryQueue(chunk = neighbours[Directions.EAST], sectionHeight)
        }
    }

    private fun ChunkRenderer.handle(update: SingleBlockDataUpdate) {
        master.tryQueue(update.chunk, update.position.sectionHeight)
    }

    private fun ChunkRenderer.handle(update: ChunkLocalBlockUpdate) {
        if (!update.chunk.neighbours.complete) return
        val sectionHeights: Int2ObjectOpenHashMap<BooleanArray> = Int2ObjectOpenHashMap()
        for ((position, state) in update.updates) {
            val neighbours = sectionHeights.getOrPut(position.sectionHeight) { BooleanArray(Directions.SIZE) }
            val inSectionHeight = position.y.inSectionHeight
            if (inSectionHeight == 0) {
                neighbours[0] = true
            } else if (inSectionHeight == ChunkSize.SECTION_MAX_Y) {
                neighbours[1] = true
            }
            if (position.z == 0) {
                neighbours[2] = true
            } else if (position.z == ChunkSize.SECTION_MAX_Z) {
                neighbours[3] = true
            }
            if (position.x == 0) {
                neighbours[4] = true
            } else if (position.x == ChunkSize.SECTION_MAX_X) {
                neighbours[5] = true
            }
        }
        val neighbours = update.chunk.neighbours.neighbours
        for ((sectionHeight, neighbourUpdates) in sectionHeights) {
            master.tryQueue(update.chunk, sectionHeight)

            if (neighbourUpdates[0]) {
                master.tryQueue(update.chunk, sectionHeight - 1)
            }
            if (neighbourUpdates[1]) {
                master.tryQueue(update.chunk, sectionHeight + 1)
            }
            if (neighbourUpdates[2]) {
                master.tryQueue(neighbours[Directions.NORTH], sectionHeight)
            }
            if (neighbourUpdates[3]) {
                master.tryQueue(neighbours[Directions.SOUTH], sectionHeight)
            }
            if (neighbourUpdates[4]) {
                master.tryQueue(neighbours[Directions.WEST], sectionHeight)
            }
            if (neighbourUpdates[5]) {
                master.tryQueue(neighbours[Directions.EAST], sectionHeight)
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
        val events = renderer.session.events

        events.listen<DimensionChangeEvent> { renderer.unloadWorld() }
        events.listen<WorldUpdateEvent> { renderer.handle(it.update) }

        renderer.session::state.observe(this) { if (it == PlaySessionStates.DISCONNECTED) renderer.unloadWorld() }
    }
}
