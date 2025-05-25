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

package de.bixilon.minosoft.gui.rendering.gui.elements.text.fade

import kotlin.time.TimeSource.Monotonic.ValueTimeMark
import kotlin.time.Duration

interface FadePhase {
    val times: FadingTimes

    fun isDone(time: ValueTimeMark): Boolean
    fun getAlpha(time: ValueTimeMark): Float

    fun next(time: ValueTimeMark): FadePhase?
    fun stop(time: ValueTimeMark): FadePhase? = next(time)

    class In(
        override val times: FadingTimes,
        private val start: ValueTimeMark,
    ) : FadePhase {
        private val end = start + times.`in`

        private fun getProgress(time: ValueTimeMark) = ((time - start) / times.`in`).toFloat()

        override fun isDone(time: ValueTimeMark) = time > end
        override fun getAlpha(time: ValueTimeMark) = getProgress(time)

        override fun next(time: ValueTimeMark): FadePhase? {
            if (times.stay > Duration.ZERO) return Stay(times, time)
            if (times.out > Duration.ZERO) return Out(times, time)

            return null
        }

        override fun stop(time: ValueTimeMark): FadePhase? {
            if (times.out <= Duration.ZERO) return null
            val progress = getProgress(time)
            if (progress == 0.0f) return null

            return Out(times, time) // TODO: set progress
        }
    }

    class Stay(
        override val times: FadingTimes,
        private val start: ValueTimeMark,
    ) : FadePhase {
        private val end = start + times.stay

        override fun isDone(time: ValueTimeMark) = time > end
        override fun getAlpha(time: ValueTimeMark) = 1.0f

        override fun next(time: ValueTimeMark): FadePhase? {
            if (times.out > Duration.ZERO) return Out(times, time)

            return null
        }
    }

    class Out(
        override val times: FadingTimes,
        private val start: ValueTimeMark,
    ) : FadePhase {
        private val end = start + times.out
        override fun getAlpha(time: ValueTimeMark) = 1.0f - ((time - start) / times.out).toFloat()

        override fun isDone(time: ValueTimeMark) = time > end

        override fun next(time: ValueTimeMark): FadePhase? = null
    }


    companion object {

        fun FadingTimes.createPhase(time: ValueTimeMark): FadePhase? {
            if (`in` > Duration.ZERO) return In(this, time)
            if (stay > Duration.ZERO) return Stay(this, time)
            if (out > Duration.ZERO) return Out(this, time)

            return null
        }
    }
}
