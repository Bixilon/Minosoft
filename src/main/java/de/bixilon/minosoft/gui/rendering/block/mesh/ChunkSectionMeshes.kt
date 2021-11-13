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

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import glm_.vec2.Vec2i
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

class ChunkSectionMeshes(
    renderWindow: RenderWindow,
    chunkPosition: Vec2i,
    sectionHeight: Int,
) {
    private val centerLength = Vec3d(Vec3i.of(chunkPosition, sectionHeight, Vec3i(8, 8, 8))).length2()
    var opaqueMesh: ChunkSectionMesh? = ChunkSectionMesh(renderWindow, 150000, centerLength)
        private set
    var translucentMesh: ChunkSectionMesh? = ChunkSectionMesh(renderWindow, 50000, centerLength)
        private set
    var transparentMesh: ChunkSectionMesh? = ChunkSectionMesh(renderWindow, 50000, centerLength)
        private set

    // used for frustum culling
    val minPosition = Vec3i(16)
    val maxPosition = Vec3i(0)

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
        maxPosition += 1
    }

    @Synchronized
    fun unload() {
        opaqueMesh?.unload()
        translucentMesh?.unload()
        transparentMesh?.unload()
    }

    fun addBlock(x: Int, y: Int, z: Int) {
        if (x < minPosition.x) {
            minPosition.x = x
        }
        if (y < minPosition.y) {
            minPosition.y = y
        }
        if (z < minPosition.z) {
            minPosition.z = z
        }

        if (x > maxPosition.x) {
            maxPosition.x = x
        }
        if (y > maxPosition.y) {
            maxPosition.y = y
        }
        if (z > maxPosition.z) {
            maxPosition.z = z
        }
    }
}
