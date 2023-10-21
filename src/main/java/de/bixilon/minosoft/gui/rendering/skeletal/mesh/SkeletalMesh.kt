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

    private fun addVertex(position: FloatArray, offset: Int, transformedUV: Vec2, transform: Float, textureShaderId: Float) {
        data.add(
            position[offset + 0], position[offset + 1], position[offset + 2],
        )
        data.add(transformedUV.array)
        data.add(transform, textureShaderId)
    }

    override fun addQuad(positions: FaceVertexData, uv: Array<Vec2>, transform: Int, texture: ShaderTexture) {
        val transform = transform.buffer()
        val textureShaderId = texture.shaderId.buffer()

        order.iterate { position, uvIndex ->
            val transformedUV = texture.transformUV(uv[uvIndex])
            addVertex(positions, position * Vec3.length, transformedUV, transform, textureShaderId)
        }
    }


    data class SkeletalMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val transform: Int,
        val indexLayerAnimation: Int,
    ) {
        companion object : MeshStruct(SkeletalMeshStruct::class)
    }
}
