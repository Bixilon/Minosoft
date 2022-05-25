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

package de.bixilon.minosoft.gui.rendering.gui.gui.elements.input.node

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.popper.text.TextPopper

class NodeSuggestionsElement(guiRenderer: GUIRenderer, position: Vec2i) : TextPopper(guiRenderer, position, ChatComponent.EMPTY) {
    var suggestions: List<Any?>? = null
        set(value) {
            if (field == value) {
                return
            }
            visible = value != null && value.isNotEmpty()
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

    init {
        trackMouse = false
    }

    private fun updateSuggestions(suggestions: List<Any?>) {
        val message = StringBuilder()
        for (suggestion in suggestions) {
            message.append(suggestion.toString())
            message.append('\n')
        }
        this.textElement.text = TextComponent(message.toString()).color(ChatColors.RED)
        forceSilentApply()
    }
}
