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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderIdentifiable
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import de.bixilon.minosoft.gui.rendering.util.mesh.uv.UnpackedUV

class GUIMesh(
    context: RenderContext,
    val halfSize: Vec2f,
    data: AbstractFloatList,
) : Mesh(context, GUIMeshStruct, initialCacheSize = 32768, data = data), GUIVertexConsumer {
    private val whiteTexture = context.textures.whiteTexture
    override val order = context.system.quadOrder

    override fun clear() = Unit


    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        addVertex(data, halfSize, x, y, texture ?: whiteTexture.texture, u, v, tint, options)
    }

    override fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        addVertex(data, halfSize, x, y, textureId, u, v, tint, options)
    }

    override fun addCache(cache: GUIMeshCache) {
        data.add(cache.data)
    }

    data class GUIMeshStruct(
        val position: Vec2f,
        val uv: UnpackedUV,
        val indexLayerAnimation: Int,
        val tintColor: RGBColor,
    ) {
        companion object : MeshStruct(GUIMeshStruct::class)
    }

    companion object {

        fun transformPosition(position: Vec2f, halfSize: Vec2f): Vec2f {
            val res = Vec2f(position)
            res /= halfSize
            res.x -= 1.0f
            res.y = 1.0f - res.y
            return res
        }

        fun addVertex(data: AbstractFloatList, halfSize: Vec2f, x: Float, y: Float, texture: ShaderIdentifiable, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
            addVertex(data, halfSize, x, y, texture.shaderId.buffer(), u, v, tint, options)
        }

        fun addVertex(data: AbstractFloatList, halfSize: Vec2f, x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
            val x = x / halfSize.x - 1.0f
            val y = 1.0f - y / halfSize.y


            var color = tint

            if (options != null) {
                options.tintColor?.let { color = tint.mix(it) }

                if (options.alpha != 1.0f) {
                    color = color.with(alpha = color.alpha * options.alpha)
                }
            }

            data.add(x)
            data.add(y)
            data.add(u)
            data.add(v)
            data.add(textureId)
            data.add(color.rgba.buffer())
        }
    }

    override fun ensureSize(size: Int) {
        data.ensureSize(size)
    }
}
