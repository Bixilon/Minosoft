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
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer

class CulledQueue(
    private val renderer: ChunkRenderer,
) {
    private val viewDistance: HashSet<Chunk> = HashSet()
    private val culled: HashMap<ChunkPosition, HashSet<ChunkSection>> = HashMap() // TODO: flatten to set?
    private val lock = ReentrantLock()

    val size: Int get() = viewDistance.size + culled.size


    fun clear() = lock.locked {
        viewDistance.clear()
        culled.clear()
    }

    operator fun minusAssign(chunk: Chunk) = lock.locked {
        viewDistance -= chunk
        culled -= chunk.position
    }

    operator fun minusAssign(section: ChunkSection): Unit = lock.locked {
        culled[section.chunk.position]?.remove(section)
    }

    operator fun plusAssign(chunk: Chunk) = lock.locked {
        if (!renderer.visibility.isInViewDistance(chunk.position)) {
            viewDistance += chunk
        } else {
            val culled = culled.getOrPut(chunk.position) { HashSet() }
            chunk.sections.forEach { culled += it }
        }
    }

    operator fun plusAssign(section: ChunkSection): Unit = lock.locked {
        if (!renderer.visibility.isInViewDistance(SectionPosition.of(section))) {
            viewDistance += section.chunk
        } else {
            culled.getOrPut(section.chunk.position) { HashSet() } += section
        }
    }

    private fun moveToCulled() {
        if (viewDistance.isEmpty()) return

        val iterator = viewDistance.iterator()
        while (iterator.hasNext()) {
            val chunk = iterator.next()
            if (!renderer.visibility.isInViewDistance(chunk.position)) continue

            iterator.remove()
            if (chunk.sections.lowest > chunk.sections.highest) continue // no sections

            val culled = culled.getOrPut(chunk.position) { HashSet() }

            chunk.sections.forEach { culled += it }
        }
    }

    private fun moveToViewDistance() {
        if (culled.isEmpty()) return

        val iterator = culled.iterator()
        while (iterator.hasNext()) {
            val (position, sections) = iterator.next()
            if (sections.isEmpty()) {
                iterator.remove()
                continue
            }
            if (renderer.visibility.isInViewDistance(position)) continue

            iterator.remove()

            this.viewDistance += sections.iterator().next().chunk // dirty hack
        }
    }

    private fun moveToMeshQueue() {
        if (culled.isEmpty()) return

        renderer.meshingQueue.lock.lock()

        val iterator = culled.iterator()
        while (iterator.hasNext()) {
            val (position, sections) = iterator.next()
            if (position !in renderer.visibility) continue

            val sectionIterator = sections.iterator()
            while (sectionIterator.hasNext()) {
                val section = sectionIterator.next()
                if (section !in renderer.visibility) continue

                sectionIterator.remove()

                renderer.meshingQueue.unsafeAdd(section)
            }

            if (sections.isEmpty()) {
                iterator.remove()
            }
        }

        renderer.meshingQueue.sort()
        renderer.meshingQueue.lock.unlock()
    }

    fun enqueueViewDistance() = lock.locked {
        moveToViewDistance()
        moveToCulled()
    }

    fun enqueue() = lock.locked { moveToMeshQueue() }
}
