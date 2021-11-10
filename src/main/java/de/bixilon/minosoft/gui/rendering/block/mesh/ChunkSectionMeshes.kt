/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.block.mesh

import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import glm_.vec3.Vec3i

class ChunkSectionMeshes(
    renderWindow: RenderWindow,
) {
    var opaqueMesh: ChunkSectionMesh? = ChunkSectionMesh(renderWindow)
        private set
    var translucentMesh: ChunkSectionMesh? = ChunkSectionMesh(renderWindow)
        private set
    var transparentMesh: ChunkSectionMesh? = ChunkSectionMesh(renderWindow)
        private set

    val minPosition = Vec3i.EMPTY
    val maxPosition = Vec3i.EMPTY

    lateinit var aabb: AABB
        private set

    @Synchronized
    fun load() {
        var mesh = this.opaqueMesh!!
        if (mesh.data.isEmpty) {
            this.opaqueMesh = null
        } else {
            mesh.load()
        }


        mesh = this.translucentMesh!!
        if (mesh.data.isEmpty) {
            this.translucentMesh = null
        } else {
            mesh.load()
        }

        mesh = this.transparentMesh!!
        if (mesh.data.isEmpty) {
            this.transparentMesh = null
        } else {
            mesh.load()
        }
    }
}
