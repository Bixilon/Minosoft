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

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.kutil.concurrent.lock.LockUtil.acquired
import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.camera.frustum.FrustumResults
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesh.details.ChunkMeshDetails
import de.bixilon.minosoft.gui.rendering.chunk.queue.meshing.ChunkMeshingCause
import de.bixilon.minosoft.gui.rendering.chunk.visible.VisibleMeshes
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStates
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

class LoadedMeshes(
    private val renderer: ChunkRenderer,
) {
    private val meshes: MutableMap<ChunkPosition, Int2ObjectOpenHashMap<ChunkMeshes>> = HashMap(1000)
    private val lock = RWLock.rwlock()


    val size get() = meshes.size

    operator fun get(position: SectionPosition) = lock.acquired { meshes[position.chunkPosition]?.get(position.y) }
    operator fun plusAssign(mesh: ChunkMeshes) {
        assert(mesh.state == MeshStates.LOADED)
        lock.lock()
        val chunk = meshes.getOrPut(mesh.position.chunkPosition) { Int2ObjectOpenHashMap() }

        val previous = chunk.put(mesh.position.y, mesh)
        lock.unlock()
        val meshes = renderer.visibility.meshes

        if (previous != null) {
            meshes -= previous
            renderer.unloadingQueue += previous
        }


        val frustum = renderer.visibility.contains(mesh.position, mesh.min, mesh.max)
        if (frustum == FrustumResults.OUTSIDE) {
            return
        }

        meshes.lock.locked {
            meshes.unsafeAdd(mesh, frustum, true)
            meshes.sort() // TODO: replace mesh (no need to sort again)
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
        renderer.cache -= position
    }

    operator fun minusAssign(position: ChunkPosition) {
        val meshes = lock.locked { meshes.remove(position) } ?: return
        meshes.values.forEach { renderer.visibility.meshes -= it }

        renderer.unloadingQueue += meshes.values
        renderer.cache -= position
    }

    fun clear() = lock.locked {
        renderer.visibility.meshes.clear()
        for (meshes in meshes.values) {
            renderer.unloadingQueue += meshes.values
        }
        meshes.clear()
    }

    fun addTo(visible: VisibleMeshes, force: Boolean) = lock.acquired {
        // TODO: somehow cache the sorting

        val frustum = renderer.context.camera.frustum

        val iterator = this.meshes.iterator()
        while (iterator.hasNext()) {
            val (position, meshes) = iterator.next()

            if (position !in frustum) continue

            for (mesh in meshes.values) {
                // TODO: unload if occluded (but we can not distinct between frustum and occlusion)
                val result = renderer.visibility.contains(mesh.position, mesh.min, mesh.max)
                if (result == FrustumResults.OUTSIDE) continue

                visible.unsafeAdd(mesh, result, force)
            }
        }
    }

    fun update() = lock.locked {
        renderer.meshingQueue.lock.lock()

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
                renderer.cache -= chunkPosition
                continue
            }

            val sections = meshes.values.iterator()
            while (sections.hasNext()) {
                val mesh = sections.next() ?: continue

                if (!renderer.visibility.isInViewDistance(mesh.position)) {
                    sections.remove()
                    renderer.culledQueue += mesh.section
                    renderer.visibility.meshes -= mesh
                    renderer.unloadingQueue += mesh
                    renderer.cache -= chunkPosition
                    continue
                }

                val next = ChunkMeshDetails.update(mesh.details, mesh.position, renderer.visibility.sectionPosition) + renderer.mesher.details

                if (next == mesh.details) continue

                renderer.meshingQueue.unsafeAdd(mesh.section, ChunkMeshingCause.LEVEL_OF_DETAIL_UPDATE)
            }

            if (meshes.isEmpty()) {
                iterator.remove()
            }
        }
        renderer.meshingQueue.sort()
        renderer.meshingQueue.lock.unlock()
    }
}
