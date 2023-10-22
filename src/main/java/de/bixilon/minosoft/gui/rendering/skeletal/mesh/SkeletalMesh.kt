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

package de.bixilon.minosoft.gui.rendering.skeletal.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct

class SkeletalMesh(context: RenderContext, initialCacheSize: Int) : Mesh(context, SkeletalMeshStruct, initialCacheSize = initialCacheSize), SkeletalConsumer {
    override val order = context.system.quadOrder

    private fun addVertex(position: FaceVertexData, positionOffset: Int, uv: FaceVertexData, uvOffset: Int, transform: Float, normal: Float, textureShaderId: Float) {
        data.add(
            position[positionOffset + 0], position[positionOffset + 1], position[positionOffset + 2],
            uv[uvOffset + 0], uv[uvOffset + 1],
            transform, normal,
        )
        data.add(textureShaderId)
    }

    private fun encodePart(part: Float): Int {
        val unsigned = (part + 1.0f) / 2.0f // remove negative sign
        return (unsigned * 15.0f).toInt() and 0x0F
    }

    private fun encodeNormal(normal: Vec3): Int {
        val x = encodePart(normal.x)
        val y = encodePart(normal.y)
        val z = encodePart(normal.z)

        return (y shl 8) or (z shl 4) or (x)
    }

    override fun addQuad(positions: FaceVertexData, uv: FaceVertexData, transform: Int, normal: Vec3, texture: ShaderTexture) {
        val transform = transform.buffer()
        val textureShaderId = texture.shaderId.buffer()
        val normal = encodeNormal(normal).buffer()

        order.iterate { position, uvIndex ->
            addVertex(positions, position * Vec3.length, uv, uvIndex * Vec2.length, transform, normal, textureShaderId)
        }
    }


    data class SkeletalMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val transform: Int,
        val normal: Int,
        val indexLayerAnimation: Int,
    ) {
        companion object : MeshStruct(SkeletalMeshStruct::class)
    }
}
