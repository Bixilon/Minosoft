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

package de.bixilon.minosoft.gui.rendering.chunk.queue.loading

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesh.cache.ChunkMeshCache
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererUtil.maxBusyTime

class MeshUnloadingQueue(
    private val renderer: ChunkRenderer,
) {
    private val meshes = ArrayDeque<ChunkMeshes>()
    private val caches = ArrayDeque<ChunkMeshCache>()
    private val lock = Lock.lock()

    var updates = 0
        get() {
            val value = field
            field = 0
            return value
        }

    private inline fun <E> work(queue: ArrayDeque<E>, unloader: (E) -> Unit) {
        if (queue.isEmpty()) return

        val start = now()
        val maxTime = renderer.maxBusyTime

        var index = 0
        while (queue.isNotEmpty()) {
            if (index++ % MeshLoadingQueue.BATCH_SIZE == 0 && now() - start >= maxTime) break

            val entry = queue.removeFirst()
            unloader.invoke(entry)
            updates++
        }
    }

    fun work() = lock.locked {
        work(this.meshes, ChunkMeshes::unload)
        work(this.caches, ChunkMeshCache::unload)
    }


    operator fun plusAssign(cache: ChunkMeshCache) = lock.locked { caches += cache }

    @JvmName("plusAssignCache")
    operator fun plusAssign(caches: Collection<ChunkMeshCache>) = lock.locked { this.caches += caches }

    operator fun plusAssign(mesh: ChunkMeshes) = lock.locked { meshes += mesh }

    @JvmName("plusAssignMesh")
    operator fun plusAssign(meshes: Collection<ChunkMeshes>) = lock.locked { this.meshes += meshes }
}
