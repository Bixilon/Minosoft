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

package de.bixilon.minosoft.gui.rendering.gui.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties
import de.bixilon.minosoft.gui.rendering.system.base.RenderOrder
import de.bixilon.minosoft.gui.rendering.system.base.texture.TexturePart
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

interface GUIVertexConsumer {
    val order: RenderOrder

    fun addVertex(position: Vec2, texture: ShaderTexture?, uv: Vec2, tint: RGBColor, options: GUIVertexOptions?) = addVertex(position.x, position.y, texture, uv.x, uv.y, tint, options)
    fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBColor, options: GUIVertexOptions?)
    fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: Int, options: GUIVertexOptions?)

    fun addQuad(start: Vec2, end: Vec2, texture: ShaderTexture?, uvStart: Vec2 = UV_START, uvEnd: Vec2 = UV_END, tint: RGBColor, options: GUIVertexOptions?) {
        val start = start.array
        val end = end.array
        val uvStart = (texture?.transformUV(uvStart) ?: uvStart).array
        val uvEnd = (texture?.transformUV(uvEnd) ?: uvEnd).array

        order.iterate { position, uv ->
            addVertex(
                if (position == 0 || position == 3) start[0] else end[0], if (position <= 1) start[1] else end[1],
                texture,
                if (uv == 0 || uv == 3) uvStart[0] else uvEnd[0], if (uv <= 1) uvStart[1] else uvEnd[1],
                tint, options,
            )
        }
    }

    fun addQuad(start: Vec2, end: Vec2, texture: TexturePart, tint: RGBColor, options: GUIVertexOptions?) {
        addQuad(start, end, texture.texture, texture.uvStart, texture.uvEnd, tint, options)
    }

    fun addQuad(start: Vec2, end: Vec2, tint: RGBColor, options: GUIVertexOptions?) {
        addQuad(start, end, null, tint = tint, options = options)
    }


    fun addChar(start: Vec2, end: Vec2, texture: Texture?, uvStart: Vec2, uvEnd: Vec2, italic: Boolean, tint: RGBColor, options: GUIVertexOptions?) {
        val topOffset = if (italic) (end.y - start.y) / FontProperties.CHAR_BASE_HEIGHT * FormattingProperties.ITALIC_OFFSET else 0.0f


        val start = start.array
        val end = end.array
        val uvStart = (texture?.transformUV(uvStart) ?: uvStart).array
        val uvEnd = (texture?.transformUV(uvEnd) ?: uvEnd).array

        order.iterate { position, uv ->
            val x = when (position) {
                0 -> start[0] + topOffset
                1 -> end[0] + topOffset
                2 -> end[0]
                3 -> start[0]
                else -> Broken()
            }
            addVertex(
                x, if (position <= 1) start[1] else end[1],
                texture,
                if (uv == 0 || uv == 3) uvStart[0] else uvEnd[0], if (uv <= 1) uvStart[1] else uvEnd[1],
                tint, options,
            )
        }
    }

    fun addCache(cache: GUIMeshCache)

    fun ensureSize(size: Int)

    companion object {
        val UV_START = Vec2(0.0f, 0.0f)
        val UV_END = Vec2(1.0f, 1.0f)
    }
}
