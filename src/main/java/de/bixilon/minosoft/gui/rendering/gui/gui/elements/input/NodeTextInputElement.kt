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

import de.bixilon.minosoft.commands.nodes.CommandNode
import de.bixilon.minosoft.commands.stack.CommandStack
import de.bixilon.minosoft.commands.stack.print.PlayerPrintTarget
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.text.mark.TextCursorStyles

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


    private fun createStack(): CommandStack {
        return CommandStack(
            connection = guiRenderer.connection,
            print = PlayerPrintTarget(guiRenderer.connection)
        )
    }

    override fun onChange() {
        val value = value
        if (value.isBlank()) {
            return super.onChange()
        }
        try {
            node.getSuggestions(CommandReader(value), createStack())
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
        super.onChange()
    }

    fun submit() {
        try {
            node.execute(CommandReader(value), createStack())
        } catch (exception: Throwable) {
            exception.printStackTrace()
        }
    }
}
