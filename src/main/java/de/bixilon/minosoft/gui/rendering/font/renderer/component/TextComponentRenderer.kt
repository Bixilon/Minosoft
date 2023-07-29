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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.font.manager.FontManager
import de.bixilon.minosoft.gui.rendering.font.renderer.CodePointAddResult
import de.bixilon.minosoft.gui.rendering.font.renderer.code.CodePointRenderer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.LineRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextOffset
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.font.types.FontType
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

object TextComponentRenderer : ChatComponentRenderer<TextComponent> {

    private fun getRenderer(codePoint: Int, properties: TextRenderProperties, textFont: FontType?, fontManager: FontManager): CodePointRenderer? {
        return properties.font?.get(codePoint) ?: textFont?.get(codePoint) ?: fontManager.default[codePoint]
    }

    private fun getColor(properties: TextRenderProperties, text: TextComponent): RGBColor {
        return properties.forcedColor ?: text.color ?: properties.fallbackColor
    }

    private fun renderNewline(properties: TextRenderProperties, offset: TextOffset, info: TextRenderInfo, consuming: Boolean): Boolean {
        val height = offset.getNextLineHeight(properties)
        if (!offset.addLine(properties, info, properties.lineHeight, height, consuming)) {
            info.cutOff = true
            return true
        }

        if (!consuming) {
            info.size.y += height
        }


        return false
    }

    private fun renderStrikethrough(offset: Vec2, width: Float, italic: Boolean, color: RGBColor, properties: TextRenderProperties, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val y = offset.y + properties.charSpacing.top + properties.charBaseHeight / 2.0f - 1.0f

        // TODO: italic
        consumer.addQuad(Vec2(offset.x, y), Vec2(offset.x + width, y + 1.0f), color, options)
    }

    private fun renderUnderline(offset: Vec2, width: Float, italic: Boolean, color: RGBColor, properties: TextRenderProperties, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val y = offset.y + properties.charSpacing.top + properties.charBaseHeight

        // TODO: italic
        consumer.addQuad(Vec2(offset.x, y), Vec2(offset.x + width, y + 1.0f), color, options)
    }

    private fun renderFormatting(offset: Vec2, text: TextComponent, width: Float, color: RGBColor, properties: TextRenderProperties, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (width <= 0.0f) return
        val italic = FormattingCodes.ITALIC in text.formatting
        if (FormattingCodes.UNDERLINED in text.formatting) {
            renderUnderline(offset, width, italic, color, properties, consumer, options)
        }
        if (FormattingCodes.STRIKETHROUGH in text.formatting) {
            renderStrikethrough(offset, width, italic, color, properties, consumer, options)
        }
    }


    private fun LineRenderInfo.pushAndRender(offset: Vec2, text: TextComponent, line: StringBuilder, width: Float, color: RGBColor, properties: TextRenderProperties, consumer: GUIVertexConsumer?, options: GUIVertexOptions?) {
        if (consumer == null) {
            push(text, line)
        } else {
            renderFormatting(offset, text, width, color, properties, consumer, options)
        }
    }


    override fun render(offset: TextOffset, fontManager: FontManager, properties: TextRenderProperties, info: TextRenderInfo, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, text: TextComponent): Boolean {
        if (text.message.isEmpty()) return false


        if (consumer != null && info.lineIndex == 0 && info.lines.isNotEmpty() && offset.offset.x == offset.initial.x) {
            // switched to consumer mode but offset was not updated yet
            offset.align(properties.alignment, info.lines.first().width, info.size)
        }

        val textFont = fontManager[text.font]
        val color = getColor(properties, text)
        val formatting = text.formatting
        var skipWhitespaces = false

        val line = StringBuilder()
        var update = false
        var filled = false
        val lineStart = Vec2(offset.offset)


        val stream = text.message.codePoints().iterator()
        while (stream.hasNext()) {
            val codePoint = stream.nextInt()
            if (codePoint == '\n'.code) {
                if (!properties.allowNewLine) continue
                val lineIndex = info.lineIndex
                val width = offset.offset.x - lineStart.x
                filled = renderNewline(properties, offset, info, consumer != null)
                if (line.isNotEmpty()) {
                    info.lines[lineIndex].pushAndRender(lineStart, text, line, width, color, properties, consumer, options)
                }
                lineStart(offset.offset)
                skipWhitespaces = true
                update = false
                if (filled) break else continue
            }
            if (skipWhitespaces && Character.isWhitespace(codePoint)) {
                continue
            }

            val renderer = getRenderer(codePoint, properties, textFont, fontManager)
            if (renderer == null || renderer.calculateWidth(properties.scale, properties.shadow) <= 0.0f) {
                update = true
                continue
            }
            skipWhitespaces = false
            update = false

            val lineIndex = info.lineIndex

            val width = offset.offset.x - lineStart.x

            val lineInfo = renderer.render(offset, color, properties, info, formatting, codePoint, consumer, options)
            if (lineIndex != info.lineIndex && info.lines.isNotEmpty() && consumer != null) {
                renderFormatting(lineStart, text, width, color, properties, consumer, options)
                lineStart(offset.offset)
            }
            if (lineInfo == CodePointAddResult.BREAK) {
                filled = true
                break
            }

            if (lineIndex != info.lineIndex) {
                // new line started
                if (consumer == null) {
                    info.lines[lineIndex].push(text, line) // previous line
                } else {
                    line.clear()
                }
            }
            line.appendCodePoint(codePoint)
        }

        if (update && consumer == null) {
            info.update(offset, properties, 0.0f, 0.0f, true)
        }
        if (line.isNotEmpty()) {
            info.lines[info.lineIndex].pushAndRender(lineStart, text, line, offset.offset.x - lineStart.x, color, properties, consumer, options)
        }

        return filled
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
}
