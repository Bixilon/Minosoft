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

package de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.tasks

import de.bixilon.kutil.concurrent.lock.LockUtil.acquired
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantRWLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer

class MesherTaskManager(
    val renderer: ChunkRenderer,
    val max: Int = maxOf(minOf(Runtime.getRuntime().availableProcessors() - 1, DefaultThreadPool.threadCount - 1), 1)
) {
    private val tasks: MutableSet<MeshPrepareTask> = HashSet(max)
    private val lock = ReentrantRWLock()

    val size get() = tasks.size

    operator fun plusAssign(task: MeshPrepareTask) = lock.locked { tasks += task }
    operator fun minusAssign(task: MeshPrepareTask) = lock.locked { tasks -= task }

    fun interrupt(requeue: Boolean) = lock.acquired {
        for (task in tasks) {
            task.interrupt()
            if (requeue) {
                renderer.invalidate(task.section)
            }
        }
    }

    fun interruptIf(requeue: Boolean, predicate: (SectionPosition) -> Boolean) = lock.acquired {
        for (task in tasks) {
            if (!predicate.invoke(task.position)) continue

            task.interrupt()
            if (requeue) {
                renderer.invalidate(task.section)
            }
        }
    }

    fun interrupt(position: ChunkPosition) = interruptIf(false) { it.chunkPosition == position }
    fun interrupt(position: SectionPosition) = interruptIf(false) { it == position }
}
