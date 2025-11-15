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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ThreadPoolRunnable
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.queue.ChunkQueueItem
import de.bixilon.minosoft.gui.rendering.chunk.queue.QueuePosition
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.tasks.MeshPrepareTask
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.tasks.MeshPrepareTaskManager
import de.bixilon.minosoft.util.SystemInformation
import kotlin.math.abs

class ChunkMeshingQueue(
    private val renderer: ChunkRenderer,
) {
    private val comparator = ChunkQueueComparator()
    val tasks = MeshPrepareTaskManager(renderer)
    val maxMeshesToLoad = if (SystemInformation.RUNTIME.maxMemory() > 1_000_000_000) 100 else 60

    private var working = false
    private val queue: MutableList<ChunkQueueItem> = ArrayList()
    private val set: MutableSet<ChunkQueueItem> = HashSet()

    private val lock = ReentrantLock()


    val size: Int get() = queue.size


    fun sort() = lock.locked {
        comparator.update(renderer)
        queue.sortWith(comparator)
    }

    operator fun plusAssign(item: ChunkQueueItem)
    operator fun minusAssign(position: ChunkPosition)
    operator fun minusAssign(position: SectionPosition)


    fun work() {
        if (working) return // do not work twice
        val size = tasks.size
        if (queue.isEmpty() || size >= tasks.max || renderer.loadingQueue.size >= maxMeshesToLoad) {
            return
        }
        working = true


        val items: MutableList<ChunkQueueItem> = ArrayList(tasks.max - size)
        lock()
        for (i in 0 until tasks.max - size) {
            if (queue.isEmpty()) {
                break
            }
            val item = queue.removeAt(0)
            set -= item
            items += item
        }
        unlock()
        val camera = renderer.cameraSectionPosition
        for (item in items) {
            val distance = abs(item.position.x - camera.x) + abs(item.position.z - camera.z) // TODO: Should y get in here too?
            val task = MeshPrepareTask(item.position)
            tasks += task

            DefaultThreadPool += ThreadPoolRunnable(forcePool = true, priority = if (distance <= 1) ThreadPool.Priorities.HIGH else ThreadPool.Priorities.LOW) { renderer.mesher.tryMesh(item, task) }
        }
        working = false
    }


    @Deprecated("shit")
    private fun remove(chunkPosition: ChunkPosition) {
        val remove: MutableSet<ChunkQueueItem> = mutableSetOf()
        queue.removeAll {
            if (it.position.chunkPosition != chunkPosition) {
                return@removeAll false
            }
            remove += it
            return@removeAll true
        }
        set -= remove
    }


    @Deprecated("shit")
    private fun cleanup(lock: Boolean) {
        if (lock) lock()
        val remove: MutableSet<ChunkQueueItem> = mutableSetOf()
        queue.removeAll {
            if (renderer.visibility.isChunkVisible(it.section.chunk)) {
                return@removeAll false
            }
            remove += it
            return@removeAll true
        }
        set -= remove
        if (lock) unlock()
    }


    @Deprecated("shit")
    private fun clear(lock: Boolean) {
        if (lock) lock()
        this.queue.clear()
        this.set.clear()
        if (lock) unlock()
    }


    @Deprecated("shit")
    private fun remove(item: ChunkQueueItem, lock: Boolean) {
        if (lock) lock()
        if (this.set.remove(item)) {
            this.queue -= item
        }
        if (lock) unlock()
    }

    @Deprecated("shit")
    private fun remove(position: QueuePosition, lock: Boolean) {
        if (lock) lock()
        // dirty hacking
        if (this.set.unsafeCast<MutableSet<QueuePosition>>().remove(position)) {
            this.queue.unsafeCast<MutableList<QueuePosition>>() -= position
        }
        if (lock) unlock()
    }

    @Deprecated("shit")
    private fun queue(item: ChunkQueueItem) {
        lock()
        if (set.add(item)) {
            if (item.position.chunkPosition == renderer.cameraSectionPosition.chunkPosition) {
                queue.add(0, item)
            } else {
                queue += item
            }
        }
        // TODO: move to front if own chunk

        unlock()
    }
}
