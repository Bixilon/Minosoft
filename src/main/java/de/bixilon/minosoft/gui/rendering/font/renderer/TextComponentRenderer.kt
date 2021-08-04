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

    override fun render(offset: Vec2i, element: LabeledElement, renderWindow: RenderWindow, consumer: GUIVertexConsumer, text: TextComponent) {
        var first = true
        for (char in text.message.toCharArray()) {
            if (char == '\n') {
                offset.y += Font.CHAR_HEIGHT + Font.VERTICAL_SPACING
                continue
            }
            if (!first) {
                offset.x += Font.HORIZONTAL_SPACING
                first = false
            }

            val charData = renderWindow.font[char] ?: continue
            charData.render(offset, text, consumer)

            offset.x += charData.calculateWidth(text)
        }

        if (text.formatting.contains(PreChatFormattingCodes.ITALIC)) {
            offset.x += CharData.ITALIC_OFFSET.ceil
        }
        if (text.formatting.contains(PreChatFormattingCodes.BOLD)) {
            offset.x += CharData.BOLD_OFFSET.ceil
        }
    }
}
