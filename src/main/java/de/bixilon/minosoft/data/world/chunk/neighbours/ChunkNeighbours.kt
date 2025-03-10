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

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.biome.accessor.noise.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.ChunkUtil
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import kotlin.math.abs

class ChunkNeighbours(val chunk: Chunk) {
    val neighbours = ChunkNeighbourArray()
    private var count = 0

    val complete: Boolean get() = count == ChunkNeighbourArray.COUNT

    operator fun set(offset: ChunkPosition, chunk: Chunk) {
        this.chunk.lock.lock()
        val current = neighbours[offset]
        neighbours[offset] = chunk
        if (current == null) {
            count++
            if (complete) {
                complete()
            }
        }
        this.chunk.lock.unlock()
    }

    fun remove(offset: ChunkPosition) {
        chunk.lock.lock()
        val current = neighbours[offset]
        if (current != null) {
            neighbours[offset] = null
            count--
        }
        chunk.lock.unlock()
    }

    fun completeSection(section: ChunkSection, sectionHeight: SectionHeight, noise: NoiseBiomeAccessor?) {
        section.neighbours = ChunkUtil.getNeighbours(neighbours, chunk, sectionHeight)
    }

    private fun complete() {
        val noise = chunk.world.biomes.noise
        for ((index, section) in chunk.sections.withIndex()) {
            if (section == null) continue
            val sectionHeight = index + chunk.minSection
            completeSection(section, sectionHeight, noise)
        }
        chunk.light.clear()
        chunk.light.calculate()
        chunk.light.propagate()
    }

    operator fun get(direction: Directions): Chunk? {
        return neighbours[direction]
    }

    operator fun get(offset: ChunkPosition): Chunk? {
        if (offset == ChunkPosition.EMPTY) return chunk
        return traceChunk(offset)
    }

    fun update(sectionHeight: Int) {
        for (nextSectionHeight in sectionHeight - 1..sectionHeight + 1) {
            if (nextSectionHeight < chunk.minSection || nextSectionHeight > chunk.maxSection) {
                continue
            }

            val section = chunk[nextSectionHeight] ?: continue
            val sectionNeighbours = ChunkUtil.getNeighbours(neighbours, chunk, nextSectionHeight)
            section.neighbours = sectionNeighbours
        }
    }

    fun traceChunk(offset: ChunkPosition): Chunk? {
        if (offset == ChunkPosition.EMPTY) return chunk
        if (abs(offset.x) <= 1 && abs(offset.z) <= 1) return this.neighbours[offset]

        // TODO: optimize diagonal trace
        return when {
            offset.z < 0 -> neighbours[Directions.NORTH]?.neighbours?.traceChunk(offset.plusZ())
            offset.z > 0 -> neighbours[Directions.SOUTH]?.neighbours?.traceChunk(offset.minusZ())
            offset.x < 0 -> neighbours[Directions.WEST]?.neighbours?.traceChunk(offset.plusX())
            offset.x > 0 -> neighbours[Directions.EAST]?.neighbours?.traceChunk(offset.minusX())
            else -> Broken("Invalid chunk offset: $offset")
        }
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
