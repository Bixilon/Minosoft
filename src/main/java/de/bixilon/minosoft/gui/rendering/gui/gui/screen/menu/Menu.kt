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

package de.bixilon.minosoft.gui.rendering.gui.gui.screen.menu

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
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
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util.EMPTY

abstract class Menu(
    guiRenderer: GUIRenderer,
    val preferredElementWidth: Float = 150.0f,
) : Screen(guiRenderer), AbstractLayout<Element> {
    private val elements: MutableList<Element> = mutableListOf()

    private var maxElementWidth = -1.0f
    private var totalHeight = -1.0f

    override var activeElement: Element? = null
    override var activeDragElement: Element? = null

    init {
        super.construct()
    }

    override fun update() {
        val elementWidth = maxOf(minOf(preferredElementWidth, size.x / 3), 0.0f)
        var maxElementWidth = elementWidth

        var totalHeight = 0.0f
        for (element in elements) {
            val currentElementSize = element.size
            val elementSize = Vec2(elementWidth, currentElementSize.y)
            element.preferredSize = elementSize
            maxElementWidth = maxOf(maxElementWidth, element.size.x) // width may not be changeable
            totalHeight += currentElementSize.y
        }
        this.maxElementWidth = maxElementWidth
        totalHeight += maxOf(0.0f, (elements.size - 1) * BUTTON_Y_MARGIN)
        this.totalHeight = totalHeight
        super.update()
    }

    fun add(element: Element) {
        element.parent = this
        elements += element
        invalidate()
    }

    operator fun plusAssign(element: Element) = add(element)

    override fun forceRender(offset: Vec2, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        val size = size
        super.forceRender(offset, consumer, options)
        val maxElementWidth = maxElementWidth
        val startOffset = (size - Vec2i(maxElementWidth, totalHeight)) / 2
        for (element in elements) {
            element.render(offset + startOffset + Vec2((maxElementWidth - element.size.x) / 2, 0), consumer, options)
            startOffset.y += BUTTON_Y_MARGIN + element.size.y
        }
    }

    override fun onMouseEnter(position: Vec2, absolute: Vec2): Boolean {
        super<AbstractLayout>.onMouseEnter(position, absolute)
        return true
    }

    override fun onMouseMove(position: Vec2, absolute: Vec2): Boolean {
        super<AbstractLayout>.onMouseMove(position, absolute)
        return true
    }

    override fun onMouseLeave(): Boolean {
        super<Screen>.onMouseLeave()
        return true
    }

    override fun onMouseAction(position: Vec2, button: MouseButtons, action: MouseActions, count: Int): Boolean {
        val (element, delta) = getAt(position) ?: return true
        element.onMouseAction(delta, button, action, count)
        return true
    }

    override fun getAt(position: Vec2): Pair<Element, Vec2>? {
        var element: Element? = null
        val delta = Vec2(position)
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
            element.onMouseEnter(Vec2.EMPTY, Vec2.EMPTY)
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

    override fun onScroll(position: Vec2, scrollOffset: Vec2): Boolean {
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
        const val BUTTON_Y_MARGIN = 5.0f
    }

    override fun construct() = Unit
}
