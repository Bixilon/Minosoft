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

package de.bixilon.minosoft.gui.rendering.hud.elements.input

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.hud.nodes.layout.AbsoluteLayout
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.ImageNode
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.LabelNode
import de.bixilon.minosoft.util.MMath
import glm_.vec2.Vec2i


open class TextField(
    renderWindow: RenderWindow,
    defaultText: String = "",
    var maxLength: Int = 256,
) : AbsoluteLayout(renderWindow), KeyConsumer, MouseConsumer {
    override var focused: Boolean = true
    private var textBuilder: StringBuilder = StringBuilder(defaultText)
    private val textElement = LabelNode(renderWindow, sizing = sizing, text = ChatComponent.valueOf(raw = text), background = false)
    private var position = text.length

    var text: String
        get() = textBuilder.toString()
        set(value) {
            position = value.length
            textBuilder = StringBuilder(value)
            update()
        }

    init {
        addChild(Vec2i(0, 0), textElement)
        addChild(Vec2i(0, 0), ImageNode(renderWindow, sizing = sizing, renderWindow.WHITE_TEXTURE, 0, RenderConstants.TEXT_BACKGROUND_COLOR))
        clearChildrenCache()
    }

    fun clearText() {
        textBuilder.clear()
        update()
    }

    private fun update() {
        textElement.text = ChatComponent.valueOf(raw = text)
    }

    override fun keyInput(keyCodes: KeyCodes) {
        when (keyCodes) {
            KeyCodes.KEY_BACKSPACE -> {
                if (textBuilder.isEmpty()) {
                    return
                }
                textBuilder.deleteCharAt(--position)
            }
            KeyCodes.KEY_DELETE -> {
                if (textBuilder.isEmpty() || position == textBuilder.length) {
                    return
                }
                textBuilder.deleteCharAt(position)
            }
            KeyCodes.KEY_ENTER -> {
                if (position > maxLength) {
                    return
                }
                textBuilder.insert(position++, '\n')
            }
            KeyCodes.KEY_LEFT -> {
                position = MMath.clamp(position - 1, 0, text.length)
                return
            }
            KeyCodes.KEY_RIGHT -> {
                position = MMath.clamp(position + 1, 0, text.length)
                return
            }
            // ToDo: Up and down for line breaks, shift and ctrl modifier, ...
            else -> {
                return
            }
        }
        update()
        super.keyInput(keyCodes)
    }

    override fun charInput(char: Char) {
        if (position >= maxLength) {
            return
        }
        textBuilder.insert(position++, char.toString())
        update()
    }
}
