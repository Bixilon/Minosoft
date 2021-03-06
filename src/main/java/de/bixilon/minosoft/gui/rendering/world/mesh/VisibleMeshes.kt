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

package de.bixilon.minosoft.gui.rendering.world.mesh

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import de.bixilon.minosoft.util.KUtil.format

class VisibleMeshes(val cameraPosition: Vec3 = Vec3.EMPTY) {
    val opaque: MutableList<SingleWorldMesh> = mutableListOf()
    val translucent: MutableList<SingleWorldMesh> = mutableListOf()
    val transparent: MutableList<SingleWorldMesh> = mutableListOf()
    val text: MutableList<SingleWorldMesh> = mutableListOf()
    val blockEntities: MutableList<BlockEntityRenderer<*>> = mutableListOf()

    val sizeString: String
        get() = "${opaque.size.format()}|${translucent.size.format()}|${transparent.size.format()}|${text.size.format()}|${blockEntities.size.format()}"


    fun addMesh(mesh: WorldMesh) {
        val distance = (cameraPosition - mesh.center).length2()
        mesh.opaqueMesh?.let {
            it.distance = distance
            opaque += it
        }
        mesh.translucentMesh?.let {
            it.distance = distance
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
        opaque.sortBy { it.distance }
        translucent.sortBy { -it.distance }
        transparent.sortBy { it.distance }
        text.sortBy { it.distance }
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
