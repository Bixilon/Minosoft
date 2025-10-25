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

package de.bixilon.minosoft.gui.rendering.entities.feature.block

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.chunk.mesh.BlockVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.MeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

class BlockMeshBuilder(context: RenderContext) : MeshBuilder(context, BlockMeshStruct), BlockVertexConsumer {
    override val order = context.system.quadOrder
    val offset = MVec3f()

    override fun ensureSize(size: Int) {
        data.ensureSize(size)
    }

    override fun addVertex(position: Vec3f, uv: Vec2f, texture: ShaderTexture, tintColor: RGBColor, lightIndex: Int) {
        data.ensureSize(BlockMeshStruct.floats)
        val transformedUV = texture.transformUV(uv)
        data.add(position.x + offset.x, position.y + offset.y, position.z + offset.z)
        data.add(transformedUV.x, transformedUV.y)
        data.add(
            texture.shaderId.buffer(),
            (((lightIndex shl 24) or tintColor.rgb).buffer())
        )
    }

    override inline fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, textureId: Float, lightTint: Float) {
        data.add(
            x + offset.x, y + offset.y, z + offset.z,
            u, v,
            textureId, lightTint,
        )
    }
    override inline fun addVertex(x: Float, y: Float, z: Float, uv: Float, textureId: Float, lightTint: Float) {
        data.add(
            x + offset.x, y + offset.y, z + offset.z,
            UnpackedUV.unpackU(uv), UnpackedUV.unpackV(uv),
            textureId, lightTint,
        )
    }


    data class BlockMeshStruct(
        val position: Vec3f,
        val uv: UnpackedUV,
        val indexLayerAnimation: Int,
        val tint: RGBColor,
    ) {
        companion object : MeshStruct(BlockMeshStruct::class)
    }
}
