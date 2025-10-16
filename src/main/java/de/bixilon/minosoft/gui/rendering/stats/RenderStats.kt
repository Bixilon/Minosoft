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

package de.bixilon.minosoft.gui.rendering.stats

import de.bixilon.kutil.avg.duration.DurationAverage
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.time.TimeUtil.now
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class RenderStats : AbstractRenderStats {
    override val avgDrawTime = DurationAverage(1.seconds, Duration.INFINITE)
    override val avgFrameTime = DurationAverage(1.seconds, Duration.INFINITE)
    override var totalFrames: Long = 0L
        private set

    private var lastFrameStartTime = TimeUtil.NULL

    private var lastSmoothFPSCalculationTime = TimeUtil.NULL

    override var smoothAvgFPS: Double = 0.0
        get() {
            val time = now()
            if (time - lastSmoothFPSCalculationTime > 100.milliseconds) {
                field = avgFPS
                lastSmoothFPSCalculationTime = time
            }
            return field
        }
        private set

    override val avgFPS: Double
        get() {
            val avgFrameTime = avgFrameTime.avg

            return 1.seconds / avgFrameTime
        }


    override fun startFrame() {
        lastFrameStartTime = now()
    }

    override fun endFrame() {
        val time = now()

        val delta = time - lastFrameStartTime

        avgFrameTime += delta

        totalFrames++
    }

    override fun endDraw() {
        val time = now()
        val delta = time - lastFrameStartTime

        avgDrawTime += delta
    }
}
