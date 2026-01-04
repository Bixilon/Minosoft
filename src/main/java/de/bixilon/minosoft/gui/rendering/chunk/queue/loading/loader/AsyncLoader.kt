/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk.queue.loading.loader

import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantLock
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.queue.loading.MeshLoadingQueue
import de.bixilon.minosoft.gui.rendering.system.base.buffer.AsyncBufferLoader
import de.bixilon.minosoft.gui.rendering.system.base.buffer.GpuBufferStates

class AsyncLoader(
    val queue: MeshLoadingQueue,
    val loader: AsyncBufferLoader,
) : AbstractMeshLoader {
    private val lock = ReentrantLock()
    private var done: ArrayDeque<ChunkMeshes> = ArrayDeque()

    private fun callback(mesh: ChunkMeshes) {
        if (mesh.opaque != null && mesh.opaque.buffer.state != GpuBufferStates.INITIALIZED) return
        if (mesh.translucent != null && mesh.translucent.buffer.state != GpuBufferStates.INITIALIZED) return
        if (mesh.text != null && mesh.text.buffer.state != GpuBufferStates.INITIALIZED) return

        lock.locked { done += mesh }
    }

    private fun enqueue() {
        val mesh = queue.take() ?: return
        mesh.opaque?.let { loader.load(it.buffer) { callback(mesh) } }
        mesh.translucent?.let { loader.load(it.buffer) { callback(mesh) } }
        mesh.text?.let { loader.load(it.buffer) { callback(mesh) } }
    }


    override fun work() {
        enqueue()
        if (done.isEmpty()) return

        lock.locked {
            while (done.isNotEmpty()) {
                val mesh = done.removeFirst()
                mesh.load()

                queue.renderer.loaded += mesh
            }
        }
    }

    override fun queue() {
        enqueue()
    }
}
