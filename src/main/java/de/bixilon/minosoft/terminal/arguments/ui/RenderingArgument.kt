/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.terminal.arguments.ui

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.*
import com.github.ajalt.clikt.parameters.types.choice
import de.bixilon.minosoft.gui.rendering.RenderingOptions
import de.bixilon.minosoft.gui.rendering.system.base.RenderSystemFactory
import de.bixilon.minosoft.gui.rendering.system.window.WindowFactory
import de.bixilon.minosoft.terminal.arguments.AppliedArgument
import de.bixilon.minosoft.util.collections.MemoryOptions

class RenderingArgument : OptionGroup(), AppliedArgument {
    private val _disable by option("--disable_rendering").flag(default = RenderingOptions.disabled).deprecated("--disable_rendering is deprecated, use --no-rendering instead")
    val disable by option("--no-rendering").flag(default = RenderingOptions.disabled)
    val _noCursorCatch by option("--disable_cursor_catch").flag(default = !RenderingOptions.cursorCatch).deprecated("--disable_cursor_catch is deprecated, use --no-cursor-catch instead")
    val noCursorCatch by option("--no-cursor-catch").flag(default = !RenderingOptions.cursorCatch)
    val windowApi by option("--window-api").choice(choices = WindowFactory.FACTORIES.map { it.key }.toTypedArray()).convert { WindowFactory.FACTORIES[it] ?: fail("No such window api: $it") }.defaultLazy { WindowFactory.factory!! }
    val renderApi by option("--render-api").choice(choices = RenderSystemFactory.FACTORIES.map { it.key }.toTypedArray()).convert { RenderSystemFactory.FACTORIES[it] ?: fail("No such render api: $it") }.defaultLazy { RenderSystemFactory.factory!! }

    val noNativeMemory by option("--no-native-memory").flag(default = !MemoryOptions.native)


    override fun apply() {
        RenderingOptions.disabled = _disable || disable
        RenderingOptions.cursorCatch = !(_noCursorCatch || noCursorCatch)
        WindowFactory.factory = windowApi
        RenderSystemFactory.factory = renderApi

        MemoryOptions.native = !noNativeMemory
    }
}
