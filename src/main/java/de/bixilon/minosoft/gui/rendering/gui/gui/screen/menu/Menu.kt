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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu

import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.gui.AbstractLayout
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.Screen
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY
import glm_.vec2.Vec2d
import glm_.vec2.Vec2i

abstract class Menu(
    guiRenderer: GUIRenderer,
    val preferredElementWidth: Int = 150,
) : Screen(guiRenderer), AbstractLayout<Element> {
    private val elements: MutableList<Element> = mutableListOf()

    private var maxElementWidth = -1
    private var totalHeight = -1

    override var activeElement: Element? = null
    override var activeDragElement: Element? = null

    override fun forceSilentApply() {
        val elementWidth = maxOf(minOf(preferredElementWidth, size.x / 3), 0)
        var maxElementWidth = elementWidth

        var totalHeight = 0
        for (element in elements) {
            val currentElementSize = element.size
            val elementSize = Vec2i(elementWidth, currentElementSize.y)
            element.size = elementSize
            maxElementWidth = maxOf(maxElementWidth, element.size.x) // width may not be changeable
            totalHeight += currentElementSize.y
        }
        this.maxElementWidth = maxElementWidth
        totalHeight += maxOf(0, (elements.size - 1) * BUTTON_Y_MARGIN)
        this.totalHeight = totalHeight
        super.forceSilentApply()
        cacheUpToDate = false
    }

    fun add(element: Element) {
        element.parent = this
        elements += element
        forceSilentApply()
    }

    operator fun plusAssign(element: Element) = add(element)

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        super.forceRender(offset, consumer, options)
        val maxElementWidth = maxElementWidth
        val startOffset = (size - Vec2i(maxElementWidth, totalHeight)) / 2
        for (element in elements) {
            element.render(offset + startOffset + Vec2i((maxElementWidth - element.size.x) / 2, 0), consumer, options)
            startOffset.y += BUTTON_Y_MARGIN + element.size.y
        }
    }

    override fun onMouseEnter(position: Vec2i, absolute: Vec2i): Boolean {
        super<AbstractLayout>.onMouseEnter(position, absolute)
        return true
    }

    override fun onMouseMove(position: Vec2i, absolute: Vec2i): Boolean {
        super<AbstractLayout>.onMouseMove(position, absolute)
        return true
    }

    override fun onMouseLeave(): Boolean {
        super<Screen>.onMouseLeave()
        return true
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions): Boolean {
        val (element, delta) = getAt(position) ?: return true
        element.onMouseAction(delta, button, action)
        return true
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
    }

    override fun getAt(position: Vec2i): Pair<Element, Vec2i>? {
        var element: Element? = null
        val delta = Vec2i(position)
        val elementWidth = maxElementWidth
        val size = size
        val xStart = (size.x - elementWidth) / 2
        if (position.x < xStart || position.x >= xStart + elementWidth) {
            return null
        }
        delta.x = position.x - xStart
        // x matches
        val yStart = (size.y - totalHeight) / 2
        var yOffset = position.y - yStart
        for (buttonEntry in elements) {
            if (yOffset < 0) {
                break
            }
            val buttonSize = buttonEntry.size
            if (yOffset < buttonSize.y) {
                element = buttonEntry
                break
            }
            yOffset -= buttonSize.y
            yOffset -= BUTTON_Y_MARGIN
        }
        delta.y = yOffset

        if (element == null) {
            return null
        }

        val width = element.size.x
        val halfWidthDelta = (elementWidth - width) / 2
        delta.x -= halfWidthDelta
        if (delta.x < 0 || delta.x >= width) {
            return null
        }

        return Pair(element, delta)
    }

    override fun tick() {
        for (element in elements) {
            element.tick()
        }
        super.tick()
    }

    override fun onKey(key: KeyCodes, type: KeyChangeTypes): Boolean {
        if (type != KeyChangeTypes.RELEASE && key == KeyCodes.KEY_TAB) {
            var element: Element?
            var initialIndex = elements.indexOf(activeElement)
            if (initialIndex == -1) {
                initialIndex = 0
            }
            var index = initialIndex
            while (true) {
                index++
                if (index >= elements.size) {
                    index = 0
                }
                if (index == initialIndex) {
                    return true
                }
                element = elements.getOrNull(index) ?: return true
                if (element.canFocus) {
                    break
                }
            }
            if (element == null) {
                return true
            }

            activeElement?.onMouseLeave()
            element.onMouseEnter(Vec2i.EMPTY, Vec2i.EMPTY)
            activeElement = element
            return true // no passthrough the key to current active element
        }
        activeElement?.onKey(key, type)

        return true
    }

    override fun onCharPress(char: Int): Boolean {
        activeElement?.onCharPress(char)
        return true
    }

    override fun onScroll(position: Vec2i, scrollOffset: Vec2d): Boolean {
        val (element, delta) = getAt(position) ?: return true
        element.onScroll(delta, scrollOffset)
        return true
    }

    private fun reset() {
        for (element in elements) {
            element.onClose()
        }
        activeElement?.onMouseLeave()
        activeElement = null
    }

    override fun onOpen() {
        super.onOpen()
        reset()
    }

    override fun onHide() {
        super.onHide()
        reset()
    }

    override fun onClose() {
        super.onClose()
        reset()
    }

    private companion object {
        const val BUTTON_Y_MARGIN = 5
    }
}
