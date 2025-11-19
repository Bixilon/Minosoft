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

package de.bixilon.minosoft.gui.rendering.chunk.queue.meshing

import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ThreadPoolRunnable
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.tasks.MeshPrepareTask
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.tasks.MesherTaskManager
import kotlin.math.abs

class ChunkMeshingQueue(
    private val renderer: ChunkRenderer,
) {
    private val comparator = MeshQueueComparator()
    val tasks = MesherTaskManager(renderer)

    private var working = false
    private val queue = ArrayDeque<MeshQueueItem>(1000)
    private val positions: MutableSet<SectionPosition> = HashSet(1000)

    val lock = ReentrantLock()


    val size: Int get() = queue.size


    fun sort() = lock.locked {
        comparator.update(renderer.visibility.eyePosition)
        queue.sortWith(comparator)
    }

    fun clear() = lock.locked {
        queue.clear()
        this.positions.clear()
    }

    fun unsafeAdd(section: ChunkSection, cause: ChunkMeshingCause) {
        val position = SectionPosition.of(section)
        if (!positions.add(position)) return

        this.queue += MeshQueueItem(section, cause)
    }

    operator fun plusAssign(section: ChunkSection) = lock.locked {
        val position = SectionPosition.of(section)
        if (!positions.add(position)) return

        this.queue += MeshQueueItem(section, ChunkMeshingCause.UNKNOWN)
        queue.sortWith(comparator)
    }

    operator fun minusAssign(position: ChunkPosition) = removeIf(false) { it.chunkPosition == position }

    operator fun minusAssign(position: SectionPosition) = lock.locked {
        if (!this.positions.remove(position)) return@locked
        this.queue.removeIf { it.position == position } // TODO: only first
    }

    fun removeIf(requeue: Boolean, predicate: (position: SectionPosition) -> Boolean): Unit = lock.locked {
        val iterator = queue.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (!predicate.invoke(item.position)) continue

            if (requeue) {
                if (item.section in renderer.visibility) continue // it will just land in here again
                iterator.remove()

                renderer.unload(item.section) // TODO: don't remove from culled queue (and from meshing queue)
                renderer.culledQueue += item.section
            } else {
                iterator.remove()
            }


            this.positions -= item.position
        }
    }


    private fun enqueue(section: ChunkSection) {
        val camera = renderer.visibility.sectionPosition
        val delta = SectionPosition.of(section) - camera

        val distance = maxOf(abs(delta.x), abs(delta.y) - 2, abs(delta.z))
        val task = MeshPrepareTask(section)
        tasks += task

        DefaultThreadPool += ThreadPoolRunnable(forcePool = true, priority = if (distance <= 1) ThreadPool.Priorities.HIGH else ThreadPool.Priorities.LOW) {
            val position = SectionPosition.of(section)

            val previous = renderer.loaded[position]

            val mesh = try {
                task.thread = Thread.currentThread()
                renderer.mesher.mesh(previous, section)
            } catch (_: InterruptedException) {
                return@ThreadPoolRunnable
            } finally {
                task.thread = null
                Thread.interrupted() // clear interrupted flag
                tasks -= task
            }
            Thread.interrupted() // clear interrupted flag

            if (mesh == null) {
                renderer.loaded -= position
            } else {
                renderer.loadingQueue += mesh
            }

            work()
        }
    }

    fun work() {
        if (working) return
        if (queue.isEmpty() || tasks.size >= tasks.max || renderer.loadingQueue.size >= renderer.loadingQueue.max) {
            return
        }
        working = true

        lock.lock()
        while (queue.isNotEmpty()) {
            if (tasks.size >= tasks.max) break

            val item = queue.removeFirst()
            positions -= item.position
            enqueue(item.section)
        }
        lock.unlock()

        working = false
    }
}
