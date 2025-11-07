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

import de.bixilon.kmath.vec.vec2.f.MVec2f
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.SimplePoolRunnable
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.rendering.RenderConstants.UV_PRECISION_CORRECTION
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.system.window.CursorModes

object RenderUtil {

    fun RenderContext.pause() {
        val guiRenderer = renderer[GUIRenderer]?.gui ?: return
        guiRenderer.pause()
    }

    inline fun RenderContext.runAsync(profile: String? = null, crossinline runnable: () -> Unit) {
        DefaultThreadPool += SimplePoolRunnable(ThreadPool.Priorities.HIGHER) {
            try {
                if (profile != null) {
                    profiler.profile(profile) { runnable.invoke() }
                } else {
                    runnable()
                }
            } catch (error: Throwable) {
                window.cursorMode = CursorModes.NORMAL
                error.printStackTrace()
                this.session.error = error
                Exception("Exception in rendering: ${session.id}", error).crash()
            }
        }
    }

    fun MVec2f.fixUVStart() {
        if (x < 1.0f - UV_PRECISION_CORRECTION && x > 0.0f) x += UV_PRECISION_CORRECTION
        if (y < 1.0f - UV_PRECISION_CORRECTION && y > 0.0f) y += UV_PRECISION_CORRECTION
    }

    fun MVec2f.fixUVEnd() {
        if (x < 1.0f && x > UV_PRECISION_CORRECTION) x -= UV_PRECISION_CORRECTION
        if (y < 1.0f && y > UV_PRECISION_CORRECTION) y -= UV_PRECISION_CORRECTION
    }

    fun RenderContext.unload() {
        renderer.unload()
        models.unload()
        skeletal.unload()

        light.unload()
        textures.unload()

        queue.work()
    }
}
