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

package de.bixilon.minosoft.gui.rendering.font

import de.bixilon.kutil.math.simple.FloatMath.ceil
import de.bixilon.minosoft.data.text.PreChatFormattingCodes
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.TextStyle
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec2.Vec2t

class CharData(
    private val renderWindow: RenderWindow,
    val char: Char,
    val texture: AbstractTexture,
    val width: Int,
    val scaledWidth: Int,
    var uvStart: Vec2,
    var uvEnd: Vec2,
) {

    fun postInit() {
        uvStart = uvStart * texture.textureArrayUV
        uvEnd = uvEnd * texture.textureArrayUV
    }

    fun render(position: Vec2i, color: RGBColor, shadow: Boolean, italic: Boolean, bold: Boolean, strikethrough: Boolean, underlined: Boolean, consumer: GUIVertexConsumer, options: GUIVertexOptions?, scale: Float) {
        if (shadow) {
            _render(position, color, true, italic, bold, strikethrough, underlined, consumer, options, scale)
        }
        _render(position, color, false, italic, bold, strikethrough, underlined, consumer, options, scale)
    }

    private fun GUIVertexConsumer.addQuad(start: Vec2t<*>, end: Vec2t<*>, texture: AbstractTexture, uvStart: Vec2, uvEnd: Vec2, italic: Boolean, tint: RGBColor, options: GUIVertexOptions?) {
        val italicOffset = if (italic) (end.y.toFloat() - start.y.toFloat()) / Font.CHAR_HEIGHT.toFloat() * ITALIC_OFFSET else 0.0f

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

        for ((vertexIndex, textureIndex) in this.order) {
            addVertex(positions[vertexIndex], texture, texturePositions[textureIndex], tint, options)
        }
    }

    private fun _render(position: Vec2i, color: RGBColor, shadow: Boolean, italic: Boolean, bold: Boolean, strikethrough: Boolean, underlined: Boolean, vertexConsumer: GUIVertexConsumer, options: GUIVertexOptions?, scale: Float) {
        var color = color

        var shadowOffset = 0.0f
        if (shadow) {
            shadowOffset = SHADOW_OFFSET
            color *= 0.25f
        }

        var boldOffset = 0.0f

        if (bold) {
            boldOffset = BOLD_OFFSET * scale
        }
        val charHeight = Font.CHAR_HEIGHT * scale
        val horizontalSpacing = Font.HORIZONTAL_SPACING * scale
        val verticalSpacing = Font.VERTICAL_SPACING * scale


        val startPosition = Vec2(position) + (shadowOffset * scale)
        val endPosition = startPosition + (Vec2(scaledWidth * scale, charHeight))



        vertexConsumer.addQuad(startPosition, endPosition, texture, uvStart, uvEnd, italic, color, options)

        if (bold) {
            vertexConsumer.addQuad(startPosition + Vec2(boldOffset, 0.0f), endPosition + Vec2(boldOffset, 0.0f), texture, uvStart, uvEnd, italic, color, options)
        }

        if (strikethrough) {
            vertexConsumer.addQuad(startPosition + Vec2(-horizontalSpacing, charHeight / 2.0f - scale / 2), Vec2(endPosition.x + horizontalSpacing, startPosition.y + charHeight / 2.0f + scale / 2), renderWindow.WHITE_TEXTURE.texture, renderWindow.WHITE_TEXTURE.uvStart, renderWindow.WHITE_TEXTURE.uvEnd, italic, color, options)
        }

        if (underlined) {
            vertexConsumer.addQuad(startPosition + Vec2i(-horizontalSpacing, charHeight), Vec2i(endPosition.x + boldOffset + horizontalSpacing, startPosition.y + charHeight + verticalSpacing / 2.0f), renderWindow.WHITE_TEXTURE.texture, renderWindow.WHITE_TEXTURE.uvStart, renderWindow.WHITE_TEXTURE.uvEnd, italic, color, options)
        }

        // ToDo: Obfuscated
    }

    fun calculateWidth(style: TextStyle, scale: Float): Int {
        var width = scaledWidth.toFloat()
        if (style.formatting.contains(PreChatFormattingCodes.SHADOWED)) {
            width += SHADOW_OFFSET
        }

        return (width * scale).ceil
    }


    companion object {
        const val ITALIC_OFFSET = 2.5f
        const val SHADOW_OFFSET = 1.0f
        const val BOLD_OFFSET = 0.5f
    }
}
