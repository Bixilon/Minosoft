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
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import glm_.vec2.Vec2i

class TextInputElement(
    guiRenderer: GUIRenderer,
    val maxLength: Int = Int.MAX_VALUE,
    val cursorStyles: TextCursorStyles = TextCursorStyles.CLICKED,
    var editable: Boolean = true,
) : Element(guiRenderer) {
    private val cursor = ColorElement(guiRenderer, size = Vec2i(1, Font.TOTAL_CHAR_HEIGHT))
    private val textElement = MarkTextElement(guiRenderer, "", background = false, parent = this)
    private val background = ColorElement(guiRenderer, Vec2i.EMPTY, RenderConstants.TEXT_BACKGROUND_COLOR)
    private var cursorOffset: Vec2i = Vec2i.EMPTY
    private val _value = StringBuffer(256)
    var value: String
        get() = _value.toString()
        set(value) {
            pointer = 0
            if (_value.equals(value)) {
                return
            }
            _value.replace(0, _value.length, value)
            forceApply()
        }
    private var textUpToDate = false
    private var pointer = 0
    private var cursorTick = 0

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        background.render(offset, consumer, options)

        textElement.render(offset, consumer, options)

        if (cursorTick < 20) {
            cursor.render(offset + cursorOffset, consumer, options)
        }
    }

    override fun forceSilentApply() {
        if (!textUpToDate) {
            textElement._chatComponent = TextComponent(_value)
            textElement.unmark()
            textElement.forceSilentApply()
            textUpToDate = true
        }
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
            if (pointer > textElement.markStartPosition) {
                pointer = textElement.markStartPosition
            }
        }
        val appendLength = minOf(insert.length, maxLength - _value.length)
        _value.insert(pointer, insert.substring(0, appendLength))
        pointer += appendLength
        textUpToDate = false
    }

    override fun onCharPress(char: Int) {
        if (_value.length >= maxLength || !editable) {
            return
        }
        cursorTick = CURSOR_TICK_ON_ACTION
        insert(char.toChar().toString())
        forceApply()
    }

    private fun mark(mark: Boolean, right: Boolean) {
        val marked = textElement.marked
        if (mark) {
            var start: Int = textElement.markStartPosition
            var end: Int = textElement.markEndPosition
            if (right) {
                if (start == pointer) {
                    start++
                } else {
                    if (start < 0) {
                        start = pointer
                        end = start
                    }
                    end++
                }
            } else {
                if (end == pointer) {
                    end--
                } else {
                    if (start < 0) {
                        end = pointer
                        start = end
                    }
                    start--
                }
            }
            textElement.mark(start, end)
            if (right) {
                pointer++
            } else {
                pointer--
            }
            return
        }

        pointer = if (marked) if (right) textElement.markEndPosition else textElement.markStartPosition else if (right) minOf(_value.length, pointer + 1) else maxOf(0, pointer - 1)
        textElement.unmark()
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes) {
        if (type == KeyChangeTypes.RELEASE) {
            return
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
                    return
                }
                if (textElement.marked) {
                    insert("")
                } else if (pointer == 0) {
                    return
                } else {
                    _value.deleteCharAt(pointer - 1)
                    pointer--
                    textUpToDate = false
                }
            }
            KeyCodes.KEY_DELETE -> {
                if (_value.isEmpty() || !editable) {
                    return
                }
                if (textElement.marked) {
                    insert("")
                } else if (pointer == _value.length) {
                    return
                } else {
                    _value.deleteCharAt(pointer)
                    textUpToDate = false
                }
            }
            KeyCodes.KEY_LEFT -> {
                if (pointer == 0) {
                    if (!shiftDown) {
                        textElement.unmark()
                    }
                    return
                }
                mark(shiftDown, false)
            }
            KeyCodes.KEY_RIGHT -> {
                if (pointer == _value.length) {
                    if (!shiftDown && pointer == _value.length) {
                        textElement.unmark()
                    }
                    return
                }
                mark(shiftDown, true)
            }
            KeyCodes.KEY_HOME -> {
                textElement.unmark()
                pointer = 0
            }
            KeyCodes.KEY_END -> {
                textElement.unmark()
                pointer = value.length
            }
            else -> return textElement.onKey(key, type)
        }
        forceApply()
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
    }

    companion object {
        private const val CURSOR_TICK_ON_ACTION = 10
    }
}
