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

package de.bixilon.minosoft.gui.rendering.skeletal.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.block.element.FaceVertexData
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

class SkeletalMesh(context: RenderContext, initialCacheSize: Int = 1000) : AbstractSkeletalMesh(context, SkeletalMeshStruct, initialCacheSize = initialCacheSize) {

    private fun addVertex(position: FaceVertexData, positionOffset: Int, uv: UnpackedUV, uvOffset: Int, transformNormal: Float, textureShaderId: Float) {
        data.add(
            position[positionOffset + 0], position[positionOffset + 1], position[positionOffset + 2],
            uv.raw[uvOffset + 0], uv.raw[uvOffset + 1],
            transformNormal,
            textureShaderId,
        )
    }

    override fun addQuad(positions: FaceVertexData, uv: UnpackedUV, transform: Int, normal: Vec3, texture: ShaderTexture, path: String) {
        val transformNormal = ((transform shl 12) or SkeletalMeshUtil.encodeNormal(normal)).buffer()
        val textureShaderId = texture.shaderId.buffer()

        order.iterate { position, uvIndex ->
            addVertex(positions, position * Vec3.length, uv, uvIndex * Vec2.length, transformNormal, textureShaderId)
        }
    }


    data class SkeletalMeshStruct(
        val position: Vec3,
        val uv: UnpackedUV,
        val transformNormal: Int,
        val indexLayerAnimation: Int,
    ) {
        companion object : MeshStruct(SkeletalMeshStruct::class)
    }

    companion object : SkeletalMeshBuilder {
        override fun buildMesh(context: RenderContext) = SkeletalMesh(context)
    }
}
