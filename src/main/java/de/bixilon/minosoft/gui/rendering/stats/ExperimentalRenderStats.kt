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

import de.bixilon.kutil.avg._long.LongAverage
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.kutil.random.RandomUtil.nextInt
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.kutil.time.TimeUtil.now
import glm_.func.common.clamp
import java.util.*
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ExperimentalRenderStats : AbstractRenderStats {
    private val renderStats = RenderStats()
    private val random = Random()

    private val baseMultiplier = random.nextFloat(1.0f, 1.5f)
    private val baseJitter = random.nextInt(0, 20)

    override val avgFrameTime = LongAverage(1.seconds)
    override val avgDrawTime = LongAverage(1.seconds)

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
            val avgFPS = renderStats.avgFPS

            val multiplier = 3.0f * baseMultiplier * random.nextFloat(0.9f, 1.1f)

            var fps = avgFPS * multiplier

            fps += baseJitter

            fps += random.nextInt(-10, 10)

            return fps.clamp(0.0, 10000.0)
        }


    init {
        avgFrameTime.add(5000000L) // ToDo: Add real stats
        avgFrameTime.add(5000000L) // ToDo: Add real stats
    }

    override val totalFrames: Long
        get() = renderStats.totalFrames

    override fun startFrame() {
        renderStats.startFrame()
    }

    override fun endDraw() {
        renderStats.endDraw()
    }

    override fun endFrame() {
        renderStats.endFrame()
    }
}
