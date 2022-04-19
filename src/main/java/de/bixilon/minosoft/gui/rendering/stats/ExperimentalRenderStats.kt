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

package de.bixilon.minosoft.gui.rendering.stats

import de.bixilon.kotlinglm.func.common.clamp
import de.bixilon.kutil.avg.Average
import de.bixilon.kutil.avg.LongAverage
import de.bixilon.kutil.random.RandomUtil.nextFloat
import de.bixilon.kutil.time.TimeUtil
import kotlin.random.Random

class ExperimentalRenderStats : AbstractRenderStats {
    private val renderStats = RenderStats()

    private val baseMultiplier = Random.nextFloat(1.0f, 1.5f)
    private val baseJitter = Random.nextInt(0, 20)

    override val avgFrameTime: Average<Long> = LongAverage(Long.MAX_VALUE)

    private var lastSmoothFPSCalculationTime = 0L
    override var smoothAvgFPS: Double = 0.0
        get() {
            val time = TimeUtil.time
            if (time - lastSmoothFPSCalculationTime > 100) {
                field = avgFPS
                lastSmoothFPSCalculationTime = time
            }
            return field
        }
        private set

    override val avgFPS: Double
        get() {
            val avgFPS = renderStats.avgFPS

            val multiplier = 3.0f * baseMultiplier * Random.nextFloat(0.9f, 1.1f)

            var fps = avgFPS * multiplier

            fps += baseJitter

            fps += Random.nextInt(-10, 10)

            return fps.clamp(0.0, 10000.0)
        }


    init {
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
