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

package de.bixilon.minosoft.gui.rendering.world.queue.meshing

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.length2
import de.bixilon.minosoft.gui.rendering.world.WorldQueueItem
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.world.queue.QueuePosition
import de.bixilon.minosoft.gui.rendering.world.queue.meshing.tasks.MeshPrepareTask
import de.bixilon.minosoft.gui.rendering.world.queue.meshing.tasks.MeshPrepareTaskManager
import de.bixilon.minosoft.util.SystemInformation

class ChunkMeshingQueue(
    private val renderer: WorldRenderer,
) {
    val tasks = MeshPrepareTaskManager(renderer)
    val maxMeshesToLoad = if (SystemInformation.RUNTIME.maxMemory() > 1_000_000_000) 150 else 80

    @Volatile
    private var working = false
    private val queue: MutableList<WorldQueueItem> = mutableListOf()
    private val set: MutableSet<WorldQueueItem> = HashSet()

    private val lock = SimpleLock()


    val size: Int get() = queue.size


    fun sort() {
        lock()
        val position = renderer.cameraChunkPosition
        val height = renderer.cameraSectionHeight
        val cameraSectionPosition = Vec3i(position.x, height, position.y)
        queue.sortBy {
            if (it.chunkPosition == position) {
                // our own chunk always has the highest priority
                return@sortBy -Int.MAX_VALUE
            }
            (it.sectionPosition - cameraSectionPosition).length2()
        }
        unlock()
    }


    fun work() {
        if (working) return // do not work twice
        val size = tasks.size
        if (queue.isEmpty() || size >= tasks.max || renderer.loadingQueue.size >= maxMeshesToLoad) {
            return
        }
        working = true


        val items: MutableList<WorldQueueItem> = mutableListOf()
        lock()
        for (i in 0 until tasks.max - size) {
            if (queue.isEmpty()) {
                break
            }
            val item = queue.removeFirst()
            set -= item
            items += item
        }
        unlock()
        for (item in items) {
            val runnable = ThreadPoolRunnable(if (item.chunkPosition == renderer.cameraChunkPosition) ThreadPool.HIGH else ThreadPool.LOW, interruptable = true) // ToDo: Also make neighbour chunks important
            val task = MeshPrepareTask(item.chunkPosition, item.sectionHeight, runnable)
            task.runnable.runnable = Runnable { renderer.mesher.tryMesh(item, task, task.runnable) }
            tasks += task
        }
        working = false
    }


    fun remove(chunkPosition: ChunkPosition) {
        val remove: MutableSet<WorldQueueItem> = mutableSetOf()
        queue.removeAll {
            if (it.chunkPosition != chunkPosition) {
                return@removeAll false
            }
            remove += it
            return@removeAll true
        }
        set -= remove
    }

    fun cleanup(lock: Boolean) {
        if (lock) lock()
        val remove: MutableSet<WorldQueueItem> = mutableSetOf()
        queue.removeAll {
            if (renderer.visibilityGraph.isChunkVisible(it.chunkPosition)) {
                return@removeAll false
            }
            remove += it
            return@removeAll true
        }
        set -= remove
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
        this.set.clear()
        if (lock) unlock()
    }


    fun remove(item: WorldQueueItem, lock: Boolean) {
        if (lock) lock()
        if (this.set.remove(item)) {
            this.queue -= item
        }
        if (lock) unlock()
    }

    fun remove(position: QueuePosition, lock: Boolean) {
        if (lock) lock()
        // dirty hacking
        if (this.set.unsafeCast<MutableSet<QueuePosition>>().remove(position)) {
            this.queue.unsafeCast<MutableList<QueuePosition>>() -= position
        }
        if (lock) unlock()
    }


    fun queue(item: WorldQueueItem) {
        lock()
        // TODO: don't remove and readd
        if (set.remove(item)) {
            queue -= item
        }
        if (item.chunkPosition == renderer.cameraChunkPosition) {
            queue.add(0, item)
        } else {
            queue += item
        }
        set += item
        unlock()
    }
}
