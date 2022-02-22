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

package de.bixilon.minosoft.gui.rendering.gui.gui

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreater
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isSmaller
import glm_.vec2.Vec2i

class LayoutedGUIElement<T : LayoutedElement>(
    val layout: T,
) : GUIMeshElement<Element>(layout.unsafeCast()) {
    protected var lastDragPosition: Vec2i? = null

    override fun prepare() {
        prepare(layout.layoutOffset)
    }

    private fun getOffset(position: Vec2i): Vec2i? {
        val layoutOffset = layout.layoutOffset
        val size = element.size
        if (position isSmaller layoutOffset) {
            return null
        }
        val offset = position - layoutOffset
        if (offset isGreater size) {
            return null
        }
        return offset
    }

    override fun onMouseMove(position: Vec2i): Boolean {
        val lastPosition = lastPosition
        val offset = getOffset(position)
        if (offset == null) {
            if (lastPosition == null) {
                return false
            }
            // move out
            this.lastPosition = null
            return element.onMouseLeave()
        }

        val previousOutside = lastPosition == null
        this.lastPosition = offset

        if (previousOutside) {
            return element.onMouseEnter(offset, position)
        }

        return element.onMouseMove(offset, position)
    }

    override fun onDragMove(position: Vec2i, draggable: Dragged): Element? {
        val lastDragPosition = lastDragPosition
        val offset = getOffset(position)
        if (offset == null) {
            if (this.lastDragPosition == null) {
                return null
            }
            // move out
            this.lastDragPosition = null
            return element.onDragLeave(draggable)
        }

        val previousOutside = lastDragPosition == null
        this.lastDragPosition = offset

        if (previousOutside) {
            return element.onDragEnter(offset, position, draggable)
        }

        return element.onDragMove(offset, position, draggable)
    }

    override fun onDragLeave(draggable: Dragged): Element? {
        var element: Element? = null
        if (lastDragPosition != null) {
            element = this.element.onDragLeave(draggable)
        }
        lastDragPosition = null
        return element
    }

    override fun onDragSuccess(draggable: Dragged): Element? {
        var element: Element? = null
        if (lastDragPosition != null) {
            element = this.element.onDragSuccess(draggable)
        }
        lastDragPosition = null
        return element
    }
}
