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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.world.biome.accessor.noise.NoiseBiomeAccessor
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.ChunkUtil

class ChunkNeighbours(val chunk: Chunk) : Iterable<Chunk?> {
    val neighbours: Array<Chunk?> = arrayOfNulls(COUNT)
    private var count = 0

    val complete: Boolean get() = count == COUNT

    fun get(): Array<Chunk>? {
        if (count == COUNT) { // TODO: Race condition!
            return neighbours.unsafeCast()
        }
        return null
    }

    @Deprecated("index")
    operator fun set(index: Int, chunk: Chunk) {
        this.chunk.lock.lock()
        val current = neighbours[index]
        neighbours[index] = chunk
        if (current == null) {
            count++
            if (count == COUNT) {
                complete(get()!!)
            }
        }
        this.chunk.lock.unlock()
    }

    operator fun set(offset: ChunkPosition, chunk: Chunk) {
        set(getIndex(offset), chunk)
    }

    @Deprecated("index")
    fun remove(index: Int) {
        chunk.lock.lock()
        val current = neighbours[index]
        neighbours[index] = null
        if (current != null) {
            count--
        }
        chunk.lock.unlock()
    }

    fun remove(offset: ChunkPosition) {
        remove(getIndex(offset))
    }

    fun completeSection(neighbours: Array<Chunk>, section: ChunkSection, sectionHeight: SectionHeight, noise: NoiseBiomeAccessor?) {
        section.neighbours = ChunkUtil.getDirectNeighbours(neighbours, chunk, sectionHeight)
    }

    private fun complete(neighbours: Array<Chunk>) {
        val noise = chunk.world.biomes.noise
        for ((index, section) in chunk.sections.withIndex()) {
            if (section == null) continue
            val sectionHeight = index + chunk.minSection
            completeSection(neighbours, section, sectionHeight, noise)
        }
        chunk.light.recalculate(false)
        chunk.light.propagateFromNeighbours(fireEvent = false, fireSameChunkEvent = false)
    }

    @Deprecated("index")
    operator fun get(index: Int): Chunk? {
        return neighbours[index]
    }

    operator fun get(offset: ChunkPosition): Chunk {
        if (offset.xz == 0) return chunk
        return this[getIndex(offset)] // TODO: trace
    }

    override fun iterator(): Iterator<Chunk?> {
        return neighbours.iterator()
    }


    fun update(neighbours: Array<Chunk>, sectionHeight: Int) {
        for (nextSectionHeight in sectionHeight - 1..sectionHeight + 1) {
            if (nextSectionHeight < chunk.minSection || nextSectionHeight > chunk.maxSection) {
                continue
            }

            val section = chunk[nextSectionHeight] ?: continue
            val sectionNeighbours = ChunkUtil.getDirectNeighbours(neighbours, chunk, nextSectionHeight)
            section.neighbours = sectionNeighbours
        }
    }

    fun traceChunk(offset: ChunkPosition): Chunk = when {
        offsetX == 0 -> when {
            offsetZ == 0 -> chunk
            offsetZ < 0 -> neighbours[3]?.neighbours?.trace(offsetX, offsetZ + 1)
            offsetZ > 0 -> neighbours[4]?.neighbours?.trace(offsetX, offsetZ - 1)
            else -> Broken()
        }

        offsetX < 0 -> when {
            offsetZ == 0 -> neighbours[1]?.neighbours?.trace(offsetX + 1, offsetZ)
            offsetZ < 0 -> neighbours[0]?.neighbours?.trace(offsetX + 1, offsetZ + 1)
            offsetZ > 0 -> neighbours[2]?.neighbours?.trace(offsetX + 1, offsetZ - 1)
            else -> Broken()
        }

        offsetX > 0 -> when {
            offsetZ == 0 -> neighbours[6]?.neighbours?.trace(offsetX - 1, offsetZ)
            offsetZ < 0 -> neighbours[5]?.neighbours?.trace(offsetX - 1, offsetZ + 1)
            offsetZ > 0 -> neighbours[7]?.neighbours?.trace(offsetX - 1, offsetZ - 1)
            else -> Broken()
        }

        else -> Broken()
    }

    fun traceBlock(position: BlockPosition): BlockState? {
        val chunkPosition = position.chunkPosition
        val chunkDelta = (chunkPosition - chunk.position)
        val chunk = traceChunk(chunkDelta)
        return chunk?.get(position.inChunkPosition)
    }

    fun traceBlock(origin: InChunkPosition, offset: BlockPosition) = traceBlock(offset - origin)
    fun traceBlock(origin: InChunkPosition, direction: Directions) = traceBlock((BlockPosition(origin) + direction))

    companion object {
        const val COUNT = 8
        const val NORTH = 3
        const val SOUTH = 4
        const val WEST = 1
        const val EAST = 6


        /**
         * 0 | 3 | 5
         * 1 | - | 6
         * 2 | 4 | 7
         */

        val OFFSETS = arrayOf(
            ChunkPosition(-1, -1), // 0
            ChunkPosition(-1, +0), // 1
            ChunkPosition(-1, +1), // 2
            ChunkPosition(+0, -1), // 3
            ChunkPosition(+0, +1), // 4
            ChunkPosition(+1, -1), // 5
            ChunkPosition(+1, +0), // 6
            ChunkPosition(+1, +1), // 7
        )
    }
}
