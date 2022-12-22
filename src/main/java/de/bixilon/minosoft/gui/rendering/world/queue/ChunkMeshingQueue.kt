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

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.length2
import de.bixilon.minosoft.gui.rendering.world.SectionPrepareTask
import de.bixilon.minosoft.gui.rendering.world.WorldQueueItem
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import de.bixilon.minosoft.util.SystemInformation

class ChunkMeshingQueue(
    private val renderer: WorldRenderer,
) {
    val maxPreparingTasks = maxOf(DefaultThreadPool.threadCount - 2, 1)
    val maxMeshesToLoad = if (SystemInformation.RUNTIME.maxMemory() > 1_000_000_000) 150 else 80

    @Volatile
    private var working = false
    private val queue: MutableList<WorldQueueItem> = mutableListOf() // queue, that is visible, and should be rendered
    private val set: MutableSet<WorldQueueItem> = HashSet() // queue, that is visible, and should be rendered

    val lock = SimpleLock()

    val preparingTasks: MutableSet<SectionPrepareTask> = mutableSetOf() // current running section preparing tasks
    val preparingTasksLock = SimpleLock()


    val size: Int get() = queue.size


    fun sort() {
        lock.lock()
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
        lock.unlock()
    }


    fun work() {
        if (working) return // do not work twice
        val size = preparingTasks.size
        if (queue.isEmpty() || size >= maxPreparingTasks || renderer.loadingQueue.size >= maxMeshesToLoad) {
            return
        }
        working = true


        val items: MutableList<WorldQueueItem> = mutableListOf()
        lock.lock()
        for (i in 0 until maxPreparingTasks - size) {
            if (queue.isEmpty()) {
                break
            }
            val item = queue.removeFirst()
            set.remove(item)
            items += item
        }
        lock.unlock()
        for (item in items) {
            val task = SectionPrepareTask(item.chunkPosition, item.sectionHeight, ThreadPoolRunnable(if (item.chunkPosition == renderer.cameraChunkPosition) ThreadPool.HIGH else ThreadPool.LOW, interruptable = true)) // Our own chunk is the most important one ToDo: Also make neighbour chunks important
            task.runnable.runnable = Runnable {
                renderer.prepareItem(item, task, task.runnable)
            }
            preparingTasksLock.lock()
            preparingTasks += task
            preparingTasksLock.unlock()
            DefaultThreadPool += task.runnable
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

    fun cleanup() {
        val remove: MutableSet<WorldQueueItem> = mutableSetOf()
        queue.removeAll {
            if (renderer.visibilityGraph.isChunkVisible(it.chunkPosition)) {
                return@removeAll false
            }
            remove += it
            return@removeAll true
        }
        set -= remove
    }

    fun lock() {
        this.lock.lock()
    }

    fun unlock() {
        this.lock.unlock()
    }

    fun interrupt(position: ChunkPosition) {
        preparingTasksLock.acquire()
        for (task in preparingTasks) {
            if (task.chunkPosition == position) {
                task.runnable.interrupt()
            }
        }
        preparingTasksLock.release()
    }

    fun interrupt() {
        preparingTasksLock.acquire()
        for (task in preparingTasks) {
            task.runnable.interrupt()
        }
        preparingTasksLock.release()
    }

    fun interrupt(position: ChunkPosition, height: SectionHeight) {
        preparingTasksLock.acquire()
        for (task in preparingTasks) {
            if (task.chunkPosition == position && task.sectionHeight == height) {
                task.runnable.interrupt()
            }
        }
        preparingTasksLock.release()
    }

    fun interruptCleanup() {
        preparingTasksLock.acquire()
        for (task in preparingTasks) {
            if (!renderer.visibilityGraph.isChunkVisible(task.chunkPosition)) {
                task.runnable.interrupt()
            }
        }
        preparingTasksLock.release()
    }

    fun clear() {

    }

    fun remove(task: SectionPrepareTask) {
        preparingTasksLock.lock()
        preparingTasks -= task
        preparingTasksLock.unlock()
    }

    operator fun minusAssign(task: SectionPrepareTask) = remove(task)


    fun remove(position: ChunkPosition, height: SectionHeight) {

    }


    fun queue(item: WorldQueueItem) {
        lock.lock()
        if (set.remove(item)) {
            queue.remove(item) // Prevent duplicated entries (to not prepare the same chunk twice (if it changed and was not prepared yet or ...)
        }
        if (item.chunkPosition == renderer.cameraChunkPosition) {
            queue.add(0, item)
        } else {
            queue += item
        }
        set += item
        lock.unlock()
    }
}
