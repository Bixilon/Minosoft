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

package de.bixilon.minosoft.gui.rendering.world.queue.loading

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import de.bixilon.minosoft.gui.rendering.world.queue.QueuePosition
import de.bixilon.minosoft.gui.rendering.world.util.WorldRendererUtil.maxBusyTime
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class MeshLoadingQueue(
    private val renderer: WorldRenderer,
) {
    private val meshes: MutableList<WorldMesh> = mutableListOf() // prepared meshes, that can be loaded in the (next) frame
    private val positions: MutableSet<QueuePosition> = HashSet()
    private val lock = SimpleLock()

    val size: Int get() = meshes.size


    fun work() {
        lock.lock()
        if (meshes.isEmpty()) {
            lock.unlock()
            return
        }

        var count = 0
        val start = TimeUtil.millis()
        val maxTime = renderer.maxBusyTime // If the player is still, then we can load more chunks (to not cause lags)

        var meshes: Int2ObjectOpenHashMap<WorldMesh> = unsafeNull()
        var position: ChunkPosition? = null

        renderer.loaded.lock()
        while (this.meshes.isNotEmpty() && (TimeUtil.millis() - start < maxTime)) {
            val mesh = this.meshes.removeAt(0)
            this.positions -= QueuePosition(mesh)

            mesh.load()

            if (position != mesh.chunkPosition) {
                meshes = renderer.loaded.meshes.getOrPut(mesh.chunkPosition) { Int2ObjectOpenHashMap() }
                position = mesh.chunkPosition
            }


            meshes.put(mesh.sectionHeight, mesh)?.let {
                renderer.visible.removeMesh(it)
                it.unload()
            }

            val visible = renderer.visibilityGraph.isSectionVisible(mesh.chunkPosition, mesh.sectionHeight, mesh.minPosition, mesh.maxPosition, true)
            if (visible) {
                count++
                renderer.visible.addMesh(mesh)
            }
        }
        renderer.loaded.unlock()

        lock.unlock()

        if (count > 0) {
            renderer.visible.sort()
        }
    }


    fun queue(mesh: WorldMesh) {
        lock.lock()
        if (!this.positions.add(QueuePosition(mesh))) {
            // already inside, remove
            meshes.remove(mesh)
        }
        if (mesh.chunkPosition == renderer.cameraChunkPosition) {
            // still higher priority
            meshes.add(0, mesh)
        } else {
            meshes += mesh
        }
        lock.unlock()
    }

    fun abort(position: ChunkPosition, lock: Boolean = true) {
        if (lock) this.lock.lock()
        val positions: MutableSet<QueuePosition> = mutableSetOf()
        this.positions.removeAll {
            if (it.position != position) {
                return@removeAll false
            }
            positions += it
            return@removeAll true
        }
        this.meshes.removeAll { QueuePosition(it.chunkPosition, it.sectionHeight) in positions }
        if (lock) this.lock.unlock()
    }


    fun cleanup(lock: Boolean) {
        val remove: MutableSet<QueuePosition> = mutableSetOf()

        if (lock) this.lock.lock()
        this.positions.removeAll {
            if (renderer.visibilityGraph.isChunkVisible(it.position)) {
                return@removeAll false
            }
            remove += it
            return@removeAll true
        }

        this.meshes.removeAll { QueuePosition(it) in remove }
        if (lock) this.lock.unlock()
    }

    fun clear(lock: Boolean) {
        if (lock) this.lock.lock()
        this.positions.clear()
        this.meshes.clear()
        if (lock) this.lock.unlock()
    }


    fun lock() {
        this.lock.lock()
    }

    fun unlock() {
        this.lock.unlock()
    }
}
