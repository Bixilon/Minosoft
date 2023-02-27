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

package de.bixilon.minosoft.data.text.events.hover

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.gui.popper.text.TextPopper
import javafx.scene.text.Text

class TextHoverEvent(
    text: Any?,
) : HoverEvent {
    val text = ChatComponent.of(text)
    private var popper: TextPopper? = null

    override fun applyJavaFX(text: Text) {
        text.accessibleText = this.text.message
    }

    override fun onMouseEnter(guiRenderer: GUIRenderer, position: Vec2i, absolute: Vec2i): Boolean {
        val popper = TextPopper(guiRenderer, absolute, text)
        popper.show()
        this.popper = popper
        return true
    }

    override fun onMouseLeave(guiRenderer: GUIRenderer): Boolean {
        this.popper?.let { guiRenderer.popper -= it }
        this.popper = null
        return true
    }

    override fun hashCode(): Int {
        return text.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is TextHoverEvent) return false
        return text == other.text
    }

    companion object : HoverEventFactory<TextHoverEvent> {
        override val name: String = "show_text"

        override fun build(json: JsonObject, restrictedMode: Boolean): TextHoverEvent {
            return TextHoverEvent(ChatComponent.of(json.data))
        }
    }
}
