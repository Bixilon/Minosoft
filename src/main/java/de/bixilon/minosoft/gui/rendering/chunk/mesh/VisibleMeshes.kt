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

package de.bixilon.minosoft.gui.rendering.chunk.mesh

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalTask
import de.bixilon.kutil.concurrent.worker.unconditional.UnconditionalWorker
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.util.KUtil.format

class VisibleMeshes(val cameraPosition: Vec3 = Vec3.EMPTY, previous: VisibleMeshes? = null) {
    val opaque: ArrayList<SingleWorldMesh> = ArrayList(previous?.opaque?.size ?: 128)
    val translucent: ArrayList<SingleWorldMesh> = ArrayList(previous?.translucent?.size ?: 16)
    val transparent: ArrayList<SingleWorldMesh> = ArrayList(previous?.transparent?.size ?: 128)
    val text: ArrayList<SingleWorldMesh> = ArrayList(previous?.text?.size ?: 16)
    val blockEntities: ArrayList<BlockEntityRenderer<*>> = ArrayList(previous?.blockEntities?.size ?: 128)

    val sizeString: String
        get() = "${opaque.size.format()}|${translucent.size.format()}|${transparent.size.format()}|${text.size.format()}|${blockEntities.size.format()}"


    fun addMesh(mesh: WorldMesh) {
        val distance = (cameraPosition - mesh.center).length2()
        mesh.opaqueMesh?.let {
            it.distance = distance
            opaque += it
        }
        mesh.translucentMesh?.let {
            it.distance = -distance
            translucent += it
        }
        mesh.transparentMesh?.let {
            it.distance = distance
            transparent += it
        }
        mesh.textMesh?.let {
            it.distance = distance
            text += it
        }
        mesh.blockEntities?.let {
            blockEntities += it
        }
    }


    fun sort() {
        val worker = UnconditionalWorker()
        worker += UnconditionalTask(ThreadPool.Priorities.HIGHER) { opaque.sort() }
        worker += UnconditionalTask(ThreadPool.Priorities.HIGHER) { translucent.sort() }
        worker += UnconditionalTask(ThreadPool.Priorities.HIGHER) { transparent.sort() }
        worker += UnconditionalTask(ThreadPool.Priorities.HIGHER) { text.sort() }
        worker.work()
    }


    fun removeMesh(mesh: WorldMesh) {
        mesh.opaqueMesh?.let { opaque -= it }
        mesh.translucentMesh?.let { translucent -= it }
        mesh.transparentMesh?.let { transparent -= it }
        mesh.textMesh?.let { text -= it }
        mesh.blockEntities?.let { blockEntities -= it }
    }

    fun clear() {
        opaque.clear()
        translucent.clear()
        transparent.clear()
        text.clear()
        blockEntities.clear()
    }
}
