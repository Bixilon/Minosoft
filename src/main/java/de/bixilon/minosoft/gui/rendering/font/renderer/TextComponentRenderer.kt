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
import de.bixilon.minosoft.gui.rendering.gui.elements.text.LabeledElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.util.MMath.ceil
import glm_.vec2.Vec2i

object TextComponentRenderer : ChatComponentRenderer<TextComponent> {

    override fun render(initialOffset: Vec2i, offset: Vec2i, size: Vec2i, z: Int, element: LabeledElement, renderWindow: RenderWindow, consumer: GUIVertexConsumer?, text: TextComponent) {
        var first = true

        /**
         * @return If the text can't fit into the layout anymore
         */
        fun wrap(): Boolean {
            val yAdd = Font.CHAR_HEIGHT + Font.VERTICAL_SPACING
            if (size.y + yAdd > element.maxSize.y) {
                return true
            }
            offset.x = initialOffset.x
            offset.y += yAdd
            size.y += yAdd

            return false
        }

        /**
         * @return If the text can't fit into the layout anymore
         */
        fun add(x: Int): Boolean {
            if (offset.x - initialOffset.x + x > element.maxSize.x) {
                if (wrap()) {
                    return true
                }
            } else {
                offset.x += x
            }

            if (size.x < offset.x - initialOffset.x) {
                size.x += x
            }

            return false
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
