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
import glm_.vec2.Vec2i

object TextComponentRenderer : ChatComponentRenderer<TextComponent> {

    override fun render(initialOffset: Vec2i, offset: Vec2i, size: Vec2i, z: Int, element: Element, fontAlignment: ElementAlignments, renderWindow: RenderWindow, consumer: GUIVertexConsumer?, renderInfo: TextRenderInfo, text: TextComponent) {
        val elementMaxSize = element.maxSize
        // ToDo: Only 1 quad for the underline and the strikethrough
        var first = true

        var currentLineInfo = renderInfo.lines.getOrElse(renderInfo.currentLine) {
            val lineInfo = TextLineInfo()

            renderInfo.lines += lineInfo
            lineInfo
        }

        fun pushLine() {
            renderInfo.currentLine++
            currentLineInfo = TextLineInfo()
            renderInfo.lines += currentLineInfo
        }

        fun updateOffset() {
            val renderInfo = renderInfo
            offset.x = initialOffset.x
            if (consumer != null) {
                // set offset of the next line to match the expected alignment
                offset.x += fontAlignment.getOffset(element.size.x, renderInfo.lines[renderInfo.currentLine].width)
            }
        }

        /**
         * @return If the text can't fit into the layout anymore
         */
        fun wrap(): Boolean {
            val yAdd = Font.CHAR_HEIGHT + Font.VERTICAL_SPACING
            if (size.y + yAdd > elementMaxSize.y) {
                return true
            }
            if (consumer == null) {
                pushLine()
            } else {
                renderInfo.currentLine++
            }
            updateOffset()

            offset.y += yAdd
            size.y += yAdd

            return false
        }

        /**
         * @return If the text can't fit into the layout anymore
         */
        fun add(x: Int): Boolean {
            if (offset.x - initialOffset.x + x > elementMaxSize.x) {
                if (wrap()) {
                    return true
                }
            } else {
                offset.x += x
                if (consumer == null) {
                    currentLineInfo.width += x
                }
            }

            if (size.x < offset.x - initialOffset.x) {
                size.x += x
            }

            return false
        }


        if (offset.x == initialOffset.x) {
            updateOffset()
        }

        for (char in text.message.toCharArray()) {
            if (char == '\n') {
                if (wrap()) {
                    return
                }
                continue
            }

            // skip wrapped spaces
            if (offset.y != initialOffset.y && offset.x == initialOffset.x && char == ' ') {
                continue
            }

            val charData = renderWindow.font[char] ?: continue

            if (first) {
                first = false

                // Add initial size
                if (size.y == 0) {
                    size.y = Font.CHAR_HEIGHT + Font.VERTICAL_SPACING
                }
            } else if (offset.x != initialOffset.x && add(Font.HORIZONTAL_SPACING)) { // ToDo: Only add space when char fits
                return
            }

            val width = charData.calculateWidth(text)

            if (offset.x == initialOffset.x && offset.x - initialOffset.x + width > element.maxSize.x) {
                return
            }
            consumer?.let { charData.render(offset, z, text, it) }

            if (consumer != null) {
                renderInfo.lines[renderInfo.currentLine].chars += char
            }

            if (add(width)) {
                return
            }
        }

        if (text.formatting.contains(PreChatFormattingCodes.ITALIC)) {
            val italicOffset = CharData.ITALIC_OFFSET.ceil
            offset.x += italicOffset
            size.x += italicOffset
        }
    }
}
