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

package de.bixilon.minosoft.gui.rendering.particle

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.util.collections.floats.AbstractFloatList

class ParticleMesh(context: RenderContext, data: AbstractFloatList) : Mesh(context, ParticleMeshStruct, PrimitiveTypes.POINT, -1, clearOnLoad = false, data = data) {

    fun addVertex(position: Vec3d, scale: Float, texture: AbstractTexture, tintColor: RGBColor, uvMin: FloatArray? = null, uvMax: FloatArray? = null, light: Int) {
        val minTransformedUV = if (uvMin == null) {
            EMPTY_UV_ARRAY
        } else {
            texture.renderData.transformUV(uvMin)
        }
        val maxTransformedUV = texture.renderData.transformUV(uvMax)
        val data = data
        data.add(position.x.toFloat())
        data.add(position.y.toFloat())
        data.add(position.z.toFloat())
        data.add(minTransformedUV)
        data.add(maxTransformedUV)
        data.add(texture.renderData.shaderTextureId.buffer())
        data.add(scale)
        data.add(tintColor.rgba.buffer())
        data.add(light.buffer())
    }


    companion object {
        private val EMPTY_UV_ARRAY = floatArrayOf(0.0f, 0.0f)
    }

    data class ParticleMeshStruct(
        val position: Vec3,
        val minUVCoordinates: Vec2,
        val maxUVCoordinates: Vec2,
        val indexLayerAnimation: Int,
        val scale: Float,
        val tintColor: RGBColor,
        val light: Int,
    ) {
        companion object : MeshStruct(ParticleMeshStruct::class)
    }
}
