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

package de.bixilon.minosoft.gui.rendering.skeletal

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct

class SkeletalMesh(renderWindow: RenderWindow, initialCacheSize: Int) : Mesh(renderWindow, SkeletalMeshStruct, initialCacheSize = initialCacheSize), SkeletalVertexConsumer {

    override fun addVertex(position: FloatArray, uv: Vec2, transform: Int, texture: ShaderTexture, flags: Int) {
        val transformedUV = texture.transformUV(uv)
        data.add(position[0])
        data.add(position[1])
        data.add(position[2])
        data.add(transformedUV.x)
        data.add(transformedUV.y)
        data.add(transform.buffer())
        data.add(texture.shaderId.buffer())
        data.add(flags.buffer())
    }

    @Deprecated("Pretty rendering specific")
    override fun addVertex(position: FloatArray, transformedUV: Vec2, transform: Float, textureShaderId: Float, flags: Float) {
        data.add(position[0])
        data.add(position[1])
        data.add(position[2])
        data.add(transformedUV.x)
        data.add(transformedUV.y)
        data.add(transform)
        data.add(textureShaderId)
        data.add(flags)
    }


    data class SkeletalMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val transform: Int,
        val indexLayerAnimation: Int,
        val flags: Int,
    ) {
        companion object : MeshStruct(SkeletalMeshStruct::class)
    }
}
