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

package de.bixilon.minosoft.gui.rendering.stats

import de.bixilon.minosoft.util.avg.LongAverage

class RenderStats : AbstractRenderStats {
    override val avgFrameTime: LongAverage = LongAverage(1L * 1000000000L) // 1 second * SECOND_SCALE
    override var totalFrames: Long = 0L
        private set

    private var lastFrameStartTime = -1L

    private var lastSmoothFPSCalculationTime = 0L

    override var smoothAvgFPS: Double = 0.0
        get() {
            val time = System.currentTimeMillis()
            if (time - lastSmoothFPSCalculationTime > 100) {
                field = avgFPS
                lastSmoothFPSCalculationTime = time
            }
            return field
        }
        private set

    override val avgFPS: Double
        get() {
            val avgFrameTime = avgFrameTime.avg

            return 1000000000L / avgFrameTime.toDouble()  // SECOND_SCALE
        }


    override fun startFrame() {
        lastFrameStartTime = System.nanoTime()
    }

    override fun endFrame() {
        val time = System.nanoTime()

        val delta = time - lastFrameStartTime

        avgFrameTime += delta


        totalFrames++
    }
}
