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
import com.github.ajalt.clikt.parameters.options.deprecated
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
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
    val windowApi by option("--window-api").choice(choices = WindowFactory.factories.map { it.key }.toTypedArray() + NONE)
    val renderApi by option("--render-api").choice(choices = RenderSystemFactory.factories.map { it.key }.toTypedArray() + NONE)

    val noNativeMemory by option("--no-native-memory").flag(default = !MemoryOptions.native)
    val profileFrames by option("--profile-frames").flag(default = RenderingOptions.profileFrames)
    val debugGpuMemoryLeaks by option("--debug-gpu-memory-leaks").flag(default = RenderingOptions.debugGpuMemoryLeaks)


    override fun apply() {
        RenderingOptions.disabled = _disable || disable
        RenderingOptions.cursorCatch = !(_noCursorCatch || noCursorCatch)
        windowApi?.let { WindowFactory.factory = if (it == NONE) null else (WindowFactory.factories[it] ?: throw IllegalArgumentException("Can not find window api: $it")) }
        renderApi?.let { RenderSystemFactory.factory = if (it == NONE) null else (RenderSystemFactory.factories[it] ?: throw IllegalArgumentException("Can not find render system: $it")) }

        MemoryOptions.native = !noNativeMemory
        RenderingOptions.profileFrames = profileFrames
        RenderingOptions.debugGpuMemoryLeaks = debugGpuMemoryLeaks
    }

    companion object {
        const val NONE = "none"
    }
}
