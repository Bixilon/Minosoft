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

package de.bixilon.minosoft.gui.rendering.hud.elements.input

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.text.TextSetProperties
import de.bixilon.minosoft.gui.rendering.hud.nodes.layout.AbsoluteLayout
import de.bixilon.minosoft.gui.rendering.hud.nodes.primitive.LabelNode
import de.bixilon.minosoft.util.MMath
import glm_.vec2.Vec2i


open class TextField(
    renderWindow: RenderWindow,
    val properties: TextFieldProperties,
) : AbsoluteLayout(renderWindow), KeyConsumer, MouseConsumer {
    private var textBuilder: StringBuilder = StringBuilder(properties.defaultText)
    val textElement = LabelNode(renderWindow, sizing = sizing.copy(), setProperties = TextSetProperties(hardWrap = 100), text = ChatComponent.of(text), background = false)
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
        clearChildrenCache()
    }

    fun clearText() {
        textBuilder.clear()
        update()
    }

    private fun update() {
        textElement.text = ChatComponent.of(text)
    }

    override fun keyInput(keyCodes: KeyCodes) {
        when (keyCodes) {
            KeyCodes.KEY_BACKSPACE -> {
                if (textBuilder.isEmpty() || position == 0) {
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
                if (renderWindow.inputHandler.isKeyDown(KeyCodes.KEY_LEFT_CONTROL, KeyCodes.KEY_RIGHT_CONTROL, KeyCodes.KEY_LEFT_SHIFT, KeyCodes.KEY_RIGHT_SHIFT)) {
                    // new line
                    if (position > properties.maxLength) {
                        return
                    }
                    textBuilder.insert(position++, '\n')
                    return
                }
                properties.onSubmit(text)
                if (properties.submitCloses) {
                    properties.onClose()
                }
                text = properties.defaultText
            }
            KeyCodes.KEY_LEFT -> {
                position = MMath.clamp(position - 1, 0, text.length)
                return
            }
            KeyCodes.KEY_RIGHT -> {
                position = MMath.clamp(position + 1, 0, text.length)
                return
            }
            KeyCodes.KEY_V -> {
                if (renderWindow.inputHandler.isKeyDown(KeyCodes.KEY_LEFT_CONTROL, KeyCodes.KEY_RIGHT_CONTROL)) {
                    // paste
                    textInput(renderWindow.getClipboardText())
                }
            }
            // ToDo: Up and down for line breaks, shift and ctrl modifier, ...
            else -> {
                return
            }
        }
        update()
        super.keyInput(keyCodes)
    }

    override fun tick(tick: Long) {
        if ((tick / FIELD_CURSOR_BLINK_INTERVAL) % 2L == 0L && position == text.length) {
            textElement.sText = "$textBuilder" + "_"
        } else {
            textElement.sText = textBuilder.toString()
        }
    }

    override fun close() {
        properties.onClose()
    }

    override fun charInput(char: Char) {
        if (position >= properties.maxLength) {
            return
        }
        val previous = textBuilder.toString()
        textBuilder.insert(position++, char.toString())
        properties.onInput(previous, textBuilder.toString())
        update()
    }

    fun textInput(text: String) {
        if (position >= properties.maxLength) {
            return
        }
        val length = MMath.clamp(text.length, 0, properties.maxLength - position)
        val previous = textBuilder.toString()
        textBuilder.insert(position, text.substring(0, length))
        position += length
        properties.onInput(previous, textBuilder.toString())
        update()
    }

    companion object {
        const val FIELD_CURSOR_BLINK_INTERVAL = 8
    }
}
