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

package de.bixilon.minosoft.gui.rendering.input.key.manager

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.config.key.KeyCodes.Companion.isPrintable
import de.bixilon.minosoft.gui.rendering.input.InputHandler
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes
import de.bixilon.minosoft.gui.rendering.system.window.KeyChangeTypes

class InputHandlerManager(
    val input: InputManager,
) {
    private val context = input.context
    var handler: InputHandler? = null
        set(value) {
            if (field == value) {
                return
            }
            val previous = field
            field = value
            if ((previous == null) == (value == null)) return
            if (previous == null) {
                skipMouse = true
            }

            if (value == null) {
                disable()
            } else {
                enable()
            }
        }
    private var skipChar = false
    private var skipMouse = false
    private var skipKey = false


    fun onMouse(position: Vec2): Boolean {
        if (skipMouse) {
            skipMouse = false
            return true
        }
        val handler = this.handler ?: return false

        handler.onMouseMove(position)
        return true
    }

    fun onKey(code: KeyCodes, change: KeyChangeTypes) {
        if (skipKey) {
            skipKey = false
            return
        }
        val handler = this.handler ?: return
        handler.onKey(code, change)
    }

    fun onChar(char: Int) {
        if (skipChar) {
            skipChar = false
            return
        }
        val handler = this.handler ?: return
        handler.onCharPress(char)
    }

    fun onScroll(delta: Vec2): Boolean {
        val handler = this.handler ?: return false
        handler.onScroll(delta)

        return true
    }


    private fun enable() {
        context.window.cursorMode = CursorModes.NORMAL
        input.clear()
    }

    private fun disable() {
        context.window.cursorMode = CursorModes.DISABLED
    }

    fun checkSkip(code: KeyCodes, pressed: Boolean, previous: InputHandler?) {
        val next = handler
        if (next == null || previous == next) return

        if (pressed) {
            this.skipKey = true
        }
        if (code.isPrintable()) {
            this.skipChar = true
        }
    }
}
