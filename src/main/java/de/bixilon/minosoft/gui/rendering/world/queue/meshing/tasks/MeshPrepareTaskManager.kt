/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.world.queue.meshing.tasks

import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer

class MeshPrepareTaskManager(
    private val renderer: WorldRenderer,
    val max: Int = minOf(maxOf(Runtime.getRuntime().availableProcessors() - 1, 1), DefaultThreadPool.threadCount - 1),
) {
    private val tasks: MutableSet<MeshPrepareTask> = mutableSetOf() // current running section preparing tasks
    private val lock = SimpleLock()

    val size: Int get() = tasks.size


    fun add(task: MeshPrepareTask) {
        lock.lock()
        tasks += task
        lock.unlock()

        DefaultThreadPool += task.runnable
    }

    operator fun plusAssign(task: MeshPrepareTask) = add(task)

    fun remove(task: MeshPrepareTask) {
        lock.lock()
        tasks -= task
        lock.unlock()
    }

    operator fun minusAssign(task: MeshPrepareTask) = remove(task)

    fun interruptAll() {
        lock.acquire()
        for (task in tasks) {
            task.runnable.interrupt()
        }
        lock.release()
    }

    fun interrupt(position: ChunkPosition) {
        lock.acquire()
        for (task in tasks) {
            if (task.chunkPosition == position) {
                task.runnable.interrupt()
            }
        }
        lock.release()
    }

    fun interrupt(position: ChunkPosition, height: SectionHeight) {
        lock.acquire()
        for (task in tasks) {
            if (task.chunkPosition == position && task.sectionHeight == height) {
                task.runnable.interrupt()
            }
        }
        lock.release()
    }


    fun cleanup() {
        lock.acquire()
        for (task in tasks) {
            if (!renderer.visibilityGraph.isChunkVisible(task.chunkPosition)) {
                task.runnable.interrupt()
            }
        }
        lock.release()
    }
}
