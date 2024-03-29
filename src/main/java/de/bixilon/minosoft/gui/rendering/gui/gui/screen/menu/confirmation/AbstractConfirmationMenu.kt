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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.confirmation

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.gui.rendering.font.renderer.element.TextRenderProperties
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.input.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.elements.spacer.SpacerElement
import de.bixilon.minosoft.gui.rendering.gui.elements.text.TextElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu.Menu

abstract class AbstractConfirmationMenu(
    guiRenderer: GUIRenderer,
    private val text: Any,
    private val subtext: Any,
) : Menu(guiRenderer) {

    protected abstract fun createButtons(): Array<ButtonElement>

    fun close() {
        guiRenderer.gui.pop()
    }

    fun show() {
        guiRenderer.gui.push(this)
    }

    protected fun createCopyToClipboardButton(text: String): ButtonElement {
        return ButtonElement(guiRenderer, "Copy to clipboard") {
            context.window.clipboardText = text
            close()
        }
    }

    protected fun initButtons() {
        add(TextElement(guiRenderer, text, background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER, scale = 1.5f)))
        add(TextElement(guiRenderer, subtext, background = null, properties = TextRenderProperties(HorizontalAlignments.CENTER)))
        add(SpacerElement(guiRenderer, Vec2(0, 30)))

        for (button in createButtons()) {
            add(button)
        }

        add(ButtonElement(guiRenderer, "Cancel") {
            close()
        })
    }

    companion object {
        val VALUE_COLOR = ChatColors.BLUE
    }
}
