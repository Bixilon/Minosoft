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

package de.bixilon.minosoft.gui.rendering.gui.elements.text.fade

interface FadePhase {
    val times: FadingTimes

    fun isDone(millis: Long): Boolean
    fun getAlpha(millis: Long): Float

    fun next(millis: Long): FadePhase?
    fun stop(millis: Long): FadePhase? = next(millis)

    class In(
        override val times: FadingTimes,
        private val start: Long,
    ) : FadePhase {
        private val end = start + times.`in`

        private fun getProgress(millis: Long) = (millis - start).toFloat() / times.`in`

        override fun isDone(millis: Long) = millis > end
        override fun getAlpha(millis: Long) = getProgress(millis)

        override fun next(millis: Long): FadePhase? {
            if (times.stay > 0) return Stay(times, millis)
            if (times.out > 0) return Out(times, millis)

            return null
        }

        override fun stop(millis: Long): FadePhase? {
            if (times.out <= 0) return null
            val progress = getProgress(millis)
            if (progress == 0.0f) return null

            return Out(times, millis) // TODO: set progress
        }
    }

    class Stay(
        override val times: FadingTimes,
        private val start: Long,
    ) : FadePhase {
        private val end = start + times.stay

        override fun isDone(millis: Long) = millis > end
        override fun getAlpha(millis: Long) = 1.0f

        override fun next(millis: Long): FadePhase? {
            if (times.out > 0) return Out(times, millis)

            return null
        }
    }

    class Out(
        override val times: FadingTimes,
        private val start: Long,
    ) : FadePhase {
        private val end = start + times.out
        override fun getAlpha(millis: Long) = 1.0f - ((millis - start).toFloat() / times.out)

        override fun isDone(millis: Long) = millis > end

        override fun next(millis: Long): FadePhase? = null
    }


    companion object {

        fun FadingTimes.createPhase(millis: Long): FadePhase? {
            if (`in` > 0) return In(this, millis)
            if (stay > 0) return Stay(this, millis)
            if (out > 0) return Out(this, millis)

            return null
        }
    }
}
