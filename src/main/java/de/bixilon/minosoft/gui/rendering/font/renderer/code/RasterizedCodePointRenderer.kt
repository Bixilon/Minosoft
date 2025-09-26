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

package de.bixilon.minosoft.gui.rendering.font.renderer.code

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.kmath.vec.vec2.f.MVec2f
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties.BOLD_OFFSET
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties.SHADOW_COLOR
import de.bixilon.minosoft.gui.rendering.font.renderer.properties.FormattingProperties.SHADOW_OFFSET
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

interface RasterizedCodePointRenderer : CodePointRenderer {
    val texture: Texture

    val uvStart: Vec2f
    val uvEnd: Vec2f

    val width: Float


    override fun calculateWidth(scale: Float, shadow: Boolean): Float {
        var width = width
        if (shadow) {
            width += SHADOW_OFFSET
        }

        return width * scale
    }

    override fun render(position: Vec2f, properties: TextRenderProperties, color: RGBAColor, shadow: Boolean, bold: Boolean, italic: Boolean, scale: Float, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (shadow) {
            render(position + (SHADOW_OFFSET * scale), properties, color * SHADOW_COLOR, bold, italic, scale, consumer, options)
        }
        render(position, properties, color, bold, italic, scale, consumer, options)
    }

    fun calculateStart(properties: TextRenderProperties, base: Vec2f, scale: Float): MVec2f {
        val position = MVec2f(base)
        position.y += properties.charSpacing.top * scale

        return position
    }

    fun calculateEnd(properties: TextRenderProperties, start: Vec2f, scale: Float): MVec2f {
        val position = MVec2f(start)
        position.y += (properties.charBaseHeight * scale)
        position.x += width * scale

        return position
    }

    private fun render(position: Vec2f, properties: TextRenderProperties, color: RGBAColor, bold: Boolean, italic: Boolean, scale: Float, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val startPosition = calculateStart(properties, position, scale)
        val endPosition = calculateEnd(properties, startPosition.unsafe, scale)

        consumer.addChar(startPosition.unsafe, endPosition.unsafe, texture, uvStart, uvEnd, italic, color, options)

        if (bold) {
            // render char another time but offset in x direction
            val boldOffset = BOLD_OFFSET * scale
            startPosition.x += boldOffset
            endPosition.x += boldOffset
            consumer.addChar(
                start = startPosition.unsafe,
                end = endPosition.unsafe,
                texture, uvStart, uvEnd, italic, color, options)
        }
    }
}
