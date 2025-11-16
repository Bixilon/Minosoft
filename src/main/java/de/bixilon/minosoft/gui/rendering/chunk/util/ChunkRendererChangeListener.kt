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
import de.bixilon.minosoft.data.world.chunk.update.block.SingleBlockUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkDataUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkLightUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkUnloadUpdate
import de.bixilon.minosoft.data.world.chunk.update.chunk.NeighbourSetUpdate
import de.bixilon.minosoft.gui.rendering.RenderingStates
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.modding.event.listener.CallbackEventListener.Companion.listen
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

object ChunkRendererChangeListener {

    private fun ChunkRenderer.handle(update: SingleBlockUpdate) {
        if (!update.chunk.neighbours.complete) return
        val neighbours = update.chunk.neighbours
        val sectionHeight = update.position.sectionHeight

        invalidate(update.chunk, sectionHeight)
        val inPosition = update.position.inSectionPosition

        if (inPosition.y == 0) {
            invalidate(update.chunk, sectionHeight - 1)
        } else if (inPosition.y == ChunkSize.SECTION_MAX_Y) {
            invalidate(update.chunk, sectionHeight + 1)
        }
        if (inPosition.z == 0) {
            invalidate(neighbours[Directions.NORTH], sectionHeight)
        } else if (inPosition.z == ChunkSize.SECTION_MAX_Z) {
            invalidate(neighbours[Directions.SOUTH], sectionHeight)
        }
        if (inPosition.x == 0) {
            invalidate(neighbours[Directions.WEST], sectionHeight)
        } else if (inPosition.x == ChunkSize.SECTION_MAX_X) {
            invalidate(neighbours[Directions.EAST], sectionHeight)
        }
    }

    private fun ChunkRenderer.handle(update: ChunkLocalBlockUpdate) {
        if (!update.chunk.neighbours.complete) return
        val sectionHeights: Int2ObjectOpenHashMap<BooleanArray> = Int2ObjectOpenHashMap()
        for ((position, _) in update.change) {
            val neighbours = sectionHeights.getOrPut(position.sectionHeight) { BooleanArray(Directions.SIZE) }
            val inSectionHeight = position.y.inSectionHeight
            if (inSectionHeight == 0) {
                neighbours[Directions.O_DOWN] = true
            } else if (inSectionHeight == ChunkSize.SECTION_MAX_Y) {
                neighbours[Directions.O_UP] = true
            }
            if (position.z == 0) {
                neighbours[Directions.O_NORTH] = true
            } else if (position.z == ChunkSize.SECTION_MAX_Z) {
                neighbours[Directions.O_SOUTH] = true
            }
            if (position.x == 0) {
                neighbours[Directions.O_WEST] = true
            } else if (position.x == ChunkSize.SECTION_MAX_X) {
                neighbours[Directions.O_EAST] = true
            }
        }
        val neighbours = update.chunk.neighbours
        for ((sectionHeight, neighbourUpdates) in sectionHeights) {
            invalidate(update.chunk, sectionHeight)

            if (neighbourUpdates[0]) {
                invalidate(update.chunk, sectionHeight - 1)
            }
            if (neighbourUpdates[1]) {
                invalidate(update.chunk, sectionHeight + 1)
            }
            if (neighbourUpdates[2]) {
                invalidate(neighbours[Directions.NORTH], sectionHeight)
            }
            if (neighbourUpdates[3]) {
                invalidate(neighbours[Directions.SOUTH], sectionHeight)
            }
            if (neighbourUpdates[4]) {
                invalidate(neighbours[Directions.WEST], sectionHeight)
            }
            if (neighbourUpdates[5]) {
                invalidate(neighbours[Directions.EAST], sectionHeight)
            }
        }
    }

    private fun ChunkRenderer.handle(update: ChunkLightUpdate) {
        if (update.cause == ChunkLightUpdate.Causes.BLOCK_CHANGE) return // change is already covered
        // TODO: Enqueue neighbour sections (light level from cullface)
        invalidate(update.section)
    }


    private fun ChunkRenderer.handle(update: ChunkUnloadUpdate) {
        unload(update.chunk)
    }

    private fun ChunkRenderer.handle(update: NeighbourSetUpdate) {
        invalidate(update.chunk)
    }

    private fun ChunkRenderer.handle(update: ChunkDataUpdate) {
        for (section in update.sections) {
            invalidate(section)
        }
    }


    private fun ChunkRenderer.handle(update: AbstractWorldUpdate) {
        if (context.state == RenderingStates.PAUSED || context.state == RenderingStates.QUITTING || context.state == RenderingStates.STOPPED) return
        when (update) {
            is NeighbourSetUpdate -> handle(update)
            is SingleBlockUpdate -> handle(update)
            is ChunkLocalBlockUpdate -> handle(update)
            is ChunkLightUpdate -> handle(update)
            is ChunkUnloadUpdate -> handle(update)
            is ChunkDataUpdate -> handle(update)
            else -> Log.log(LogMessageType.OTHER, LogLevels.WARN) { "Unknown world update happened: $update" }
        }
    }

    fun register(renderer: ChunkRenderer) {
        val events = renderer.session.events

        events.listen<WorldUpdateEvent> { renderer.handle(it.update) }

        renderer.session::state.observe(this) { if (it == PlaySessionStates.DISCONNECTED) renderer.unload(renderer.world) }
    }
}
