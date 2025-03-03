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

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.ChunkPosition

@JvmInline
value class ChunkNeighbourArray(val array: Array<Chunk?>) {

    constructor() : this(arrayOfNulls(OFFSETS.size))


    private operator fun get(index: Int) = array[index]
    private operator fun set(index: Int, chunk: Chunk?) {
        array[index] = chunk
    }

    operator fun get(direction: Directions): Chunk? {
        if (direction.axis == Axes.Y) throw IllegalArgumentException("Chunk neighbours are 2D!")
        return this[BY_DIRECTION[direction.ordinal - Directions.SIDE_OFFSET]]
    }

    operator fun get(offset: ChunkPosition) = this[offset.neighbourIndex]

    operator fun set(offset: ChunkPosition, chunk: Chunk?) {
        this[offset.neighbourIndex] = chunk
    }


    companion object {
        const val COUNT = 8
        private val BY_DIRECTION = intArrayOf(3, 4, 1, 6)

        /**
         * 0 | 3 | 5
         * 1 | - | 6
         * 2 | 4 | 7
         */
        private val _0 = ChunkPosition(-1, -1)
        private val _1 = ChunkPosition(-1, +0)
        private val _2 = ChunkPosition(-1, +1)
        private val _3 = ChunkPosition(+0, -1)
        private val _4 = ChunkPosition(+0, +1)
        private val _5 = ChunkPosition(+1, -1)
        private val _6 = ChunkPosition(+1, +0)
        private val _7 = ChunkPosition(+1, +1)


        val OFFSETS = longArrayOf(_0.raw, _1.raw, _2.raw, _3.raw, _4.raw, _5.raw, _6.raw, _7.raw)

        private val ChunkPosition.neighbourIndex: Int
            get() = when (this) { // TODO: use some hash table
                _0 -> 0
                _1 -> 1
                _2 -> 2
                _3 -> 3
                _4 -> 4
                _5 -> 5
                _6 -> 6
                _7 -> 7
                else -> throw IllegalArgumentException("Invalid chunk offset: $this")
            }
    }
}
