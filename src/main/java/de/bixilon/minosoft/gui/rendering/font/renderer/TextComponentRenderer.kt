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

package de.bixilon.minosoft.gui.rendering.font.renderer

import de.bixilon.minosoft.data.text.PreChatFormattingCodes
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.CharData
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.ElementAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.util.MMath.ceil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec2.Vec2i

object TextComponentRenderer : ChatComponentRenderer<TextComponent> {

    override fun render(initialOffset: Vec2i, offset: Vec2i, size: Vec2i, z: Int, element: Element, fontAlignment: ElementAlignments, renderWindow: RenderWindow, consumer: GUIVertexConsumer?, renderInfo: TextRenderInfo, text: TextComponent): Boolean {
        val elementMaxSize = element.maxSize
        // ToDo: Only 1 quad for the underline and the strikethrough

        var alignmentXOffset = 0
        if (size.x >= elementMaxSize.x || size.y >= elementMaxSize.y) {
            // The size is already bigger/equals the maximum size
            return true
        }

        fun addY(height: Int): Boolean {
            val nextY = offset.y + height
            val nextSizeY = nextY - initialOffset.y + Font.CHAR_HEIGHT // add initial height for chars
            if (nextSizeY >= elementMaxSize.y) {
                return true
            }
            offset.y = nextY
            if (nextSizeY > size.y) {
                size.y = nextSizeY
            }
            return false
        }

        fun wrap(): Boolean {
            if (addY(Font.CHAR_HEIGHT + Font.VERTICAL_SPACING)) {
                return true
            }
            renderInfo.currentLine++
            offset.x = initialOffset.x
            if (consumer == null) {
                // preparing phase
                renderInfo.lines += TextLineInfo()
            } else {
                if (renderInfo.currentLine >= renderInfo.lines.size) {
                    Log.log(LogMessageType.OTHER, LogLevels.FATAL) { "Crash because of $text (size=$size, maxSize=$elementMaxSize)" }
                }
                alignmentXOffset = fontAlignment.getOffset(element.size.x, renderInfo.lines[renderInfo.currentLine].width)
            }
            return false
        }

        fun addX(width: Int, wrap: Boolean = true): Boolean {
            val nextX = offset.x + width
            val nextSizeX = nextX + alignmentXOffset - initialOffset.x
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
                renderInfo.lines[renderInfo.currentLine].width += width
            }
            offset.x = nextX
            if (nextSizeX > size.x) {
                size.x = nextSizeX
            }
            return false
        }


        if (size.y == 0) {
            // Add initial height of the letter for the first line
            val nextSizeY = Font.CHAR_HEIGHT
            if (nextSizeY > elementMaxSize.y) {
                return true
            }
            if (consumer != null) {
                alignmentXOffset = fontAlignment.getOffset(element.size.x, renderInfo.lines[renderInfo.currentLine].width)
            } else {
                renderInfo.lines += TextLineInfo() // add line 0
            }
            size.y = nextSizeY
        }


        for (charCode in text.message.codePoints().toArray()) {
            val char = charCode.toChar()
            if (char == '\n') {
                if (wrap()) {
                    return true
                }
                continue
            }

            // skip spaces that are wrapped (because of a line break)
            if (offset.y != initialOffset.y && offset.x == initialOffset.x && char == ' ') {
                continue
            }

            val charData = renderWindow.font[char] ?: continue

            val charWidth = charData.calculateWidth(text)
            var width = charWidth

            if (offset.x != initialOffset.x) {
                // add spacing between letters
                width += Font.HORIZONTAL_SPACING
            }
            val previousY = offset.y

            if (addX(width)) {
                return true
            }

            val letterOffset = Vec2i(offset.x + alignmentXOffset, offset.y)

            // remove width from the offset again
            letterOffset.x -= charWidth

            if (previousY != offset.y) {
                // line was wrapped, we want to begin at the offset without the spacing
                // ToDo: Remove Font.HORIZONTAL_SPACING
            }

            consumer?.let { charData.render(letterOffset, z, text, it) }

            if (consumer == null) {
                renderInfo.lines[renderInfo.currentLine].chars += char
            }
        }

        if (text.formatting.contains(PreChatFormattingCodes.ITALIC)) {
            val italicOffset = CharData.ITALIC_OFFSET.ceil
            addX(italicOffset) // ToDo: Should this be forced?
        }
        return false
    }
}
