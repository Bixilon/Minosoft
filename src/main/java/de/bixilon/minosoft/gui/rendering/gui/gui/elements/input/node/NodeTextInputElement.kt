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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.commands.errors.ReaderError
import de.bixilon.minosoft.commands.nodes.CommandNode
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.stack.print.PlayerPrintTarget
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.suggestion.util.SuggestionUtil
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.text.mark.TextCursorStyles
import de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.TextInputElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class NodeTextInputElement(
    guiRenderer: GUIRenderer,
    var node: CommandNode,
    value: String = "",
    maxLength: Int = Int.MAX_VALUE,
    cursorStyles: TextCursorStyles = TextCursorStyles.CLICKED,
    editable: Boolean = true,
    onChange: () -> Unit = {},
    background: Boolean = true,
    shadow: Boolean = true,
    scale: Float = 1.0f,
    cutAtSize: Boolean = false,
    parent: Element? = null,
) : TextInputElement(guiRenderer, value, maxLength, cursorStyles, editable, onChange, background, shadow, scale, cutAtSize, parent) {
    private var showError = false
    private val errorElement = NodeErrorElement(guiRenderer, Vec2i.EMPTY)
    private val suggestions = NodeSuggestionsElement(guiRenderer, Vec2i.EMPTY, this)


    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        super.forceRender(offset, consumer, options)

        errorElement.position = offset
        suggestions.position = offset + Vec2i(cursorOffset.x, 0)
    }


    private fun createStack(): CommandStack {
        return CommandStack(
            connection = guiRenderer.connection,
            print = PlayerPrintTarget(guiRenderer.connection),
        )
    }

    override fun onChange() {
        val value = value
        try {
            suggestions.suggestions = node.getSuggestions(CommandReader(value), createStack()).toList()
            updateError(null)
        } catch (exception: Throwable) {
            if (exception !is ReaderError) {
                Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { exception }
            }
            updateError(exception)
        }
        super.onChange()
    }

    fun submit() {
        val stack = createStack()
        try {
            node.execute(CommandReader(value), stack)
        } catch (exception: Throwable) {
            exception.message?.let { stack.print.print(TextComponent("Error: $it").color(ChatColors.RED)) }
        }
        updateError(null)
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (suggestions.onKey(key, type)) {
            return true
        }
        return super.onKey(key, type)
    }


    private fun updateError(error: Throwable?) {
        errorElement.error = error
        showError = error != null
        cacheUpToDate = false
        if (error != null) {
            suggestions.suggestions = null
        }
    }

    override fun onClose() {
        super.onClose()
        showError = false
        suggestions.onClose()
        errorElement.onClose()
    }

    fun updateSuggestion(suggestion: Suggestion) {
        val slash = value.startsWith("/")
        var value = SuggestionUtil.apply(value.removePrefix("/"), suggestion)
        if (slash) {
            value = "/$value" // TODO: dirty hack
        }
        _set(value)
        forceApply()
    }
}
