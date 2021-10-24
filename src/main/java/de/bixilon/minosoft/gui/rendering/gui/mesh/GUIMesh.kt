/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2
import glm_.vec2.Vec2t
import glm_.vec3.Vec3
import glm_.vec4.Vec4

class GUIMesh(
    renderWindow: RenderWindow,
    val matrix: Mat4,
) : Mesh(renderWindow, GUIMeshStruct, initialCacheSize = 40000), GUIVertexConsumer {

    override fun addVertex(position: Vec2t<*>, z: Int, texture: AbstractTexture, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        data.addAll(createVertex(matrix, position, z, texture, uv, tint, options))
    }

    override fun addCache(cache: GUIMeshCache) {
        data.addAll(cache.data)
    }

    data class GUIMeshStruct(
        val position: Vec3,
        val uv: Vec2,
        val textureLayer: Int,
        val tintColor: RGBColor,
    ) {
        companion object : MeshStruct(GUIMeshStruct::class)
    }

    companion object {
        const val BASE_Z = -0.0f
        const val Z_MULTIPLIER = -0.00001f

        fun createVertex(matrix: Mat4, position: Vec2t<*>, z: Int, texture: AbstractTexture, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?): FloatArray {
            val outPosition = matrix * Vec4(position.x.toFloat(), position.y.toFloat(), 1.0f, 1.0f)
            var color = tint

            options?.let { _ ->
                options.tintColor?.let { color = color.mix(it) }

                if (options.alpha != 1.0f) {
                    color = color.with(alpha = color.floatAlpha * options.alpha)
                }
            }
            return floatArrayOf(
                outPosition.x,
                outPosition.y,
                BASE_Z + Z_MULTIPLIER * z,
                uv.x,
                uv.y,
                Float.fromBits(texture.renderData?.layer ?: RenderConstants.DEBUG_TEXTURE_ID),
                Float.fromBits(color.rgba),
            )
        }
    }
}
