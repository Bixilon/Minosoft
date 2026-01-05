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

package de.bixilon.minosoft.gui.rendering.chunk.visible

import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantLock
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalTask
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderingThreadPool
import de.bixilon.minosoft.gui.rendering.camera.frustum.FrustumResults
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.gui.rendering.chunk.mesh.types.ChunkMeshTypes
import de.bixilon.minosoft.util.KUtil.format

class VisibleMeshes(
    val visibility: ChunkVisibilityManager,
    val camera: BlockPosition = BlockPosition.EMPTY,
    previous: VisibleMeshes? = null,
) {
    val meshes: Array<ArrayList<ChunkMesh>> = arrayOf(
        ArrayList(previous?.meshes?.get(ChunkMeshTypes.OPAQUE.ordinal)?.size ?: 128),
        ArrayList(previous?.meshes?.get(ChunkMeshTypes.TRANSLUCENT.ordinal)?.size ?: 16),
        ArrayList(previous?.meshes?.get(ChunkMeshTypes.TEXT.ordinal)?.size ?: 16),
    )
    val entities: ArrayList<BlockEntityRenderer> = ArrayList(previous?.entities?.size ?: 128)

    val sizeString: String
        get() = "${meshes.joinToString("|") { it.size.format().legacy }}|${entities.size.format()}"

    val lock = ReentrantLock()


    private fun add(mesh: ChunkMeshes, frustum: FrustumResults) {
        mesh.update(camera, frustum)

        mesh.meshes.forEach { type, mesh ->
            if (mesh.occlusion == ChunkMesh.OcclusionStates.INVISIBLE) return@forEach

            this.meshes[type.ordinal] += mesh
        }

        mesh.entities?.let { entities += it }
    }

    fun unsafeAdd(mesh: ChunkMeshes, frustum: FrustumResults) = add(mesh, frustum)


    fun sort() {
        val worker = UnconditionalWorker(pool = RenderingThreadPool)
        lock.locked {
            for (mesh in meshes) {
                worker += UnconditionalTask(ThreadPool.Priorities.HIGHER) { mesh.sort() }
            }
            // TODO: sort entities
            worker.work()
        }
    }

    operator fun minusAssign(mesh: ChunkMeshes): Unit = lock.locked {
        mesh.meshes.forEach { type, mesh ->
            this.meshes[type.ordinal] -= mesh
        }
        mesh.entities?.let { entities -= it }

        visibility.invalidate(VisibilityGraphInvalidReason.MESH_UPDATE)
    }

    fun clear() {
        this.meshes.forEach { it.clear() }
        entities.clear()

        visibility.invalidate(VisibilityGraphInvalidReason.MESH_UPDATE)
    }
}
