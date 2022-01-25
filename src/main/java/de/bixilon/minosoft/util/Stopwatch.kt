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

package de.bixilon.minosoft.util

import de.bixilon.kutil.unit.UnitFormatter
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class Stopwatch {
    val id = LAST_ID++
    private val startTime = System.nanoTime()
    private val labs: MutableList<Long> = mutableListOf()

    /**
     * @return Returns the difference between the last lab and the current time
     */
    fun lab(): Long {
        val currentTime = System.nanoTime()
        val lastLab = labs.getOrNull(labs.size - 1) ?: startTime
        labs.add(currentTime)
        return currentTime - lastLab
    }

    fun labTime(): String {
        return UnitFormatter.formatNanos(lab())
    }

    fun labPrint() {
        val delta = lab()
        Log.log(LogMessageType.GENERAL, LogLevels.INFO) { "Stop watch ($id) lab: ${UnitFormatter.formatNanos(delta)}" }
    }

    fun totalTime(): String {
        return UnitFormatter.formatNanos(System.nanoTime() - startTime)
    }

    companion object {
        private var LAST_ID = 0
    }
}
