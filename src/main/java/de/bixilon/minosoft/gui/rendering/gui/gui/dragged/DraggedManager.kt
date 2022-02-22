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

import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.gui.rendering.gui.GUIElementDrawer
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.hud.Initializable
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec2.Vec2i

class DraggedManager(
    override val guiRenderer: GUIRenderer,
) : Initializable, InputHandler, GUIElementDrawer {
    var element: DraggedGUIElement<*>? = null
        set(value) {
            if (field == value || field?.element == value?.element) {
                return
            }
            val position = guiRenderer.currentCursorPosition
            field?.element?.onDragEnd(position, null) // ToDo
            field = value
            element?.element?.onDragStart(position, null) // ToDo
            applyCursor()
        }
    override var lastTickTime: Long = -1L

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

    fun draw() {
        val element = element ?: return
        val time = TimeUtil.time
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

        guiRenderer.setup()
        if (!element.enabled || element.mesh.data.isEmpty) {
            return
        }
        element.mesh.draw()
    }

    override fun onCharPress(char: Int): Boolean {
        element?.onCharPress(char) ?: return false
        return true
    }

    override fun onMouseMove(position: Vec2i): Boolean {
        element?.onMouseMove(position) ?: return false
        return true
    }

    override fun onKeyPress(type: KeyChangeTypes, key: KeyCodes): Boolean {
        element?.onKeyPress(type, key) ?: return false
        return true
    }
}
