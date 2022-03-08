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

import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class Cooldown {
    private var start = -1L
    private var end = -1L
    val ended: Boolean
        get() = timeLeft == 0L
    val time: Long
        get() {
            if (start < 0L) {
                return 0
            }
            return end - start
        }
    val timeLeft: Long
        get() {
            if (start < 0L) {
                return 0L
            }
            val time = TimeUtil.time
            return time - end
        }
    val progress: Float
        get() {
            val timeLeft = timeLeft
            if (timeLeft == 0L) {
                return 1.0f
            }
            val time = time
            if (time == 0L) {
                return 1.0f
            }
            return timeLeft.toFloat() / time
        }


    fun set(millis: Long) {
        val time = TimeUtil.time
        start = time
        end = start + millis
    }

    fun set(millis: Int) = set(millis.toLong())

    fun setTicks(ticks: Int) {
        set(ticks * ProtocolDefinition.TICK_TIME)
    }
}
