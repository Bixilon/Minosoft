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

import de.bixilon.minosoft.gui.rendering.gui.gui.GUIMeshElement
import glm_.vec2.Vec2i

class DraggedGUIElement<T : Dragged>(element: T) : GUIMeshElement<T>(element) {

    override fun prepare() {
        prepare(guiRenderer.currentMousePosition - (element.size / 2))
    }

    override fun onMouseMove(position: Vec2i): Boolean {
        val element = guiRenderer.gui.onDragMove(position, element)
        this.element.onDragMove(position, element)
        return element != null
    }
}