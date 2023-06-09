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

package de.bixilon.minosoft.gui.rendering.font.renderer.component

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.data.text.formatting.TextFormatting
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.WorldGUIConsumer
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.font.types.font.Font
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

object TextComponentRenderer : ChatComponentRenderer<TextComponent> {

    private fun getRenderer(codePoint: Int, properties: TextRenderProperties, textFont: FontType?, fontManager: FontManager): CodePointRenderer? {
        return properties.font?.get(codePoint) ?: textFont?.get(codePoint) ?: fontManager.default[codePoint]
    }

    private fun getColor(properties: TextRenderProperties, text: TextComponent): RGBColor {
        return properties.forcedColor ?: text.color ?: properties.fallbackColor
    }

    private fun renderNewline(offset: TextOffset, info: TextRenderInfo) {
        TODO()
    }

    private fun renderCodePoint(offset: TextOffset, renderer: CodePointRenderer, color: RGBColor, properties: TextRenderProperties, info: TextRenderInfo, formatting: TextFormatting, consumer: GUIVertexConsumer?, options: GUIVertexOptions?) {
        if (consumer != null) {
            renderer.render(offset.offset, color, properties.shadow, FormattingCodes.BOLD in formatting, FormattingCodes.ITALIC in formatting, properties.scale, consumer, options)
        }
        offset.offset.x += 8
    }

    private fun renderStrikethrough() {
        TODO()
    }

    private fun renderUnderline() {
        TODO()
    }

    override fun render(offset: TextOffset, fontManager: FontManager, properties: TextRenderProperties, info: TextRenderInfo, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, text: TextComponent) {
        if (text.message.isEmpty()) return

        val textFont = fontManager[text.font]
        val color = getColor(properties, text)
        val formatting = text.formatting
        var skipWhitespaces = false

        for (codePoint in text.message.codePoints()) {
            if (codePoint == '\n'.code) {
                renderNewline(offset, info)
                skipWhitespaces = true
                continue
            }
            if (skipWhitespaces && Character.isWhitespace(codePoint)) {
                continue
            }
            skipWhitespaces = false

            val renderer = getRenderer(codePoint, properties, textFont, fontManager) ?: continue
            renderCodePoint(offset, renderer, color, properties, info, formatting, consumer, options)
        }
    }

    override fun calculatePrimitiveCount(text: TextComponent): Int {
        val length = text.message.length
        var count = length
        if (text.formatting.contains(FormattingCodes.BOLD)) {
            count += length
        }
        if (text.formatting.contains(FormattingCodes.UNDERLINED)) {
            count += length
        }
        if (text.formatting.contains(FormattingCodes.STRIKETHROUGH)) {
            count += length
        }

        return count
    }

    override fun render3dFlat(context: RenderContext, offset: Vec2i, scale: Float, maxSize: Vec2i, consumer: WorldGUIConsumer, text: TextComponent, light: Int) {
        val color = text.color ?: ChatColors.BLACK

        val font = context.font[text.font]


        // TODO: strike, underlined

        for (char in text.message.codePoints()) {
            val data = font?.get(char) ?: context.font.default[char] ?: continue
            val expectedWidth = ((data.calculateWidth(scale, false) + Font.HORIZONTAL_SPACING) * scale).toInt()
            if (maxSize.x - offset.x < expectedWidth) { // ToDo
                return
            }
            val width = ((data.render3d(color, shadow = false, FormattingCodes.BOLD in text.formatting, FormattingCodes.ITALIC in text.formatting, scale = scale, consumer) + Font.HORIZONTAL_SPACING) * scale).toInt()
            offset.x += width
            consumer.offset((width / ChatComponentRenderer.TEXT_BLOCK_RESOLUTION.toFloat()))
        }
    }
}
