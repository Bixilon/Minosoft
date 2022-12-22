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

package de.bixilon.minosoft.gui.rendering.world

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.gui.rendering.world.mesh.VisibleMeshes
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class LoadedMeshes(
    private val renderer: WorldRenderer,
) {
    val meshes: MutableMap<Vec2i, Int2ObjectOpenHashMap<WorldMesh>> = mutableMapOf() // all prepared (and up to date) meshes
    private val lock = SimpleLock()


    val size: Int get() = meshes.size


    fun cleanup(lock: Boolean) {
        if (lock) this.lock.lock()

        val iterator = meshes.iterator()
        for ((chunkPosition, sections) in iterator) {
            if (renderer.visibilityGraph.isInViewDistance(chunkPosition)) {
                continue
            }
            iterator.remove()
            renderer.unloadingQueue.forceQueue(sections.values)
        }

        if (lock) this.lock.unlock()
    }

    fun clear(lock: Boolean) {
        if (lock) this.lock.lock()

        for (sections in meshes.values) {
            renderer.unloadingQueue.forceQueue(sections.values, lock)
        }

        if (lock) this.lock.unlock()
    }

    fun unload(position: ChunkPosition, lock: Boolean) {
        if (lock) this.lock.lock()

        val meshes = this.meshes.remove(position)

        if (meshes != null) {
            renderer.unloadingQueue.forceQueue(meshes.values, lock)
        }

        if (lock) this.lock.unlock()
    }

    fun unload(position: ChunkPosition, sectionHeight: Int, lock: Boolean) {
        if (lock) this.lock.lock()

        val meshes = this.meshes[position]

        if (meshes != null) {
            meshes.remove(sectionHeight)?.let {
                renderer.unloadingQueue.forceQueue(it, lock)

                if (meshes.isEmpty()) {
                    this.meshes.remove(position)
                }
            }
        }

        if (lock) this.lock.unlock()
    }


    operator fun contains(position: ChunkPosition): Boolean {
        lock.acquire()
        val contains = position in this.meshes
        lock.release()
        return contains
    }


    fun collect(visible: VisibleMeshes) {
        lock.acquire()
        for ((chunkPosition, meshes) in this.meshes) {
            if (!renderer.visibilityGraph.isChunkVisible(chunkPosition)) {
                continue
            }

            for (entry in meshes.int2ObjectEntrySet()) {
                val mesh = entry.value
                if (!renderer.visibilityGraph.isSectionVisible(chunkPosition, entry.intKey, mesh.minPosition, mesh.maxPosition, false)) {
                    continue
                }
                visible.addMesh(mesh)
            }
        }
        lock.release()
    }

    fun lock() {
        this.lock.lock()
    }

    fun unlock() {
        this.lock.unlock()
    }
}
