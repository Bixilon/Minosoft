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

package de.bixilon.minosoft.gui.rendering.font.renderer.code

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.data.text.formatting.TextFormatting
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FontProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties.BOLD_OFFSET
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties.ITALIC_OFFSET
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties.SHADOW_COLOR
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties.SHADOW_OFFSET
import de.bixilon.minosoft.gui.rendering.font.types.font.Font
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture

interface RasterizedCodePointRenderer : CodePointRenderer {
    val texture: AbstractTexture

    val uvStart: Vec2
    val uvEnd: Vec2

    val width: Float


    override fun calculateWidth(scale: Float, shadow: Boolean): Float {
        var width = width
        if (shadow) {
            width += SHADOW_OFFSET
        }

        return width * scale
    }

    override fun render(position: Vec2, color: RGBColor, shadow: Boolean, formatting: TextFormatting, consumer: GUIVertexConsumer, options: GUIVertexOptions?, scale: Float) {
        if (shadow) {
            render(position + (SHADOW_OFFSET * scale), color * SHADOW_COLOR, formatting, consumer, options, scale)
        }
        render(position, color, formatting, consumer, options, scale)
    }

    fun calculateStart(base: Vec2, scale: Float): Vec2 {
        val position = Vec2(base)
        position.y += (FontProperties.CHAR_SPACING_TOP * scale)

        return position
    }

    fun calculateEnd(start: Vec2, scale: Float): Vec2 {
        val position = Vec2(start)
        position.y += (FontProperties.CHAR_BASE_HEIGHT * scale)
        position.x += width * scale

        return position
    }

    private fun render(position: Vec2, color: RGBColor, formatting: TextFormatting, consumer: GUIVertexConsumer, options: GUIVertexOptions?, scale: Float) {
        var boldOffset = 0.0f

        val bold = FormattingCodes.BOLD in formatting
        val italic = FormattingCodes.ITALIC in formatting

        if (bold) {
            boldOffset = BOLD_OFFSET * scale
        }
        val charHeight = FontProperties.CHAR_BASE_HEIGHT * scale
        val horizontalSpacing = Font.HORIZONTAL_SPACING * scale
        val verticalSpacing = Font.VERTICAL_SPACING * scale


        val startPosition = calculateStart(position, scale)
        val endPosition = calculateEnd(startPosition, scale)



        consumer.addQuad(startPosition, endPosition, texture, uvStart, uvEnd, italic, color, options)

        if (FormattingCodes.BOLD in formatting) {
            consumer.addQuad(startPosition + Vec2(boldOffset, 0.0f), endPosition + Vec2(boldOffset, 0.0f), texture, uvStart, uvEnd, italic, color, options)
        }
        val whiteTexture = consumer.context.textureManager.whiteTexture

        if (FormattingCodes.STRIKETHROUGH in formatting) {
            consumer.addQuad(startPosition + Vec2(-horizontalSpacing, charHeight / 2.0f - scale / 2), Vec2(endPosition.x + horizontalSpacing, startPosition.y + charHeight / 2.0f + scale / 2), whiteTexture.texture, whiteTexture.uvStart, whiteTexture.uvEnd, italic, color, options)
        }

        if (FormattingCodes.UNDERLINED in formatting) {
            consumer.addQuad(startPosition + Vec2(-horizontalSpacing, charHeight), Vec2(endPosition.x + boldOffset + horizontalSpacing, startPosition.y + charHeight + verticalSpacing / 2.0f), whiteTexture.texture, whiteTexture.uvStart, whiteTexture.uvEnd, italic, color, options)
        }
    }


    private fun GUIVertexConsumer.addQuad(start: Vec2, end: Vec2, texture: AbstractTexture, uvStart: Vec2, uvEnd: Vec2, italic: Boolean, tint: RGBColor, options: GUIVertexOptions?) {
        val topOffset = if (italic) (end.y - start.y) / FontProperties.CHAR_BASE_HEIGHT * ITALIC_OFFSET else 0.0f

        val positions = arrayOf(
            Vec2(start.x + topOffset, start.y),
            Vec2(end.x + topOffset, start.y),
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

}
