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

import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantLock
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer

class CulledQueue(
    private val renderer: ChunkRenderer,
) {
    private val viewDistance: HashMap<ChunkPosition, Chunk> = HashMap()
    private val culled: HashMap<ChunkPosition, HashSet<ChunkSection>> = HashMap()
    private val lock = ReentrantLock()

    val size: Int get() = viewDistance.size + culled.size


    fun clear() = lock.locked {
        viewDistance.clear()
        culled.clear()
    }


    operator fun minusAssign(chunk: Chunk) = lock.locked {
        viewDistance -= chunk.position
        culled -= chunk.position
    }

    operator fun minusAssign(section: ChunkSection): Unit = lock.locked {
        culled[section.chunk.position]?.remove(section)
    }

    @Deprecated("why?")
    operator fun plusAssign(chunk: Chunk) = lock.locked {
        if (!renderer.visibility.isChunkVisible(chunk.position)) {
            viewDistance[chunk.position] = chunk
        } else {
            TODO("Enqueue all sections of chunk?")
        }
    }

    operator fun plusAssign(section: ChunkSection): Unit = lock.locked {
        if (!renderer.visibility.isChunkVisible(section.chunk.position)) {
            viewDistance[section.chunk.position] = section.chunk
        } else {
            culled.getOrPut(section.chunk.position) { HashSet() } += section
        }
    }

    fun enqueueViewDistance(): Int {
        if (viewDistance.isEmpty()) return 0
        lock.lock()

        var count = 0

        val iterator = viewDistance.values.iterator()
        while (iterator.hasNext()) {
            val chunk = iterator.next()
            if (!renderer.visibility.isChunkVisible(chunkPosition)) continue

            iterator.remove()

            for (height in chunk.sections.lowest..chunk.sections.highest) {
                val section = chunk.sections[height] ?: continue


                if (!renderer.visibility.isSectionVisible(section)) continue

                // TODO: enqueue
                count++
            }
        }

        lock.unlock()

        return count
    }

    fun enqueue(): Int {
        if (culled.isEmpty()) return 0
        lock.lock()

        var count = 0

        val iterator = culled.values.iterator()
        while (iterator.hasNext()) {
            val sections = iterator.next()

            val sectionIterator = sections.iterator()
            while (sectionIterator.hasNext()) {
                val section = sectionIterator.next()
                if (!renderer.visibility.isSectionVisible(section)) continue

                sectionIterator.remove()

                // TODO: enqueue
                count++
            }

            if (sections.isEmpty()) {
                iterator.remove()
            }
        }

        lock.unlock()

        return count
    }
}
