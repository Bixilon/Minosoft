/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.primitive

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import glm_.vec2.Vec2

class TextElement(
    private var _text: ChatComponent = ChatComponent.valueOf(""),
    private val font: Font,
    start: Vec2,
    z: Int = 1,
    var background: Boolean = true,
) : ElementListElement(start, z) {
    var text: ChatComponent
        get() = _text
        set(value) {
            size = Vec2(0, 0)
            _text = value
            prepare()
        }
    var sText: String
        get() = text.message
        set(value) {
            text = ChatComponent.valueOf(sText)
        }

    init {
        prepare()
    }

    private fun prepare() {
        clear()
        size = if (text.message.isBlank()) {
            Vec2(0, Font.CHAR_HEIGHT)
        } else {
            val textSize = Vec2()
            text.prepareRender(Vec2(0, 1), Vec2(), font, this, z + 1, textSize)

            if (background) {
                drawBackground(textSize + 1, z)
            }
            Vec2(textSize + 1)
        }
    }

    private fun drawBackground(end: Vec2, z: Int, tintColor: RGBColor = RenderConstants.TEXT_BACKGROUND_COLOR) {
        addChild(ImageElement(Vec2(0, 0), end, null, z, tintColor))
    }
}
