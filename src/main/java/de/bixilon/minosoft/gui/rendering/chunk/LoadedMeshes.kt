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

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.kutil.concurrent.lock.LockUtil.acquired
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesh.details.ChunkMeshDetails
import de.bixilon.minosoft.gui.rendering.chunk.visible.VisibleMeshes
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class LoadedMeshes(
    private val renderer: ChunkRenderer,
) {
    private val meshes: MutableMap<ChunkPosition, Int2ObjectOpenHashMap<ChunkMeshes>> = HashMap(1000)
    private val lock = RWLock.rwlock()


    val size get() = meshes.size

    operator fun get(position: SectionPosition) = lock.acquired { meshes[position.chunkPosition]?.get(position.y) }
    operator fun plusAssign(mesh: ChunkMeshes) {
        lock.lock()
        val chunk = meshes.getOrPut(mesh.position.chunkPosition) { Int2ObjectOpenHashMap() }

        val previous = chunk.put(mesh.position.y, mesh)
        lock.unlock()
        val meshes = renderer.visibility.meshes

        if (previous != null) {
            meshes -= previous
            renderer.unloadingQueue += previous
        }

        meshes.lock.locked {
            meshes += mesh
            meshes.sort()
        }
    }

    operator fun minusAssign(position: SectionPosition) {
        val mesh = lock.locked {
            val meshes = this.meshes[position.chunkPosition] ?: return
            val mesh = meshes.remove(position.y) ?: return
            if (meshes.isEmpty()) {
                this.meshes -= position.chunkPosition
            }
            return@locked mesh
        }

        renderer.visibility.meshes -= mesh
        renderer.unloadingQueue += mesh
    }

    operator fun minusAssign(position: ChunkPosition) {
        val meshes = lock.locked { meshes.remove(position) } ?: return
        meshes.values.forEach { renderer.visibility.meshes -= it }
        renderer.unloadingQueue += meshes.values
    }

    fun clear() = lock.locked {
        renderer.visibility.meshes.clear()
        for (meshes in meshes.values) {
            renderer.unloadingQueue += meshes.values
        }
        meshes.clear()
    }

    fun addTo(visible: VisibleMeshes) = lock.acquired {
        // TODO: somehow cache the sorting

        val iterator = this.meshes.iterator()
        while (iterator.hasNext()) {
            val (chunkPosition, meshes) = iterator.next()

            if (chunkPosition !in renderer.visibility) continue

            val iterator = meshes.keys.intIterator()
            while (iterator.hasNext()) {
                val key = iterator.nextInt()
                val mesh = meshes[key] ?: continue

                // TODO: unload if occluded (but we can not distinct between frustum and occlusion)
                if (!renderer.visibility.contains(mesh.position, mesh.min, mesh.max)) {
                    continue
                }
                visible.unsafeAdd(mesh)
            }
        }
    }

    fun update() = lock.locked {
        val iterator = this.meshes.iterator()
        while (iterator.hasNext()) {
            val (chunkPosition, meshes) = iterator.next()

            if (!renderer.visibility.isInViewDistance(chunkPosition)) {
                iterator.remove()
                val values = meshes.values
                val chunk = values.iterator().next().section.chunk // dirty hack

                meshes.values.forEach { renderer.visibility.meshes -= it }
                renderer.culledQueue += chunk
                renderer.unloadingQueue += values
                continue
            }

            val sections = meshes.keys.intIterator()
            while (sections.hasNext()) {
                val key = sections.nextInt()
                val mesh = meshes[key] ?: continue

                if (!renderer.visibility.isInViewDistance(mesh.position)) {
                    sections.remove()
                    renderer.culledQueue += mesh.section
                    renderer.visibility.meshes -= mesh
                    renderer.unloadingQueue += mesh
                    continue
                }

                val next = ChunkMeshDetails.update(mesh.details, mesh.position, renderer.visibility.sectionPosition)

                if (next == mesh.details) continue

                renderer.meshingQueue += mesh.section
            }

            if (meshes.isEmpty()) {
                iterator.remove()
            }
        }
    }
}
