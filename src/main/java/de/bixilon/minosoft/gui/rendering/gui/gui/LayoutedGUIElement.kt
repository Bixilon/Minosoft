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

package de.bixilon.minosoft.gui.rendering.gui.gui

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.LayoutedElement
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isGreater
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.isSmaller

open class LayoutedGUIElement<T : LayoutedElement>(
    val layout: T,
) : GUIMeshElement<Element>(layout.unsafeCast()) {

    override fun prepare() {
        prepare(layout.layoutOffset)
    }

    protected open fun getOffset(position: Vec2i): Vec2i? {
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
        this.lastPosition = Vec2i(offset)

        if (previousOutside) {
            return element.onMouseEnter(offset, position)
        }

        return element.onMouseMove(offset, position)
    }

    override fun onDragMove(position: Vec2i, dragged: Dragged): Element? {
        val lastDragPosition = lastDragPosition
        val offset = getOffset(position)
        if (offset == null) {
            if (this.lastDragPosition == null) {
                return null
            }
            // move out
            this.lastDragPosition = null
            return element.onDragLeave(dragged)
        }

        val previousOutside = lastDragPosition == null
        this.lastDragPosition = Vec2i(offset)

        if (previousOutside) {
            return element.onDragEnter(offset, position, dragged)
        }

        return element.onDragMove(offset, position, dragged)
    }
}
