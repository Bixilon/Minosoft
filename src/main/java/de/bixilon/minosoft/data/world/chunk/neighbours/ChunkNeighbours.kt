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

package de.bixilon.minosoft.data.world.chunk.neighbours

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.util.chunk.ChunkUtil

class ChunkNeighbours(val chunk: Chunk) {
    val neighbours: Array<Chunk?> = arrayOfNulls(COUNT)
    private var count = 0

    val complete: Boolean get() = count == COUNT

    fun get(): Array<Chunk>? {
        if (count == COUNT) {
            return neighbours.unsafeCast()
        }
        return null
    }

    operator fun set(index: Int, chunk: Chunk) {
        chunk.lock.lock()
        val current = neighbours[index]
        neighbours[index] = chunk
        if (current == null) {
            count++
            if (count == COUNT) {
                complete(get()!!)
            }
        }
        chunk.lock.unlock()
    }

    operator fun set(offset: Vec2i, chunk: Chunk) {
        set(getIndex(offset), chunk)
    }

    fun remove(index: Int) {
        chunk.lock.lock()
        val current = neighbours[index]
        neighbours[index] = null
        if (current != null) {
            count--
            if (count < COUNT) {
                giveUp()
            }
        }
        chunk.lock.unlock()
    }

    fun remove(offset: Vec2i) {
        remove(getIndex(offset))
    }

    private fun complete(neighbours: Array<Chunk>) {
        updateSectionNeighbours(neighbours)
    }

    private fun updateSectionNeighbours(neighbours: Array<Chunk>) {
        for ((index, section) in chunk.sections!!.withIndex()) {
            if (section == null) {
                continue
            }
            val sectionNeighbours = ChunkUtil.getDirectNeighbours(neighbours, chunk, index + chunk.minSection)
            section.neighbours = sectionNeighbours
        }
    }

    private fun giveUp() {

    }

    operator fun get(index: Int): Chunk? {
        return neighbours[index]
    }

    operator fun get(offset: Vec2i): Chunk? {
        if (offset.x == 0 && offset.y == 0) {
            return chunk
        }
        return this[getIndex(offset)]
    }

    companion object {
        private const val COUNT = 8
        const val NORTH = 3
        const val SOUTH = 4
        const val WEST = 1
        const val EAST = 6

        val OFFSETS = arrayOf(
            Vec2i(-1, -1), // 0
            Vec2i(-1, +0), // 1
            Vec2i(-1, +1), // 2
            Vec2i(+0, -1), // 3
            Vec2i(+0, +1), // 4
            Vec2i(+1, -1), // 5
            Vec2i(+1, +0), // 6
            Vec2i(+1, +1), // 7
        )

        fun getIndex(offset: Vec2i): Int {
            return when {
                offset.x == -1 && offset.y == -1 -> 0
                offset.x == -1 && offset.y == 0 -> 1
                offset.x == -1 && offset.y == 1 -> 2
                offset.x == 0 && offset.y == -1 -> 3
                offset.x == 0 && offset.y == 1 -> 4
                offset.x == 1 && offset.y == -1 -> 5
                offset.x == 1 && offset.y == 0 -> 6
                offset.x == 1 && offset.y == 1 -> 7
                else -> Broken("Can not get neighbour chunk from offset $offset")
            }
        }
    }
}
