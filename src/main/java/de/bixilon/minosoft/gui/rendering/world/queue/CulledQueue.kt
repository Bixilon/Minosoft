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

package de.bixilon.minosoft.gui.rendering.world.queue

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.minosoft.data.world.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class CulledQueue(
    private val renderer: WorldRenderer,
) {
    private val queue: MutableMap<Vec2i, IntOpenHashSet> = mutableMapOf() // Chunk sections that can be prepared or have changed, but are not required to get rendered yet (i.e. culled chunks)
    private val lock = SimpleLock()

    val size: Int get() = queue.size


    fun cleanup(lock: Boolean) {
        if (lock) lock()
        val iterator = queue.iterator()
        for ((chunkPosition, _) in iterator) {
            if (renderer.visibilityGraph.isChunkVisible(chunkPosition)) {
                continue
            }
            iterator.remove()
        }
        if (lock) unlock()
    }

    fun lock() {
        renderer.lock.acquire()
        this.lock.lock()
    }

    fun unlock() {
        this.lock.unlock()
        renderer.lock.release()
    }


    fun clear(lock: Boolean) {
        if (lock) lock()
        this.queue.clear()
        if (lock) unlock()
    }

    fun remove(position: ChunkPosition, lock: Boolean) {
        if (lock) lock()

        queue -= position

        if (lock) unlock()
    }

    fun remove(position: ChunkPosition, height: SectionHeight, lock: Boolean) {
        if (lock) lock()

        queue[position]?.let {
            if (it.remove(height) && it.isEmpty()) {
                queue -= position
            }
        }

        if (lock) unlock()
    }


    fun collect(): MutableList<Pair<Chunk, Int>> {
        renderer.lock.acquire()
        lock.acquire() // The queue method needs the full lock of the culledQueue

        val world = renderer.world

        world.chunks.lock.acquire()

        val list: MutableList<Pair<Chunk, Int>> = mutableListOf()

        val queueIterator = this.queue.iterator()
        for ((chunkPosition, sectionHeights) in queueIterator) {
            if (!renderer.visibilityGraph.isChunkVisible(chunkPosition)) {
                continue
            }
            val chunk = world.chunks.unsafe[chunkPosition] ?: continue

            val heightIterator = sectionHeights.intIterator()
            for (sectionHeight in heightIterator) {
                val section = chunk[sectionHeight] ?: continue
                if (!renderer.visibilityGraph.isSectionVisible(chunkPosition, sectionHeight, section.blocks.minPosition, section.blocks.maxPosition, false)) {
                    continue
                }
                list += Pair(chunk, sectionHeight)
                heightIterator.remove()
            }
            if (sectionHeights.isEmpty()) {
                queueIterator.remove()
            }
        }
        world.chunks.lock.release()

        lock.release()
        renderer.lock.release()


        return list
    }

    fun queue(position: ChunkPosition, sectionHeight: SectionHeight) {
        lock.lock()
        queue.getOrPut(position) { IntOpenHashSet() } += sectionHeight
        lock.unlock()
    }
}
