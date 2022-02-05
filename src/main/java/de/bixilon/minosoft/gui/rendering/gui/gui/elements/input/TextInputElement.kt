/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.gui.elements.input

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import glm_.vec2.Vec2i

class TextInputElement(
    guiRenderer: GUIRenderer,
    val maxLength: Int = Int.MAX_VALUE,
) : Element(guiRenderer) {
    private val cursor = ColorElement(guiRenderer, size = Vec2i(1, Font.TOTAL_CHAR_HEIGHT))
    private val textElement = TextElement(guiRenderer, "", background = false, parent = this)
    private val background = ColorElement(guiRenderer, Vec2i.EMPTY, RenderConstants.TEXT_BACKGROUND_COLOR)
    private var cursorOffset: Vec2i = Vec2i.EMPTY
    private var _value: String = ""
    var value: String
        get() = _value
        set(value) {
            pointer = 0
            if (_value == value) {
                return
            }
            _value = value
            forceApply()
        }
    private var pointer = 0
    private var cursorTick = 0

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        background.render(offset, z, consumer, options)
        textElement.render(offset, z + 1, consumer, options)

        if (cursorTick < 20) {
            cursor.render(offset + cursorOffset, z + 1 + TextElement.LAYERS, consumer, options)
        }
        return TextElement.LAYERS + 2
    }

    override fun forceSilentApply() {
        textElement.text = _value
        textElement.silentApply()
        background.size = Vec2i(prefMaxSize.x, prefMaxSize.y)

        cursorOffset = if (pointer == 0) {
            Vec2i.EMPTY
        } else {
            val preCursorText = if (pointer == value.length) {
                textElement
            } else {
                TextElement(guiRenderer, value.substring(0, pointer), parent = this)
            }
            Vec2i(preCursorText.renderInfo.lines.lastOrNull()?.width ?: 0, maxOf(preCursorText.renderInfo.lines.size - 1, 0) * preCursorText.charHeight)
        }
        cacheUpToDate = false
    }

    override fun tick() {
        if (cursorTick-- <= 0) {
            cursorTick = 40
        }
        cacheUpToDate = false
    }

    private fun silentAppend(string: String) {
        val appendLength = minOf(string.length, maxLength - _value.length)
        _value = _value.substring(0, pointer) + string.substring(0, appendLength) + _value.substring(pointer, _value.length)
        pointer += appendLength
    }

    override fun onCharPress(char: Int) {
        if (_value.length >= maxLength) {
            return
        }
        cursorTick = CURSOR_TICK_ON_ACTION
        silentAppend(char.toChar().toString())
        forceApply()
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes) {
        if (type == KeyChangeTypes.RELEASE) {
            return
        }
        val controlDown = guiRenderer.isKeyDown(ModifierKeys.CONTROL)
        cursorTick = CURSOR_TICK_ON_ACTION
        when (key) {
            KeyCodes.KEY_V -> {
                if (controlDown) {
                    silentAppend(guiRenderer.renderWindow.window.clipboardText)
                }
            }
            KeyCodes.KEY_BACKSPACE -> {
                if (pointer == 0 || _value.isEmpty()) {
                    return
                }
                _value = _value.removeRange(pointer - 1, pointer)
                pointer--
            }
            KeyCodes.KEY_DELETE -> {
                if (pointer == _value.length || _value.isEmpty()) {
                    return
                }
                _value = _value.removeRange(pointer, pointer + 1)
            }
            KeyCodes.KEY_LEFT -> {
                if (pointer == 0) {
                    return
                }
                pointer--
            }
            KeyCodes.KEY_RIGHT -> {
                if (pointer == _value.length) {
                    return
                }
                pointer++
            }
            else -> return
        }
        forceApply()
    }

    companion object {
        private const val CURSOR_TICK_ON_ACTION = 10
    }
}
