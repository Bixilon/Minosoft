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

package de.bixilon.minosoft.gui.rendering.font.renderer

import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.PreChatFormattingCodes
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import glm_.vec2.Vec2i

object TextComponentRenderer : ChatComponentRenderer<TextComponent> {

    override fun render(initialOffset: Vec2i, offset: Vec2i, size: Vec2i, element: Element, renderWindow: RenderWindow, consumer: GUIVertexConsumer?, options: GUIVertexOptions?, renderInfo: TextRenderInfo, text: TextComponent): Boolean {
        if (text.message.isEmpty()) {
            return false
        }
        val elementMaxSize = element.maxSize
        val elementSize = element.size
        val color = text.color ?: ChatColors.WHITE
        val shadow: Boolean = text.formatting.contains(PreChatFormattingCodes.SHADOWED)
        val italic: Boolean = text.formatting.contains(PreChatFormattingCodes.ITALIC)
        val bold: Boolean = text.formatting.contains(PreChatFormattingCodes.BOLD)
        val strikethrough: Boolean = text.formatting.contains(PreChatFormattingCodes.STRIKETHROUGH)
        val underlined: Boolean = text.formatting.contains(PreChatFormattingCodes.UNDERLINED)

        // ToDo: Only 1 quad for the underline and the strikethrough

        var alignmentXOffset = 0
        var currentLineText = ""
        if (size.x > elementMaxSize.x || size.y > elementMaxSize.y) {
            // The size is already bigger/equals the maximum size
            return true
        }

        fun pushLine(index: Int = -1) {
            var pushText: String = currentLineText
            if (index > 0) {
                pushText = currentLineText.substring(0, index)
            }
            if (consumer != null || pushText.isEmpty()) {
                return
            }
            renderInfo.currentLine.text += text.copy(message = pushText)
            currentLineText = ""
        }

        fun applyOffset() {
            if (consumer == null && offset.x == initialOffset.x + renderInfo.charMargin) {
                // preparing phase
                renderInfo.lines += TextLineInfo()
            } else {
                alignmentXOffset = renderInfo.fontAlignment.getOffset(elementSize.x, renderInfo.currentLine.width)
            }
        }

        fun addY(height: Int): Boolean {
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
            if (addY(renderInfo.charHeight)) {
                return true
            }
            pushLine()
            renderInfo.currentLineNumber++
            offset.x = initialOffset.x + renderInfo.charMargin
            applyOffset()
            return false
        }

        fun addX(width: Int, wrap: Boolean = true): Boolean {
            val nextX = offset.x + width
            val nextSizeX = nextX - initialOffset.x + renderInfo.charMargin // end margin
            if (nextSizeX > elementMaxSize.x) {
                if (!wrap) {
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


        if (size.y == 0) {
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

        var index = -1
        for (char in text.message.codePoints()) {
            index++
            if (char == '\n'.code) {
                if (wrap()) {
                    pushLine(index)
                    return true
                }
                continue
            }

            // skip spaces that are wrapped (because of a line break)
            if (offset.y != initialOffset.y + renderInfo.charMargin && offset.x == initialOffset.x + renderInfo.charMargin && char == ' '.code) {
                continue
            }

            val charData = renderWindow.font[char] ?: continue

            val charWidth = charData.calculateWidth(text, renderInfo.scale)
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
                pushLine(index)
                return true
            }

            val letterOffset = Vec2i(offset.x + alignmentXOffset, offset.y)

            // remove width from the offset again
            letterOffset.x -= charWidth

            if (previousY != offset.y) {
                // line was wrapped, we want to begin at the offset without the spacing
                // ToDo: Remove Font.HORIZONTAL_SPACING
            }

            consumer?.let { charData.render(letterOffset, color, shadow, italic, bold, strikethrough, underlined, it, options, renderInfo.scale) }

            if (consumer == null) {
                currentLineText += char.toChar()
            }
        }

        pushLine()
        return false
    }
}
