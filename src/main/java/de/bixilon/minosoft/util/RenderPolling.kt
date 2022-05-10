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

package de.bixilon.minosoft.util

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.terminal.RunConfiguration

object RenderPolling {
    val RENDERING_LATCH = CountUpAndDownLatch(Int.MAX_VALUE shr 1)
    var rendering: Rendering? = null
    val ENABLED = RunConfiguration.OPEN_Gl_ON_FIRST_THREAD


    /**
     * Eventually polls rendering (if opengl context is forced on the main thread)
     */
    internal fun pollRendering() {
        if (!ENABLED) {
            return
        }
        check(Thread.currentThread() == Minosoft.MAIN_THREAD) { "Current thread is not the main thread!" }
        while (true) {
            RENDERING_LATCH.waitForChange()
            rendering?.start() ?: continue
            this.rendering = null
        }
    }
}
