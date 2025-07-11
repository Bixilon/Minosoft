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

package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.SimplePoolRunnable
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.rendering.RenderConstants.UV_ADD
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes

object RenderUtil {

    fun RenderContext.pause() {
        val guiRenderer = renderer[GUIRenderer]?.gui ?: return
        guiRenderer.pause()
    }

    inline fun RenderContext.runAsync(crossinline runnable: () -> Unit) {
        DefaultThreadPool += SimplePoolRunnable(ThreadPool.Priorities.HIGHER) {
            try {
                runnable()
            } catch (error: Throwable) {
                window.cursorMode = CursorModes.NORMAL
                window.destroy()
                error.printStackTrace()
                Exception("Exception in rendering: ${session.id}", error).crash()
            }
        }
    }

    fun Vec2f.fixUVStart(): Vec2f {
        if (x < 1.0f - UV_ADD && x > 0.0f) x += UV_ADD
        if (y < 1.0f - UV_ADD && y > 0.0f) y += UV_ADD

        return this
    }

    fun Vec2f.fixUVEnd(): Vec2f {
        if (x < 1.0f && x > UV_ADD) x -= UV_ADD
        if (y < 1.0f && y > UV_ADD) y -= UV_ADD

        return this
    }
}
