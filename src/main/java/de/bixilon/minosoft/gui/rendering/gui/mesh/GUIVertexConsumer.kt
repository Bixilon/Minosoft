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
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties
import de.bixilon.minosoft.gui.rendering.system.base.RenderOrder
import de.bixilon.minosoft.gui.rendering.system.base.texture.TexturePart
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

interface GUIVertexConsumer {
    val order: RenderOrder

    fun addVertex(position: Vec2f, texture: ShaderTexture?, uv: Vec2f, tint: RGBAColor, options: GUIVertexOptions?) = addVertex(position.x, position.y, texture, uv.x, uv.y, tint, options)
    fun addVertex(x: Float, y: Float, texture: ShaderTexture?, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?)
    fun addVertex(x: Float, y: Float, textureId: Float, u: Float, v: Float, tint: RGBAColor, options: GUIVertexOptions?)

    fun addQuad(start: Vec2f, end: Vec2f, texture: ShaderTexture?, uvStart: Vec2f = UV_START, uvEnd: Vec2f = UV_END, tint: RGBAColor, options: GUIVertexOptions?) {
        val uvStart = texture?.transformUV(uvStart) ?: uvStart
        val uvEnd = texture?.transformUV(uvEnd) ?: uvEnd

        order.iterate { position, uv ->
            addVertex(
                if (position == 0 || position == 3) start.x else end.x, if (position <= 1) start.y else end.y,
                texture,
                if (uv == 0 || uv == 3) uvStart.x else uvEnd.x, if (uv <= 1) uvStart.y else uvEnd.y,
                tint, options,
            )
        }
    }

    fun addQuad(start: Vec2f, end: Vec2f, texture: TexturePart, tint: RGBAColor, options: GUIVertexOptions?) {
        addQuad(start, end, texture.texture, texture.uvStart, texture.uvEnd, tint, options)
    }

    fun addQuad(start: Vec2f, end: Vec2f, tint: RGBAColor, options: GUIVertexOptions?) {
        addQuad(start, end, null, tint = tint, options = options)
    }


    fun addChar(start: Vec2f, end: Vec2f, texture: Texture?, uvStart: Vec2f, uvEnd: Vec2f, italic: Boolean, tint: RGBAColor, options: GUIVertexOptions?) {
        val topOffset = if (italic) (end.y - start.y) / FontProperties.CHAR_BASE_HEIGHT * FormattingProperties.ITALIC_OFFSET else 0.0f

        order.iterate { position, uv ->
            val x = when (position) {
                0 -> start.x + topOffset
                1 -> end.x + topOffset
                2 -> end.x
                3 -> start.x
                else -> Broken()
            }
            addVertex(
                x, if (position <= 1) start.y else end.y,
                texture,
                if (uv == 0 || uv == 3) uvStart.x else uvEnd.x, if (uv <= 1) uvStart.y else uvEnd.y,
                tint, options,
            )
        }
    }

    fun addCache(cache: GUIMeshCache)

    fun ensureSize(size: Int)

    companion object {
        val UV_START = Vec2f(0.0f, 0.0f)
        val UV_END = Vec2f(1.0f, 1.0f)
    }
}
