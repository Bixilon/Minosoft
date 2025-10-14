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

package de.bixilon.minosoft.util

import de.bixilon.kutil.time.TimeUtil.now
import kotlin.time.Duration
import kotlin.time.TimeSource

class Stopwatch {
    val id = LAST_ID++
    private val start = now()
    private val labs: MutableList<TimeSource.Monotonic.ValueTimeMark> = mutableListOf()

    /**
     * @return Returns the difference between the last lab and the current time
     */
    fun lab(): Duration {
        val time = now()
        val last = labs.lastOrNull() ?: start
        val delta = time - last
        labs += time
        return delta
    }

    fun totalTime(): Duration {
        return now() - start
    }

    companion object {
        private var LAST_ID = 0
    }
}
