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

package de.bixilon.minosoft.gui.rendering.gui.gui.dragged

import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseActions
import de.bixilon.minosoft.gui.rendering.gui.input.mouse.MouseButtons
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.input.count.MouseClickCounter
import de.bixilon.minosoft.gui.rendering.renderer.drawable.AsyncDrawable
import de.bixilon.minosoft.gui.rendering.renderer.drawable.Drawable
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class DraggedManager(
    private val guiRenderer: GUIRenderer,
) : Initializable, InputHandler, AsyncDrawable, Drawable {
    private val clickCounter = MouseClickCounter()
    var element: DraggedGUIElement<*>? = null
        set(value) {
            if (field == value || field?.element == value?.element) {
                return
            }
            val position = guiRenderer.currentMousePosition
            val previous = field
            previous?.element?.onDragEnd(position, guiRenderer.gui.onDragMove(Vec2i(-1, -1), previous.element))

            field = value
            if (value == null) {
                guiRenderer.gui.onMouseMove(position)
            } else {
                guiRenderer.gui.onMouseMove(Vec2i(-1, -1)) // move mouse out
                value.element.onDragStart(position, guiRenderer.gui.onDragMove(position, value.element))
            }
            applyCursor()
        }
    private var lastTickTime: Long = -1L

    override fun init() {
    }

    override fun postInit() {
    }

    fun onMatrixChange() {
        element?.element?.forceSilentApply()
    }

    private fun applyCursor() {
        val window = guiRenderer.renderWindow.window
        if (window.cursorMode == CursorModes.DISABLED) {
            return
        }
        window.cursorMode = if (element == null) CursorModes.NORMAL else CursorModes.HIDDEN
    }

    override fun drawAsync() {
        val element = element ?: return
        val time = TimeUtil.millis
        val tick = time - lastTickTime > ProtocolDefinition.TICK_TIME
        if (tick) {
            lastTickTime = time
        }
        if (!element.enabled) {
            return
        }
        if (tick) {
            element.tick()

            lastTickTime = time
        }

        if (!element.skipDraw) {
            element.draw()
        }
        element.prepare()
        element.prepareAsync()
    }

    override fun draw() {
        val element = element ?: return
        if (!element.enabled) {
            return
        }
        element.postPrepare()

        guiRenderer.setup()
        if (element.mesh.data.isEmpty) {
            return
        }
        element.mesh.draw()
    }

    override fun onCharPress(char: Int): Boolean {
        val element = element ?: return false
        val target = guiRenderer.gui.onDragChar(char, element.element)
        element.element.onDragChar(char.toChar(), target)
        return true
    }

    override fun onMouseMove(position: Vec2i): Boolean {
        element?.onMouseMove(position) ?: return false
        return true
    }

    override fun onKey(type: KeyChangeTypes, key: KeyCodes): Boolean {
        val element = element ?: return false
        val target = guiRenderer.gui.onDragKey(type, key, element.element)
        val mouseButton = MouseButtons[key]
        if (mouseButton == null) {
            element.element.onDragKey(key, type, target)
            return true
        }

        val mouseAction = MouseActions[type] ?: return false

        element.element.onDragMouseAction(guiRenderer.currentMousePosition, mouseButton, mouseAction, clickCounter.getClicks(mouseButton, mouseAction, guiRenderer.currentMousePosition, TimeUtil.millis), target)
        return true
    }

    override fun onScroll(scrollOffset: Vec2d): Boolean {
        val element = element ?: return false
        val target = guiRenderer.gui.onDragScroll(scrollOffset, element.element)
        element.element.onDragScroll(guiRenderer.currentMousePosition, scrollOffset, target)
        return true
    }
}
