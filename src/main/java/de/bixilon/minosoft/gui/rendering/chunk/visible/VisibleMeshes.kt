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

package de.bixilon.minosoft.gui.rendering.chunk.visible

import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalTask
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.gui.rendering.RenderingThreadPool
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMesh
import de.bixilon.minosoft.gui.rendering.chunk.mesh.ChunkMeshes
import de.bixilon.minosoft.util.KUtil.format
import kotlin.math.abs

class VisibleMeshes(
    val camera: BlockPosition = BlockPosition.EMPTY,
    previous: VisibleMeshes? = null,
) {
    val opaque: ArrayList<ChunkMesh> = ArrayList(previous?.opaque?.size ?: 128)
    val translucent: ArrayList<ChunkMesh> = ArrayList(previous?.translucent?.size ?: 16)
    val text: ArrayList<ChunkMesh> = ArrayList(previous?.text?.size ?: 16)
    val entities: ArrayList<BlockEntityRenderer> = ArrayList(previous?.entities?.size ?: 128)

    val sizeString: String
        get() = "${opaque.size.format()}|${translucent.size.format()}|${text.size.format()}|${entities.size.format()}"


    fun add(mesh: ChunkMeshes) {
        val delta = (camera - mesh.center)
        val distance = abs(delta.x) * abs(delta.y) * abs(delta.z)
        mesh.opaque?.let {
            it.distance = distance
            opaque += it
        }
        mesh.translucent?.let {
            it.distance = -distance
            translucent += it
        }
        mesh.text?.let {
            it.distance = distance
            text += it
        }
        mesh.entities?.let {
            entities += it
        }
    }

    operator fun plusAssign(mesh: ChunkMeshes) = add(mesh)


    fun sort() {
        val worker = UnconditionalWorker(pool = RenderingThreadPool)
        worker += UnconditionalTask(ThreadPool.Priorities.HIGHER) { opaque.sort() }
        worker += UnconditionalTask(ThreadPool.Priorities.HIGHER) { translucent.sort() }
        worker += UnconditionalTask(ThreadPool.Priorities.HIGHER) { text.sort() }
        // TODO: sort entities
        worker.work()
    }


    fun remove(mesh: ChunkMeshes) {
        mesh.opaque?.let { opaque -= it }
        mesh.translucent?.let { translucent -= it }
        mesh.text?.let { text -= it }
        mesh.entities?.let { entities -= it }
    }

    operator fun minusAssign(mesh: ChunkMeshes) = remove(mesh)


    fun clear() {
        opaque.clear()
        translucent.clear()
        text.clear()
        entities.clear()
    }
}
