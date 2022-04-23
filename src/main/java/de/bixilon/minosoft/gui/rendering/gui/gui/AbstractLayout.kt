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

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments
import de.bixilon.minosoft.gui.rendering.gui.elements.HorizontalAlignments.Companion.getOffset
import de.bixilon.minosoft.gui.rendering.gui.gui.dragged.Dragged
import de.bixilon.minosoft.gui.rendering.gui.input.DragTarget
import de.bixilon.minosoft.gui.rendering.gui.input.InputElement
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes

interface AbstractLayout<T : Element> : InputElement, DragTarget {
    var activeElement: T?
    var activeDragElement: T?

    fun getAt(position: Vec2i): Pair<T, Vec2i>?

    override fun onMouseEnter(position: Vec2i, absolute: Vec2i): Boolean {
        val pair = getAt(position)
        activeElement = pair?.first
        return pair?.first?.onMouseEnter(pair.second, absolute) ?: false
    }

    override fun onMouseMove(position: Vec2i, absolute: Vec2i): Boolean {
        val pair = getAt(position)

        if (activeElement != pair?.first) {
            val activeElement = activeElement
            this.activeElement = pair?.first

            // Don't put this in the return line, compiler optimizations break it.
            val leaveConsumed = activeElement?.onMouseLeave() ?: false
            val enterConsumed = pair?.first?.onMouseEnter(pair.second, absolute) ?: false
            return leaveConsumed || enterConsumed
        }
        return pair?.first?.onMouseMove(pair.second, absolute) ?: false
    }

    override fun onMouseLeave(): Boolean {
        val activeElement = activeElement
        this.activeElement = null
        return activeElement?.onMouseLeave() ?: false
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        val (element, offset) = getAt(position) ?: return false
        return element.onMouseAction(offset, button, action, count)
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        return activeElement?.onKey(key, type) ?: false
    }

    override fun onCharPress(char: Int): Boolean {
        return activeElement?.onCharPress(char) ?: false
    }

    override fun onScroll(position: Vec2i, scrollOffset: Vec2d): Boolean {
        val (element, offset) = getAt(position) ?: return false
        return element.onScroll(offset, scrollOffset)
    }

    override fun onDragEnter(position: Vec2i, absolute: Vec2i, draggable: Dragged): Element? {
        val pair = getAt(position)
        this.activeDragElement = pair?.first
        return pair?.first?.onDragEnter(pair.second, absolute, draggable)
    }

    override fun onDragMove(position: Vec2i, absolute: Vec2i, draggable: Dragged): Element? {
        val pair = getAt(position)

        if (activeDragElement != pair?.first) {
            val activeDragElement = activeDragElement
            this.activeDragElement = pair?.first

            // Don't put this in the return line, compiler optimizations break it.
            val leaveElement = activeDragElement?.onDragLeave(draggable)
            val enterElement = pair?.first?.onDragEnter(pair.second, absolute, draggable)
            return enterElement ?: leaveElement
        }
        return pair?.first?.onDragMove(pair.second, absolute, draggable)
    }

    override fun onDragLeave(draggable: Dragged): Element? {
        val activeDragElement = this.activeDragElement
        this.activeDragElement = null
        return activeDragElement?.onDragLeave(draggable)
    }

    override fun onDragScroll(position: Vec2i, scrollOffset: Vec2d, draggable: Dragged): Element? {
        val (element, offset) = getAt(position) ?: return null
        return element.onDragScroll(offset, scrollOffset, draggable)
    }

    override fun onDragMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions, count: Int, draggable: Dragged): Element? {
        val (element, offset) = getAt(position) ?: return null
        return element.onDragMouseAction(offset, button, action, count, draggable)
    }

    override fun onDragKey(key: KeyCodes, type: KeyChangeTypes, draggable: Dragged): Element? {
        return activeDragElement?.onDragKey(key, type, draggable)
    }

    override fun onDragChar(char: Char, draggable: Dragged): Element? {
        return activeDragElement?.onDragChar(char, draggable)
    }


    companion object {

        fun Element.getAtCheck(position: Vec2i, element: Element, horizontalAlignments: HorizontalAlignments = HorizontalAlignments.LEFT, modifyY: Boolean = false): Pair<Element, Vec2i>? {
            if (position.x < 0 || position.y < 0) {
                return null
            }
            val size = element.size
            if (position.y >= size.y) {
                if (modifyY) {
                    position.y -= size.y
                }
                return null
            }
            val offset = horizontalAlignments.getOffset(this.size.x, size.x)
            if (position.x < offset) {
                return null
            }
            val xPosition = position.x - offset
            if (xPosition >= size.x) {
                return null
            }
            return Pair(element, Vec2i(xPosition, position.y))
        }
    }
}
