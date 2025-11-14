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

package de.bixilon.minosoft.gui.rendering.chunk.queue.culled

import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class CulledQueue(
    private val renderer: ChunkRenderer,
) {
    private val queue: MutableMap<ChunkPosition, IntOpenHashSet> = mutableMapOf() // Chunk sections that can be prepared or have changed, but are not required to get rendered yet (i.e. culled chunks)
    private val lock = RWLock.rwlock()

    // TODO: split (out of view, out of frustum/occlusion)

    val size: Int get() = queue.size


    @Deprecated("cleanup????")
    fun cleanup(lock: Boolean) {
        if (lock) lock()
        val iterator = queue.iterator()
        for ((chunkPosition, _) in iterator) {
            if (renderer.visibility.isChunkVisible(chunkPosition)) {
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

    fun remove(position: SectionPosition, lock: Boolean) {
        if (lock) lock()

        queue[position.chunkPosition]?.let {
            if (it.remove(position.y) && it.isEmpty()) {
                queue -= position.chunkPosition
            }
        }

        if (lock) unlock()
    }


    fun collect(): MutableList<ChunkSection> {
        renderer.lock.acquire()
        lock.acquire() // The queue method needs the full lock of the culledQueue

        val world = renderer.world

        world.lock.acquire()

        val list: MutableList<ChunkSection> = mutableListOf()

        val queueIterator = this.queue.iterator()
        for ((chunkPosition, sectionHeights) in queueIterator) {
            if (!renderer.visibility.isChunkVisible(chunkPosition)) {
                continue
            }
            val chunk = world.chunks.chunks.unsafe[chunkPosition] ?: continue
            if (!chunk.neighbours.complete) continue

            val heightIterator = sectionHeights.intIterator()
            while (heightIterator.hasNext()) {
                val sectionHeight = heightIterator.nextInt()
                val section = chunk[sectionHeight] ?: continue
                if (!renderer.visibility.isSectionVisible(section)) {
                    continue
                }
                list += section
                heightIterator.remove()
            }
            if (sectionHeights.isEmpty()) {
                queueIterator.remove()
            }
        }
        world.lock.release()

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
