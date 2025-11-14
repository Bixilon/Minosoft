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

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.profiler.stack.StackedProfiler.Companion.invoke
import de.bixilon.kutil.time.TimeUtil.now
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.chunk.ChunkRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.queue.QueuePosition
import de.bixilon.minosoft.gui.rendering.chunk.util.ChunkRendererUtil.maxBusyTime
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class MeshLoadingQueue(
    private val renderer: ChunkRenderer,
) {
    private val meshes: MutableList<ChunkMeshes> = mutableListOf() // prepared meshes, that can be loaded in the (next) frame
    private val positions: MutableSet<QueuePosition> = HashSet()
    private val lock = Lock.lock()

    val size: Int get() = meshes.size


    fun work() {
        if (meshes.isEmpty()) return
        renderer.context.profiler("lock") { lock() }

        var count = 0
        val start = now()
        val maxTime = renderer.maxBusyTime

        var meshes: Int2ObjectOpenHashMap<ChunkMeshes> = unsafeNull()
        var position: ChunkPosition? = null

        renderer.context.profiler("lock") { renderer.loaded.lock() }
        var index = 0
        while (true) {
            if (this.meshes.isEmpty()) break
            if (index++ % BATCH_SIZE == 0 && now() - start >= maxTime) break

            val mesh = this.meshes.removeAt(0)
            this.positions -= QueuePosition(mesh)

            renderer.context.profiler("load$index") { mesh.load() }

            if (position != mesh.position.chunkPosition) {
                meshes = renderer.loaded.meshes.getOrPut(mesh.position.chunkPosition) { Int2ObjectOpenHashMap() }
                position = mesh.position.chunkPosition
            }


            meshes.put(mesh.position.y, mesh)?.let {
                renderer.visible -= it
                it.unload()
            }

            val visible = renderer.visibility.isSectionVisible(mesh.position, mesh.min, mesh.max)
            if (visible) {
                count++
                renderer.visible += mesh
            }
        }
        renderer.loaded.unlock()

        unlock()

        if (count > 0) {
            renderer.visible.sort()
        }
    }


    fun queue(mesh: ChunkMeshes) {
        lock()
        if (!this.positions.add(QueuePosition(mesh))) {
            // already inside, remove
            meshes.dropIf { it.position == mesh.position }
        }
        if (mesh.position.chunkPosition == renderer.cameraSectionPosition.chunkPosition) {
            // still higher priority
            meshes.add(0, mesh)
        } else {
            meshes += mesh
        }
        unlock()
    }

    fun abort(position: ChunkPosition, lock: Boolean = true) {
        if (lock) lock()
        val positions: MutableSet<QueuePosition> = mutableSetOf()
        this.positions.removeAll {
            if (it.position.chunkPosition != position) {
                return@removeAll false
            }
            positions += it
            return@removeAll true
        }
        this.meshes.removeAll { QueuePosition(it) in positions }
        if (lock) unlock()
    }

    fun abort(position: QueuePosition, lock: Boolean = true) {
        if (lock) lock()
        if (this.positions.remove(position)) {
            this.meshes.dropIf { it.position == position.position }
        }
        if (lock) unlock()
    }


    @Deprecated("cleanup????")
    fun cleanup(lock: Boolean) {
        val remove: MutableSet<QueuePosition> = mutableSetOf()

        if (lock) lock()
        this.positions.removeAll {
            if (renderer.visibility.isChunkVisible(it.position.chunkPosition)) {
                return@removeAll false
            }
            remove += it
            return@removeAll true
        }

        this.meshes.dropIf { QueuePosition(it) in remove }
        if (lock) unlock()
    }

    private inline fun MutableList<ChunkMeshes>.dropIf(crossinline predicate: (ChunkMeshes) -> Boolean) = removeIf {
        if (!predicate.invoke(it)) return@removeIf false
        it.drop()
        return@removeIf true
    }

    fun clear(lock: Boolean) {
        if (lock) lock()
        this.positions.clear()
        while (meshes.isNotEmpty()) {
            val mesh = meshes.removeAt(0)
            mesh.drop()
            mesh.cache.drop()
        }
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

    companion object {
        const val BATCH_SIZE = 5
    }
}
