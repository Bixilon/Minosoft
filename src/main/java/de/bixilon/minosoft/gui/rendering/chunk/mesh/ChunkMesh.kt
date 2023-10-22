/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList

class ChunkMesh(
    context: RenderContext,
    val chunkPosition: Vec2i,
    val sectionHeight: Int,
    smallMesh: Boolean = false,
) {
    val center: Vec3 = Vec3(Vec3i.of(chunkPosition, sectionHeight, Vec3i(8, 8, 8)))
    var opaqueMesh: SingleChunkMesh? = SingleChunkMesh(context, if (smallMesh) 3000 else 100000)
    var translucentMesh: SingleChunkMesh? = SingleChunkMesh(context, if (smallMesh) 3000 else 10000, onDemand = true)
    var transparentMesh: SingleChunkMesh? = SingleChunkMesh(context, if (smallMesh) 3000 else 20000, onDemand = true)
    var textMesh: SingleChunkMesh? = SingleChunkMesh(context, if (smallMesh) 5000 else 50000, onDemand = true)
    var blockEntities: ArrayList<BlockEntityRenderer<*>>? = null

    // used for frustum culling
    val minPosition = Vec3i(16)
    val maxPosition = Vec3i(0)

    fun finish() {
        this.opaqueMesh?.finish()
        this.translucentMesh?.finish()
        this.transparentMesh?.finish()
        this.textMesh?.finish()
    }

    @Synchronized
    fun load() {
        this.opaqueMesh?.load()
        this.translucentMesh?.load()
        this.transparentMesh?.load()
        this.textMesh?.load()
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

        fun processMesh(mesh: SingleChunkMesh?): Boolean {
            if (mesh == null) {
                return false
            }
            val data = mesh.data
            if (data.isEmpty) {
                if (data is DirectArrayFloatList) {
                    data.unload()
                }
                return true
            }
            meshes++
            return false
        }

        if (processMesh(opaqueMesh)) opaqueMesh = null
        if (processMesh(translucentMesh)) translucentMesh = null
        if (processMesh(transparentMesh)) transparentMesh = null

        if (processMesh(textMesh)) textMesh = null

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
        textMesh?.unload()

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
