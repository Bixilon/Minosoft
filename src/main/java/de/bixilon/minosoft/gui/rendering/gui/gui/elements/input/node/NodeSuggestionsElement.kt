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

package de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.node

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.array.ArrayUtil
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.popper.Popper
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes

class NodeSuggestionsElement(guiRenderer: GUIRenderer, position: Vec2, val inputElement: NodeTextInputElement) : Popper(guiRenderer, position) {
    private var suggestionText = Array(MAX_SUGGESTIONS) { TextElement(guiRenderer, ChatComponent.EMPTY).apply { prefMaxSize = Vec2(300, TextRenderProperties.DEFAULT.lineHeight) } }
    private var textCount = 0
    private var offset = 0
    var suggestions: List<Suggestion>? = null
        set(value) {
            if (field == value) {
                return
            }
            visible = !value.isNullOrEmpty()
            if (visible && value != null) {
                updateSuggestions(value)
            }
            field = value
        }

    var visible: Boolean = false
        set(value) {
            if (field == value) {
                return
            }
            if (value) {
                guiRenderer.popper.add(this)
            } else {
                guiRenderer.popper.remove(this)
            }
            field = value
        }

    val activeSuggestion: Suggestion?
        get() {
            val suggestions = suggestions ?: return null
            if (suggestions.isEmpty()) {
                return null
            }
            return suggestions[offset]
        }


    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)
        for ((index, suggestion) in suggestionText.withIndex()) {
            if (index >= textCount) {
                break
            }

            suggestion.render(offset + Vec2(0, index * TextRenderProperties.DEFAULT.lineHeight), consumer, options)
        }
    }

    private fun updateSuggestions(suggestions: List<Suggestion>) {
        val size = Vec2()
        var textCount = 0
        val offset = offset

        val startCutAt = maxOf(0, minOf(suggestions.size - MAX_SUGGESTIONS, offset))
        val endCutAt = minOf(startCutAt + MAX_SUGGESTIONS, suggestions.size)
        for ((index, suggestion) in suggestions.withIndex()) {
            if (index < startCutAt) {
                continue
            }
            if (index >= endCutAt) {
                break
            }
            val text = suggestionText[index - startCutAt]
            val textComponent = suggestion.name.copy()
            if (index == offset) {
                textComponent.setFallbackColor(ChatColors.YELLOW)
                textComponent.underline()
            }
            text.text = textComponent
            size.x = maxOf(size.x, text.size.x)
            size.y += TextRenderProperties.DEFAULT.lineHeight
            textCount++
        }
        this.textCount = textCount
        this._size = size
        tryUpdate()
    }

    fun modifyOffset(modify: Int) {
        if (modify == 0) {
            return
        }
        val suggestions = suggestions ?: return
        offset = ArrayUtil.modifyArrayIndex(offset + modify, suggestions.size)
        updateSuggestions(suggestions)
    }


    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (suggestions.isNullOrEmpty()) {
            return super.onKey(key, type)
        }
        if (type == KeyChangeTypes.RELEASE) {
            return super.onKey(key, type)
        }
        if (key == KeyCodes.KEY_RIGHT || key == KeyCodes.KEY_TAB) {
            return applySuggestion()
        }
        val offset = when (key) {
            KeyCodes.KEY_UP -> -1
            KeyCodes.KEY_PAGE_UP -> -5
            KeyCodes.KEY_DOWN -> 1
            KeyCodes.KEY_PAGE_DOWN -> 5
            else -> 0
        }
        modifyOffset(offset)
        return offset != 0
    }

    override fun onClose() {
        reset()
    }

    fun reset() {
        suggestions = null
        offset = 0
    }

    fun applySuggestion(): Boolean {
        inputElement.updateSuggestion(activeSuggestion ?: return false)
        return true
    }

    private companion object {
        const val MAX_SUGGESTIONS = 10
    }
}
