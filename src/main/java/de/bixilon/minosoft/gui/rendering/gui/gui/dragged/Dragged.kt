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

package de.bixilon.minosoft.gui.rendering.gui.gui.dragged

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i

abstract class Dragged(guiRenderer: GUIRenderer) : Element(guiRenderer) {
    val visible: Boolean
        get() = guiRenderer.dragged.element?.element === this


    open fun onDragStart(position: Vec2i, target: Element?) = Unit
    open fun onDragMove(position: Vec2i, target: Element?) = Unit
    open fun onDragEnd(position: Vec2i, target: Element?) = Unit

    open fun onDragScroll(position: Vec2i, scrollOffset: Vec2d, target: Element?) = Unit

    open fun onDragMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions, target: Element?) = Unit
    open fun onDragKey(key: KeyCodes, type: KeyChangeTypes, target: Element?) = Unit
    open fun onDragChar(char: Char, target: Element?) = Unit

    fun show() {
        if (visible) {
            return
        }
        guiRenderer.dragged.element = DraggedGUIElement(this)
    }

    fun close() {
        if (!visible) {
            return
        }
        guiRenderer.dragged.element = null
    }
}
