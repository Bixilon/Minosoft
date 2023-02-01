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

package de.bixilon.minosoft.gui.rendering

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer

object RenderUtil {

    fun RenderContext.pause(pause: Boolean? = null) {
        val guiRenderer = renderer[GUIRenderer]?.gui ?: return
        if (pause == null) {
            guiRenderer.pause()
        } else {
            guiRenderer.pause(pause)
        }
    }

    fun RenderContext.runAsync(runnable: () -> Unit) {
        DefaultThreadPool += ThreadPoolRunnable(ThreadPool.HIGHER, forcePool = false) {
            try {
                runnable()
            } catch (error: Throwable) {
                error.printStackTrace()
                Exception("Exception in rendering: ${connection.connectionId}", error).crash()
            }
        }
    }
}
