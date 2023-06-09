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
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.FormattingCodes
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.font.WorldGUIConsumer
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextLineInfo
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderInfo
import de.bixilon.minosoft.gui.rendering.font.types.font.Font
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions

object TextComponentRenderer : ChatComponentRenderer<TextComponent> {

    override fun render(initialOffset: Vec2, offset: Vec2, size: Vec2, element: Element, context: RenderContext, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, renderInfo: TextRenderInfo, text: TextComponent): Boolean {
        if (text.message.isEmpty()) {
            return false
        }
        val font = context.font[text.font]
        val elementMaxSize = element.maxSize
        val elementSize = element.size
        val color = text.color ?: ChatColors.WHITE
        val shadow = renderInfo.shadow
        val bold: Boolean = text.formatting.contains(FormattingCodes.BOLD)
        val italic: Boolean = text.formatting.contains(FormattingCodes.ITALIC)

        // ToDo: Only 1 quad for the underline and the strikethrough
        // TODO: strike, underlined

        var alignmentXOffset = 0.0f
        var currentLineText = ""
        if (size.x > elementMaxSize.x || size.y > elementMaxSize.y) {
            // The size is already bigger/equals the maximum size
            return true
        }

        fun pushLine(cutAt: Int = -1) {
            var pushText: String = currentLineText
            if (cutAt > 0) {
                pushText = currentLineText.substring(0, cutAt)
            }
            if (consumer != null || pushText.isEmpty()) {
                return
            }
            renderInfo.currentLine.text += text.copy(message = pushText)
            currentLineText = ""
        }

        fun applyOffset() {
            val lastLine = renderInfo.lines.getOrNull(renderInfo.lineIndex)
            if (consumer == null && offset.x == initialOffset.x + renderInfo.charMargin && (lastLine == null || lastLine.width != 0.0f)) {
                // preparing phase
                renderInfo.lines += TextLineInfo()
            } else {
                alignmentXOffset = renderInfo.fontAlignment.getOffset(elementSize.x.toFloat(), renderInfo.currentLine.width)
            }
        }

        fun addY(height: Float): Boolean {
            val nextY = offset.y + height
            val nextSizeY = nextY - initialOffset.y + renderInfo.charHeight // add initial height for chars + end margin
            if (nextSizeY > elementMaxSize.y) {
                return true
            }
            offset.y = nextY
            if (nextSizeY > size.y) {
                size.y = nextSizeY
            }
            return false
        }

        fun wrap(): Boolean {
            pushLine()
            if (addY(renderInfo.charHeight)) {
                return true
            }
            renderInfo.lineIndex++
            offset.x = initialOffset.x + renderInfo.charMargin
            applyOffset()
            return false
        }

        fun addX(width: Float, wrap: Boolean = true): Boolean {
            val nextX = offset.x + width
            val nextSizeX = nextX - initialOffset.x - renderInfo.charMargin // end margin
            if (nextSizeX > elementMaxSize.x) {
                if (!wrap) {
                    pushLine()
                    return true
                }
                if (wrap()) {
                    return true
                }
                return addX(width, false)
            }
            if (consumer == null) {
                renderInfo.currentLine.width += width
            }
            offset.x = nextX
            if (nextSizeX > size.x) {
                size.x = nextSizeX
            }
            return false
        }


        if (size.y == 0.0f) {
            // Add initial height of the letter for the first line
            val nextSizeY = renderInfo.charHeight
            if (nextSizeY > elementMaxSize.y) {
                return true
            }
            size.y = nextSizeY
            size.x += renderInfo.charMargin * 2
            offset += renderInfo.charMargin
        }
        applyOffset()

        for (char in text.message.codePoints()) {
            if (char == '\n'.code) {

                if (wrap()) {
                    return true
                }
                continue
            }

            // skip spaces that are wrapped (because of a line break)
            if (offset.y != initialOffset.y + renderInfo.charMargin && offset.x == initialOffset.x + renderInfo.charMargin && char == ' '.code) {
                continue
            }

            val charData = font?.get(char) ?: context.font.default[char] ?: continue

            val charWidth = charData.calculateWidth(renderInfo.scale, renderInfo.shadow)
            var width = charWidth

            if (offset.x != initialOffset.x + renderInfo.charMargin) {
                // add spacing between letters
                width += (if (bold) {
                    Font.HORIZONTAL_SPACING_BOLD
                } else if (shadow) {
                    Font.HORIZONTAL_SPACING_SHADOW
                } else {
                    Font.HORIZONTAL_SPACING
                } * renderInfo.scale).toInt()
            }
            val previousY = offset.y

            if (addX(width)) {
                return true
            }

            val letterOffset = Vec2(offset.x + alignmentXOffset, offset.y)

            // remove width from the offset again
            letterOffset.x -= charWidth

            if (previousY != offset.y) {
                // line was wrapped, we want to begin at the offset without the spacing
                // ToDo: Remove Font.HORIZONTAL_SPACING
            }

            consumer?.let { charData.render(letterOffset, color, shadow, bold, italic, renderInfo.scale, it, options) }

            if (consumer == null) {
                currentLineText += char.toChar()
            }
        }

        pushLine()
        return false
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
