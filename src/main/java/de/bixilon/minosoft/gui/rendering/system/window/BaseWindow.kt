/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.window

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import glm_.vec2.Vec2
import glm_.vec2.Vec2i

interface BaseWindow {
    var size: Vec2i
    val sizef: Vec2
        get() = Vec2(size)
    var minSize: Vec2i
    var maxSize: Vec2i

    var visible: Boolean
    var resizable: Boolean

    var swapInterval: Int

    var cursorMode: CursorModes


    var clipboardText: String
    var title: String
    val version: String

    val time: Double

    fun init() {
        resizable = true
        swapInterval = Minosoft.config.config.game.other.swapInterval

        if (!StaticConfiguration.DEBUG_MODE) {
            cursorMode = CursorModes.DISABLED
        }
        size = DEFAULT_WINDOW_SIZE
        minSize = DEFAULT_MINIMUM_WINDOW_SIZE
        maxSize = DEFAULT_MAXIMUM_WINDOW_SIZE
    }

    fun destroy()

    fun close()

    fun swapBuffers()

    fun pollEvents()

    companion object {
        val DEFAULT_WINDOW_SIZE: Vec2i
            get() = Vec2i(900, 500)
        val DEFAULT_MINIMUM_WINDOW_SIZE: Vec2i
            get() = Vec2i(100, 100)
        val DEFAULT_MAXIMUM_WINDOW_SIZE: Vec2i
            get() = Vec2i(-1, -1)
    }
}
