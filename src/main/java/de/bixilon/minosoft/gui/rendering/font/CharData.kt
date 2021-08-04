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

package de.bixilon.minosoft.gui.rendering.font

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.PreChatFormattingCodes
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.TextStyle
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.mesh.FontVertexConsumer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.util.KUtil.decide
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec2.Vec2t

class CharData(
    private val renderWindow: RenderWindow,
    val char: Char,
    val texture: AbstractTexture,
    val width: Int,
    var uvStart: Vec2,
    var uvEnd: Vec2,
) {

    fun postInit() {
        uvStart = uvStart * texture.textureArrayUV
        uvEnd = uvEnd * texture.textureArrayUV
    }

    fun render(position: Vec2i, style: TextStyle, vertexConsumer: FontVertexConsumer) {
        render(position, false, style, vertexConsumer)
        if (style.formatting.contains(PreChatFormattingCodes.SHADOWED)) {
            render(position, true, style, vertexConsumer)
        }
    }

    private fun FontVertexConsumer.addQuad(start: Vec2t<*>, end: Vec2t<*>, texture: AbstractTexture, uvStart: Vec2, uvEnd: Vec2, italic: Boolean, tint: RGBColor) {
        val italicOffset = italic.decide(2.5f, 0.0f)
        val positions = arrayOf(
            Vec2(start.x.toFloat() + italicOffset, start.y),
            Vec2(end.x.toFloat() + italicOffset, start.y),
            end,
            Vec2(start.x, end.y),
        )
        val texturePositions = arrayOf(
            Vec2(uvEnd.x, uvStart.y),
            uvStart,
            Vec2(uvStart.x, uvEnd.y),
            uvEnd,
        )

        for ((vertexIndex, textureIndex) in Mesh.QUAD_DRAW_ODER) {
            addVertex(positions[vertexIndex], texture, texturePositions[textureIndex], tint)
        }
    }

    private fun render(position: Vec2i, shadow: Boolean, style: TextStyle, vertexConsumer: FontVertexConsumer) {
        var color = style.color ?: ChatColors.WHITE


        var shadowOffset = 0.0f
        if (shadow) {
            shadowOffset = 1.0f
            color *= 0.25f
        }

        var boldOffset = 0.0f

        if (style.formatting.contains(PreChatFormattingCodes.BOLD)) {
            boldOffset = 0.5f
        }


        val startPosition = Vec2(position) + shadowOffset
        val endPosition = startPosition + Vec2(width, Font.CHAR_HEIGHT.toFloat())


        val italic = style.formatting.contains(PreChatFormattingCodes.ITALIC)


        vertexConsumer.addQuad(startPosition, endPosition, texture, uvStart, uvEnd, italic, color)

        if (style.formatting.contains(PreChatFormattingCodes.BOLD)) {
            vertexConsumer.addQuad(startPosition + Vec2(boldOffset, 0.0f), endPosition + Vec2(boldOffset, 0.0f), texture, uvStart, uvEnd, italic, color)
        }

        if (style.formatting.contains(PreChatFormattingCodes.STRIKETHROUGH)) {
            vertexConsumer.addQuad(startPosition + Vec2(-1.0f, Font.CHAR_HEIGHT / 2.0f + 0.5f), Vec2(endPosition.x + boldOffset, startPosition.y + Font.CHAR_HEIGHT / 2.0f + 1.5f), renderWindow.WHITE_TEXTURE.texture, renderWindow.WHITE_TEXTURE.uvStart, renderWindow.WHITE_TEXTURE.uvEnd, italic, color)
        }
        if (style.formatting.contains(PreChatFormattingCodes.UNDERLINED)) {
            vertexConsumer.addQuad(startPosition + Vec2i(-1.0f, Font.CHAR_HEIGHT), Vec2i(endPosition.x + boldOffset, startPosition.y + Font.CHAR_HEIGHT + 1.0f), renderWindow.WHITE_TEXTURE.texture, renderWindow.WHITE_TEXTURE.uvStart, renderWindow.WHITE_TEXTURE.uvEnd, italic, color)
        }

        // ToDo: Obfuscated
    }
}
