/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.string.StringUtil.codePointAtOrNull
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.ChildedElement
import de.bixilon.minosoft.gui.rendering.gui.abstractions.children.manager.SingleChildrenManager
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
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY

open class TextInputElement(
    guiRenderer: GUIRenderer,
    value: String = "",
    val maxLength: Int = Int.MAX_VALUE,
    val cursorStyles: TextCursorStyles = TextCursorStyles.CLICKED,
    var editable: Boolean = true,
    var onChangeCallback: () -> Unit = {},
    val background: RGBColor? = RenderConstants.TEXT_BACKGROUND_COLOR,
    properties: TextRenderProperties = TextRenderProperties.DEFAULT,
    val cutAtSize: Boolean = false,
    parent: Element? = null,
) : Element(guiRenderer), ChildedElement {
    override val children = SingleChildrenManager()
    protected val cursor = ColorElement(guiRenderer, size = Vec2(minOf(1.0f, properties.scale), properties.lineHeight))
    protected val textElement = MarkTextElement(guiRenderer, "", background = null, parent = this, properties = properties)
    protected val backgroundElement = ColorElement(guiRenderer, Vec2.EMPTY, RenderConstants.TEXT_BACKGROUND_COLOR)
    protected var cursorOffset: Vec2i = Vec2i.EMPTY
    val _value = StringBuffer(256)
    var value: String
        get() = _value.toString()
        set(value) {
            _pointer = value.length
            if (_value.equals(value)) {
                return
            }
            set(value)
        }
    var _pointer = 0
    private var cursorTick = 0

    init {
        this.parent = parent
        this._value.append(if (value.length > maxLength) value.substring(0, maxLength) else value)
        _pointer = this._value.length
        update()
    }

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        if (background != null) {
            backgroundElement.render(offset, consumer, options)
        }
        textElement.render(offset, consumer, options)

        if (cursorTick < 20) {
            cursor.render(offset + cursorOffset, consumer, options)
        }
    }

    fun hideCursor() {
        cursorTick = 20
        cache.invalidate()
    }

    fun showCursor() {
        cursorTick = 19
        cache.invalidate()
    }

    fun unmark() {
        textElement.unmark()
    }

    protected fun set(value: String) {
        val previous = _value.toString()
        val next = _value.replace(0, _value.length, value)
        _pointer = value.length
        if (previous != next.toString()) {
            onChange()
        }
        invalidate()
    }

    private fun cutOffText() {
        if (!cutAtSize) {
            return
        }

        val newValue = StringBuilder()
        for (line in textElement.info.lines) {
            newValue.append(line.text.message)
        }
        if (newValue.length != this._value.length) {
            set(newValue.toString())
        }
        if (_pointer > newValue.length) {
            _pointer = newValue.length
        }
    }

    override fun update() {
        textElement.text = TextComponent(_value)
        textElement.unmark()
        cutOffText()
        _size = Vec2(textElement.size)
        backgroundElement.size = prefMaxSize

        cursorOffset = if (_pointer == 0) {
            Vec2i.EMPTY
        } else {
            val preCursorText = if (_pointer == value.length) {
                textElement
            } else {
                TextElement(guiRenderer, value.substring(0, _pointer), properties = textElement.properties, parent = this)
            }
            Vec2i(preCursorText.info.lines.lastOrNull()?.width ?: 0, maxOf(preCursorText.info.lines.size - 1, 0) * preCursorText.properties.lineHeight)
        }
    }

    override fun tick() {
        if (cursorTick-- <= 0) {
            cursorTick = 40
            cache.invalidate()
        } else if (cursorTick == 20 - 1) {
            cache.invalidate()
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
        onChange()
    }

    override fun onCharPress(char: Int): Boolean {
        if (_value.length >= maxLength || !editable) {
            return true
        }
        cursorTick = CURSOR_TICK_ON_ACTION
        insert(char.toChar().toString())
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
            return false
        }
        val controlDown = guiRenderer.isKeyDown(ModifierKeys.CONTROL)
        val shiftDown = guiRenderer.isKeyDown(ModifierKeys.SHIFT)
        cursorTick = CURSOR_TICK_ON_ACTION
        when (key) {
            KeyCodes.KEY_V -> {
                if (controlDown) {
                    insert(guiRenderer.context.window.clipboardText)
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
                    onChange()
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
                    onChange()
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

        return true
    }

    override fun onMouseEnter(position: Vec2, absolute: Vec2): Boolean {
        context.window.cursorShape = CursorShapes.IBEAM
        return true
    }

    override fun onMouseLeave(): Boolean {
        context.window.resetCursor()
        return true
    }

    override fun onMouseAction(position: Vec2, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        if (action != MouseActions.PRESS) {
            return true
        }
        val leftText = TextElement(guiRenderer, value, background = null)
        leftText.prefMaxSize = Vec2(position.x, size.y)
        var pointer = 0
        var heightLeft = position.y
        for (line in leftText.info.lines) {
            val message = line.text.message
            pointer += message.length // ToDo: No formatting
            heightLeft -= TextRenderProperties.DEFAULT.lineHeight
            if (heightLeft > 0) {
                continue
            }
            val charDelta = position.x - line.width
            val width = guiRenderer.context.font.default[value.codePointAtOrNull(pointer) ?: break]?.calculateWidth(1.0f, true) ?: break
            if (charDelta != 0.0f && charDelta >= width / 2) {
                pointer++
            }
            break
        }
        this._pointer = pointer
        invalidate()
        return true
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

    open fun onChange() {
        onChangeCallback()
        invalidate()
    }

    companion object {
        private const val CURSOR_TICK_ON_ACTION = 10
        private val WORD_SEPARATORS = charArrayOf(' ', ',', ';', '-', '\'', '`', '"', '“', '„', '.', '&', '@', '^', '/', '\\', '…', '*', '⁂', '=', '?', '!', '‽', '¡', '¿', '⸮', '#', '№', '%', '‰', '‱', '°', '⌀', '+', '−', '×', '÷', '~', '±', '∓', '–', '⁀', '|', '¦', '‖', '•', '·', '©', '©', '℗', '®', '‘', '’', '“', '”', '"', '"', '‹', '›', '«', '»', '(', ')', '[', ']', '{', '}', '⟨', '⟩', '”', '〃', '†', '‡', '❧', '☞', '◊', '¶', '⸿', '፠', '๛', '※', '§')
    }
}
