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

package de.bixilon.minosoft.gui.rendering.chunk.queue.loading

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererUtil.maxBusyTime

class MeshUnloadingQueue(
    private val renderer: ChunkRenderer,
) {
    private val meshes = ArrayDeque<ChunkMeshes>()
    private val lock = Lock.lock()


    fun work() {
        if (meshes.isEmpty()) return

        val start = now()
        val maxTime = renderer.maxBusyTime

        lock.lock()

        var index = 0
        while (meshes.isNotEmpty()) {
            if (index++ % MeshLoadingQueue.BATCH_SIZE == 0 && now() - start >= maxTime) break

            val mesh = meshes.removeFirst()
            mesh.unload()
            // TODO: Who unloads the cache???
        }

        lock.unlock()
    }


    operator fun plusAssign(mesh: ChunkMeshes) = lock.locked { meshes += mesh }

    operator fun plusAssign(meshes: Collection<ChunkMeshes>) = lock.locked { this.meshes += meshes }
}
