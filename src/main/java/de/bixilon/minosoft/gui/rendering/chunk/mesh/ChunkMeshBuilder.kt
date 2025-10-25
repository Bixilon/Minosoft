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
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.MeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.PackedUV

class ChunkMeshBuilder(context: RenderContext, initialCacheSize: Int) : MeshBuilder(context, ChunkMeshStruct, initialCacheSize = initialCacheSize), BlockVertexConsumer {
    override val order = context.system.quadOrder

    override fun addVertex(position: Vec3f, uv: Vec2f, texture: ShaderTexture, tintColor: RGBColor, lightIndex: Int) {
        data.ensureSize(ChunkMeshStruct.floats)
        val transformedUV = texture.transformUV(uv)
        data.add(position.x, position.y, position.z)
        data.add(PackedUV.pack(transformedUV.x, transformedUV.y))
        data.add(
            texture.shaderId.buffer(),
            (((lightIndex shl 24) or tintColor.rgb).buffer())
        )
    }

    override inline fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, textureId: Float, lightTint: Float) {
        data.add(
            x, y, z,
            PackedUV.pack(u, v),
            textureId, lightTint,
        )
    }

    override inline fun addVertex(x: Float, y: Float, z: Float, uv: Float, textureId: Float, lightTint: Float) {
        data.add(
            x, y, z,
            uv,
            textureId, lightTint,
        )
    }

    override fun bake() = ChunkMesh(create())

    data class ChunkMeshStruct(
        val position: Vec3f,
        val uv: PackedUV,
        val indexLayerAnimation: Int,
        val lightTint: Int,
    ) {
        companion object : MeshStruct(ChunkMeshStruct::class)
    }
}
