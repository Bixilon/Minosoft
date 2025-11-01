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

package de.bixilon.minosoft.data.world.chunk.neighbours

import de.bixilon.kutil.exception.Unreachable
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbourUtil.neighbourIndex
import de.bixilon.minosoft.data.world.chunk.update.chunk.ChunkLightUpdate
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import kotlin.math.abs

class ChunkNeighbours(val chunk: Chunk) {
    val array: Array<Chunk?> = arrayOfNulls(ChunkNeighbourUtil.COUNT)
    private var count = 0

    val complete: Boolean get() = count == ChunkNeighbourUtil.COUNT

    @JvmName("setOffset")
    operator fun set(offset: ChunkPosition, chunk: Chunk) {
        this.chunk.lock.lock()
        val index = offset.neighbourIndex
        val current = array[index]
        array[index] = chunk
        if (current == null) {
            count++
        }
        updateNeighbours()
        this.chunk.lock.unlock()
    }

    fun remove(offset: ChunkPosition) {
        chunk.lock.lock()
        val index = offset.neighbourIndex
        val current = array[index]
        if (current != null) {
            array[index] = null
            count--
        }
        updateNeighbours()
        chunk.lock.unlock()
    }

    fun updateNeighbourNeighbours(height: Int, section: ChunkSection?) {
        chunk.sections[height - 1]?.neighbours?.set(Directions.O_UP, section)
        chunk.sections[height + 1]?.neighbours?.set(Directions.O_DOWN, section)

        chunk.neighbours[Directions.NORTH]?.sections?.get(height)?.neighbours?.set(Directions.O_SOUTH, section)
        chunk.neighbours[Directions.SOUTH]?.sections?.get(height)?.neighbours?.set(Directions.O_NORTH, section)
        chunk.neighbours[Directions.WEST]?.sections?.get(height)?.neighbours?.set(Directions.O_EAST, section)
        chunk.neighbours[Directions.EAST]?.sections?.get(height)?.neighbours?.set(Directions.O_WEST, section)
    }

    fun updateNeighbours(section: ChunkSection) {
        section.neighbours[Directions.O_DOWN] = chunk[section.height - 1]
        section.neighbours[Directions.O_UP] = chunk[section.height + 1]

        section.neighbours[Directions.O_NORTH] = chunk.neighbours[Directions.NORTH]?.get(section.height)
        section.neighbours[Directions.O_SOUTH] = chunk.neighbours[Directions.SOUTH]?.get(section.height)
        section.neighbours[Directions.O_WEST] = chunk.neighbours[Directions.WEST]?.get(section.height)
        section.neighbours[Directions.O_EAST] = chunk.neighbours[Directions.EAST]?.get(section.height)
    }

    fun updateNeighbours() {
        val dimension = chunk.world.dimension
        for (height in dimension.minSection..dimension.maxSection) {
            val section = chunk.sections[height]
            section?.let { updateNeighbours(it) }

            updateNeighbourNeighbours(height, section)
        }

        if (complete) {
            chunk.light.recalculate(false, ChunkLightUpdate.Causes.NEIGHBOUR_CHANGE)
            chunk.light.propagateFromNeighbours(fireEvent = false, ChunkLightUpdate.Causes.NEIGHBOUR_CHANGE)
        }
    }

    operator fun get(direction: Directions): Chunk? {
        assert(direction.axis != Axes.Y) { "There are no vertical neighbours!" }
        return this.array[ChunkNeighbourUtil.BY_DIRECTION[direction.ordinal]]
    }

    operator fun get(offset: ChunkPosition): Chunk? {
        if (offset == ChunkPosition.EMPTY) return chunk
        return traceChunk(offset)
    }

    fun traceChunk(offset: ChunkPosition): Chunk? {
        if (offset == ChunkPosition.EMPTY) return chunk
        if (abs(offset.x) <= 1 && abs(offset.z) <= 1) return this.array[offset.neighbourIndex]

        // TODO: optimize diagonal trace
        return when {
            offset.z < 0 -> this[Directions.NORTH]?.neighbours?.traceChunk(offset.plusZ())
            offset.z > 0 -> this[Directions.SOUTH]?.neighbours?.traceChunk(offset.minusZ())
            offset.x < 0 -> this[Directions.WEST]?.neighbours?.traceChunk(offset.plusX())
            offset.x > 0 -> this[Directions.EAST]?.neighbours?.traceChunk(offset.minusX())
            else -> Unreachable()
        }
    }

    operator fun set(offset: ChunkPosition, chunk: Chunk?) {
        this.array[offset.neighbourIndex] = chunk
    }

    fun traceBlock(position: BlockPosition): BlockState? {
        val chunkPosition = position.chunkPosition
        val chunkDelta = (chunkPosition - chunk.position)
        val chunk = traceChunk(chunkDelta)
        return chunk?.get(position.inChunkPosition)
    }

    fun traceBlock(origin: InChunkPosition, offset: BlockPosition) = traceBlock(BlockPosition.of(chunk.position, origin) + offset)
    fun traceBlock(origin: InChunkPosition, direction: Directions) = traceBlock(BlockPosition.of(chunk.position, origin) + direction)
}
