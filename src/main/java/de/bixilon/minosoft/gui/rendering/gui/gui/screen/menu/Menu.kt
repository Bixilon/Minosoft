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

import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.button.ButtonElement
import de.bixilon.minosoft.gui.rendering.gui.gui.screen.Screen
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import glm_.vec2.Vec2i

abstract class Menu(guiRenderer: GUIRenderer) : Screen(guiRenderer) {
    private val buttons: MutableList<ButtonElement> = mutableListOf()

    private var buttonWidth = -1
    private var totalHeight = -1

    private var activeButton: ButtonElement? = null

    override fun forceSilentApply() {
        buttonWidth = size.x / 3 // 1 left and right

        var totalHeight = 0
        for (button in buttons) {
            val currentButtonSize = button.size
            val buttonSize = Vec2i(buttonWidth, currentButtonSize.y)
            button.prefMaxSize = buttonSize
            button.size = buttonSize
            totalHeight += currentButtonSize.y
        }
        totalHeight += maxOf(0, (buttons.size - 1) * BUTTON_Y_MARGIN)
        this.totalHeight = totalHeight
        super.forceSilentApply()
        cacheUpToDate = false
    }

    fun addButton(button: ButtonElement) {
        button.parent = this
        buttons += button
        forceSilentApply()
    }

    override fun forceRender(offset: Vec2i, z: Int, consumer: GUIVertexConsumer, options: GUIVertexOptions?): Int {
        val size = size
        var zUsed = super.forceRender(offset, z, consumer, options)
        val startOffset = (size - Vec2i(buttonWidth, totalHeight)) / 2
        for (button in buttons) {
            zUsed = maxOf(zUsed, button.render(offset + startOffset, z + zUsed, consumer, options) + zUsed)
            startOffset.y += BUTTON_Y_MARGIN + button.size.y
        }
        return zUsed
    }

    override fun onMouseLeave() {
        activeButton?.onMouseLeave()
        activeButton = null
    }

    override fun onMouseMove(position: Vec2i) {
        val (delta, button) = getButtonAndPositionAt(position)

        if (activeButton != button) {
            activeButton?.onMouseLeave()
            button?.onMouseEnter(delta)
            activeButton = button
            return
        }
        button?.onMouseMove(delta)
    }

    override fun onMouseAction(position: Vec2i, button: MouseButtons, action: MouseActions) {
        val (delta, buttonElement) = getButtonAndPositionAt(position)
        buttonElement?.onMouseAction(delta, button, action)
    }

    override fun onChildChange(child: Element) {
        forceSilentApply()
    }

    fun getButtonAndPositionAt(position: Vec2i): Pair<Vec2i, ButtonElement?> {
        var delta = position
        var button: ButtonElement? = null
        if (position.x in buttonWidth..buttonWidth * 2) {
            // x matches
            val yStart = (size.y - totalHeight) / 2
            var yOffset = position.y - yStart
            for (buttonEntry in buttons) {
                if (yOffset < 0) {
                    break
                }
                val buttonSize = buttonEntry.size
                if (yOffset < buttonSize.y) {
                    button = buttonEntry
                    break
                }
                yOffset -= buttonSize.y
                yOffset -= BUTTON_Y_MARGIN
            }
            delta = Vec2i(position.x - buttonWidth, yOffset)
        }

        return Pair(delta, button)
    }

    private companion object {
        const val BUTTON_Y_MARGIN = 5
    }
}
