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

package de.bixilon.minosoft.gui.rendering.chunk.mesh

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.entities.BlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList

class ChunkMeshes(
    context: RenderContext,
    val position: SectionPosition,
    smallMesh: Boolean = false,
) : BlockVertexConsumer { // TODO: Don't inherit
    val center: Vec3f = Vec3f(BlockPosition.of(position, InSectionPosition(8, 8, 8)))
    var opaqueMesh: ChunkMesh? = ChunkMesh(context, if (smallMesh) 8192 else 65536)
    var translucentMesh: ChunkMesh? = ChunkMesh(context, if (smallMesh) 4096 else 16384)
    var textMesh: ChunkMesh? = ChunkMesh(context, if (smallMesh) 1024 else 4096)
    var blockEntities: ArrayList<BlockEntityRenderer<*>>? = null

    // used for frustum culling
    var minPosition = InSectionPosition(ChunkSize.SECTION_MAX_X, ChunkSize.SECTION_MAX_Y, ChunkSize.SECTION_MAX_Z)
    var maxPosition = InSectionPosition(0, 0, 0)

    fun finish() {
        this.opaqueMesh?.preload()
        this.translucentMesh?.preload()
        this.textMesh?.preload()
    }

    fun load() {
        this.opaqueMesh?.load()
        this.translucentMesh?.load()
        this.textMesh?.load()
        val blockEntities = this.blockEntities
        if (blockEntities != null) {
            for (blockEntity in blockEntities) {
                blockEntity.load()
            }
        }
    }

    fun clearEmpty(): Int {
        var meshes = 0

        fun processMesh(mesh: ChunkMesh?): Boolean {
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
            minPosition = minPosition.with(x = x)
        }
        if (y < minPosition.y) {
            minPosition = minPosition.with(y = y)
        }
        if (z < minPosition.z) {
            minPosition = minPosition.with(z = z)
        }

        if (x > maxPosition.x) {
            maxPosition = maxPosition.with(x = x)
        }
        if (y > maxPosition.y) {
            maxPosition = maxPosition.with(y = y)
        }
        if (z > maxPosition.z) {
            maxPosition = maxPosition.with(z = z)
        }
    }

    override val order get() = Broken()
    override fun ensureSize(floats: Int) = Unit
    override fun addVertex(position: Vec3f, uv: Vec2f, texture: ShaderTexture, tintColor: RGBColor, lightIndex: Int) = Broken()
    override fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, textureId: Float, lightTint: Float) = Broken()
    override fun addVertex(x: Float, y: Float, z: Float, uv: Float, textureId: Float, lightTint: Float) = Broken()

    override fun get(transparency: TextureTransparencies) = when (transparency) {
        TextureTransparencies.TRANSLUCENT -> translucentMesh
        else -> opaqueMesh
    }!!
}
