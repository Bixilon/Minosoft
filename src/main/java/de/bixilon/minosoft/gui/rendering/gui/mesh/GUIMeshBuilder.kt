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

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kutil.collections.primitive.floats.FloatList
import de.bixilon.kutil.collections.primitive.ints.IntList
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderIdentifiable
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.IndexUtil
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadMeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct

class GUIMeshBuilder(
    context: RenderContext,
    val halfSize: Vec2f,
    data: FloatList,
    index: IntList,
) : QuadMeshBuilder(context, GUIMeshStruct, 0, data = data, index = index), GUIVertexConsumer {
    private val whiteTexture = context.textures.whiteTexture

    override val reused get() = true


    override fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        addVertex(data, halfSize, x, y, texture ?: whiteTexture.texture, u, v, tint, options)
    }

    override fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
        addVertex(data, halfSize, x, y, textureId, u, v, tint, options)
    }

    override fun addCache(cache: GUIMeshCache) {
        data += cache.data
    }

    override fun addIndexQuad(front: Boolean, reverse: Boolean) = Unit

    fun fixIndex() {
        val vertices = data.size / GUIMeshStruct.floats

        val multiplier = (if (remap) (2 * PrimitiveTypes.TRIANGLE.vertices) else PrimitiveTypes.QUAD.vertices)
        this.index.ensureSize(vertices * multiplier)

        var start = index.size / multiplier
        val native = index.toUnsafeNativeBuffer()
        if (native == null) {
            if (start > vertices) {
                index.clear() // TODO: Optimize (just fake size down)
                start = 0
            }
        } else {
            native.limit(vertices * multiplier)
        }

        for (offset in start until vertices) {
            if (remap) {
                IndexUtil.addTriangleQuad(index, offset * PrimitiveTypes.QUAD.vertices, true, false)
            } else {
                // That could be left out (=> no index buffer)
                IndexUtil.addNativeQuad(index, offset * PrimitiveTypes.QUAD.vertices, true, false)
            }
        }
    }

    data class GUIMeshStruct(
        val position: Vec2f,
        val uv: Vec2f,
        val texture: Int,
        val tint: RGBColor,
    ) {
        companion object : MeshStruct(GUIMeshStruct::class)
    }

    companion object {

        fun transformPosition(position: Vec2f, halfSize: Vec2f): Vec2f {
            val res = position.mutable()
            res /= halfSize
            res.x -= 1.0f
            res.y = 1.0f - res.y
            return res.unsafe
        }

        fun addVertex(data: FloatList, halfSize: Vec2f, x: Float, y: Float, texture: ShaderIdentifiable, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
            addVertex(data, halfSize, x, y, texture.shaderId.buffer(), u, v, tint, options)
        }

        fun addVertex(data: FloatList, halfSize: Vec2f, x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?) {
            val x = x / halfSize.x - 1.0f // TODO (performance): inverse
            val y = 1.0f - y / halfSize.y // TODO (performance): inverse


            var color = tint

            if (options != null) {
                options.tint?.let { color = tint.mixRGB(it) }

                if (options.alpha != 1.0f) {
                    color = color.with(alpha = color.alphaf * options.alpha)
                }
            }

            data.add(
                x, y,
                u, v,
                textureId,
                color.rgba.buffer(),
            )
        }
    }
}
