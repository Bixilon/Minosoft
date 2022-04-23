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

package de.bixilon.minosoft.gui.rendering.gui.gui.elements.input

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.Font
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.ColorElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.mark.MarkTextElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.mark.TextCursorStyles
import de.bixilon.minosoft.gui.rendering.gui.input.ModifierKeys
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.CursorShapes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.util.KUtil.codePointAtOrNull

class TextInputElement(
    guiRenderer: GUIRenderer,
    value: String = "",
    val maxLength: Int = Int.MAX_VALUE,
    val cursorStyles: TextCursorStyles = TextCursorStyles.CLICKED,
    var editable: Boolean = true,
    var onChange: () -> Unit = {},
    val background: Boolean = true,
    shadow: Boolean = true,
    scale: Float = 1.0f,
    val cutAtSize: Boolean = false,
    parent: Element? = null,
) : Element(guiRenderer) {
    private val cursor = ColorElement(guiRenderer, size = Vec2i(1, Font.TOTAL_CHAR_HEIGHT * scale))
    private val textElement = MarkTextElement(guiRenderer, "", background = false, parent = this, scale = scale, shadow = shadow)
    private val backgroundElement = ColorElement(guiRenderer, Vec2i.EMPTY, RenderConstants.TEXT_BACKGROUND_COLOR)
    private var cursorOffset: Vec2i = Vec2i.EMPTY
    val _value = StringBuffer(256)
    var value: String
        get() = _value.toString()
        set(value) {
            _pointer = value.length
            if (_value.equals(value)) {
                return
            }
            _set(value)
            forceApply()
        }
    private var textUpToDate = false
    var _pointer = 0
    private var cursorTick = 0

    init {
        this.parent = parent
        this._value.append(if (value.length > maxLength) value.substring(0, maxLength) else value)
        _pointer = this._value.length
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (background) {
            backgroundElement.render(offset, consumer, options)
        }
        textElement.render(offset, consumer, options)

        if (cursorTick < 20) {
            cursor.render(offset + cursorOffset, consumer, options)
        }
    }

    fun hideCursor() {
        cursorTick = 20
        cacheUpToDate = false
    }

    fun showCursor() {
        cursorTick = 19
        cacheUpToDate = false
    }

    fun unmark() {
        textElement.unmark()
    }

    private fun _set(value: String) {
        _value.replace(0, _value.length, value)
        _pointer = value.length
        onChange()
        textUpToDate = false
    }

    private fun cutOffText() {
        if (!cutAtSize) {
            return
        }

        val newValue = StringBuilder()
        for (line in textElement.renderInfo.lines) {
            newValue.append(line.text.message)
        }
        if (newValue.length != this._value.length) {
            _set(newValue.toString())
        }
    }

    override fun forceSilentApply() {
        if (!textUpToDate) {
            textElement._chatComponent = TextComponent(_value)
            textElement.unmark()
            textElement.forceSilentApply()
            textUpToDate = true
            cutOffText()
        }
        _size = Vec2i(textElement.size)
        backgroundElement.size = prefMaxSize

        cursorOffset = if (_pointer == 0) {
            Vec2i.EMPTY
        } else {
            val preCursorText = if (_pointer == value.length) {
                textElement
            } else {
                TextElement(guiRenderer, value.substring(0, _pointer), scale = textElement.scale, parent = this)
            }
            Vec2i(preCursorText.renderInfo.lines.lastOrNull()?.width ?: 0, maxOf(preCursorText.renderInfo.lines.size - 1, 0) * preCursorText.charHeight)
        }
        cacheUpToDate = false
    }

    override fun tick() {
        if (cursorTick-- <= 0) {
            cursorTick = 40
            cacheUpToDate = false
        } else if (cursorTick == 20 - 1) {
            cacheUpToDate = false
        }
    }

    private fun insert(string: String) {
        if (!editable) {
            return
        }
        val insert = string.replace("\n", "").replace("\r", "").replace('§', '&')
        if (textElement.marked) {
            _value.delete(textElement.markStartPosition, textElement.markEndPosition)
            if (_pointer > textElement.markStartPosition) {
                _pointer = textElement.markStartPosition
            }
        }
        val appendLength = minOf(insert.length, maxLength - _value.length)
        _value.insert(_pointer, insert.substring(0, appendLength))
        _pointer += appendLength
        textUpToDate = false
        onChange()
    }

    override fun onCharPress(char: Int): Boolean {
        if (_value.length >= maxLength || !editable) {
            return true
        }
        cursorTick = CURSOR_TICK_ON_ACTION
        insert(char.toChar().toString())
        forceApply()
        return true
    }

    private fun mark(mark: Boolean, right: Boolean, modify: Int) {
        val marked = textElement.marked
        if (mark) {
            if (modify == 0) {
                return
            }
            var start: Int = textElement.markStartPosition
            var end: Int = textElement.markEndPosition
            if (right) {
                if (start == _pointer) {
                    start += modify
                } else {
                    if (start < 0) {
                        start = _pointer
                        end = start
                    }
                    end += modify
                }
            } else {
                if (end == _pointer) {
                    end += modify
                } else {
                    if (start < 0) {
                        end = _pointer
                        start = end
                    }
                    start += modify
                }
            }
            textElement.mark(start, end)
            _pointer += modify
            return
        }

        _pointer = if (marked) if (right) textElement.markEndPosition else textElement.markStartPosition else if (right) minOf(_value.length, _pointer + modify) else maxOf(0, _pointer + modify)
        textElement.unmark()
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (type == KeyChangeTypes.RELEASE) {
            return true
        }
        val controlDown = guiRenderer.isKeyDown(ModifierKeys.CONTROL)
        val shiftDown = guiRenderer.isKeyDown(ModifierKeys.SHIFT)
        cursorTick = CURSOR_TICK_ON_ACTION
        when (key) {
            KeyCodes.KEY_V -> {
                if (controlDown) {
                    insert(guiRenderer.renderWindow.window.clipboardText)
                }
            }
            KeyCodes.KEY_X -> {
                if (controlDown) {
                    textElement.copy()
                    insert("")
                }
            }
            KeyCodes.KEY_BACKSPACE -> {
                if (_value.isEmpty() || !editable) {
                    return true
                }
                if (textElement.marked) {
                    insert("")
                } else if (_pointer == 0) {
                    return true
                } else {
                    val delete = if (controlDown) calculateWordPointer(false) else -1
                    _value.delete(_pointer + delete, _pointer)
                    _pointer += delete
                    textUpToDate = false
                }
            }
            KeyCodes.KEY_DELETE -> {
                if (_value.isEmpty() || !editable) {
                    return true
                }
                if (textElement.marked) {
                    insert("")
                } else if (_pointer == _value.length) {
                    return true
                } else {
                    val delete = if (controlDown) calculateWordPointer(true) else 1
                    _value.delete(_pointer, _pointer + delete)
                    textUpToDate = false
                }
            }
            KeyCodes.KEY_LEFT -> {
                if (_pointer == 0) {
                    if (!shiftDown) {
                        textElement.unmark()
                    }
                    return true
                }
                val modify = if (controlDown) {
                    calculateWordPointer(false)
                } else {
                    -1
                }
                mark(shiftDown, false, modify)
            }
            KeyCodes.KEY_RIGHT -> {
                if (_pointer == _value.length) {
                    if (!shiftDown && _pointer == _value.length) {
                        textElement.unmark()
                    }
                    return true
                }
                val modify = if (controlDown) {
                    calculateWordPointer(true)
                } else {
                    1
                }

                mark(shiftDown, true, modify)
            }
            KeyCodes.KEY_HOME -> {
                textElement.unmark()
                _pointer = 0
            }
            KeyCodes.KEY_END -> {
                textElement.unmark()
                _pointer = value.length
            }
            else -> return textElement.onKey(key, type)
        }
        forceApply()

        return true
    }

    override fun onMouseEnter(position: Vec2i, absolute: Vec2i): Boolean {
        renderWindow.window.cursorShape = CursorShapes.IBEAM
        return true
    }

    override fun onMouseLeave(): Boolean {
        renderWindow.window.resetCursor()
        return true
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (action != MouseActions.PRESS) {
            return true
        }
        val leftText = TextElement(guiRenderer, value, background = false)
        leftText.prefMaxSize = Vec2i(position.x, size.y)
        var pointer = 0
        var heightLeft = position.y
        for (line in leftText.renderInfo.lines) {
            val message = line.text.message
            pointer += message.length // ToDo: No formatting
            heightLeft -= Font.TOTAL_CHAR_HEIGHT
            if (heightLeft > 0) {
                continue
            }
            val charDelta = position.x - line.width
            val width = guiRenderer.renderWindow.font[value.codePointAtOrNull(pointer) ?: break]?.width ?: break
            if (charDelta != 0 && charDelta >= width / 2) {
                pointer++
            }
            break
        }
        this._pointer = pointer
        forceSilentApply()
        return true
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
    }

    private fun calculateWordPointer(right: Boolean): Int {
        var modify = if (right) 1 else -1
        while (_pointer + modify in 1 until _value.length) {
            val char = _value[_pointer + modify]
            if (char in WORD_SEPARATORS) {
                break
            }
            if (right) {
                modify++
            } else {
                modify--
            }
        }

        return modify
    }

    override fun onOpen() {
        cursorTick = 19 // make cursor visible
    }

    companion object {
        private const val CURSOR_TICK_ON_ACTION = 10
        private val WORD_SEPARATORS = charArrayOf(' ', ',', ';', '-', '\'', '`', '"', '“', '„', '.', '&', '@', '^', '/', '\\', '…', '*', '⁂', '=', '?', '!', '‽', '¡', '¿', '⸮', '#', '№', '%', '‰', '‱', '°', '⌀', '+', '−', '×', '÷', '~', '±', '∓', '–', '⁀', '|', '¦', '‖', '•', '·', '©', '©', '℗', '®', '‘', '’', '“', '”', '"', '"', '‹', '›', '«', '»', '(', ')', '[', ']', '{', '}', '⟨', '⟩', '”', '〃', '†', '‡', '❧', '☞', '◊', '¶', '⸿', '፠', '๛', '※', '§')
    }
}
