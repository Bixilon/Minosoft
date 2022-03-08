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

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.world.entities.BlockEntityRenderer
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3i

class WorldMesh(
    renderWindow: RenderWindow,
    val chunkPosition: Vec2i,
    val sectionHeight: Int,
    smallMesh: Boolean = false,
) {
    val center: Vec3 = Vec3(Vec3i.of(chunkPosition, sectionHeight, Vec3i(8, 8, 8)))
    var opaqueMesh: SingleWorldMesh? = SingleWorldMesh(renderWindow, if (smallMesh) 1000 else 100000)
    var translucentMesh: SingleWorldMesh? = SingleWorldMesh(renderWindow, if (smallMesh) 1000 else 10000)
    var transparentMesh: SingleWorldMesh? = SingleWorldMesh(renderWindow, if (smallMesh) 1000 else 20000)
    var blockEntities: Set<BlockEntityRenderer<*>>? = null

    // used for frustum culling
    val minPosition = Vec3i(16)
    val maxPosition = Vec3i(0)

    @Synchronized
    fun load() {
        this.opaqueMesh?.load()
        this.translucentMesh?.load()
        this.transparentMesh?.load()
        val blockEntities = this.blockEntities
        if (blockEntities != null) {
            for (blockEntity in blockEntities) {
                blockEntity.load()
            }
        }
    }

    @Synchronized
    fun clearEmpty(): Int {
        var meshes = 0
        opaqueMesh?.let {
            if (it.data.isEmpty) {
                it.data.unload()
                opaqueMesh = null
            } else {
                meshes++
            }
        }
        translucentMesh?.let {
            if (it.data.isEmpty) {
                it.data.unload()
                translucentMesh = null
            } else {
                meshes++
            }
        }
        transparentMesh?.let {
            if (it.data.isEmpty) {
                it.data.unload()
                transparentMesh = null
            } else {
                meshes++
            }
        }
        blockEntities?.let {
            if (it.isEmpty()) {
                blockEntities = null
            } else {
                meshes += it.size
            }
        }
        return meshes
    }

    @Synchronized
    fun unload() {
        opaqueMesh?.unload()
        translucentMesh?.unload()
        transparentMesh?.unload()

        val blockEntities = blockEntities
        if (blockEntities != null) {
            for (blockEntity in blockEntities) {
                blockEntity.unload()
            }
        }
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
