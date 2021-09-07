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

package de.bixilon.minosoft.util.avg

import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList

class LongAverage(override val nanos: Long) : Average<Long> {
    /**
     * List of <Add Time (nanos), Value>
     */
    private val data: MutableList<Pair<Long, Long>> = synchronizedListOf()
    private var updated = false
    private var lastAVG = 0L

    override val avg: Long
        @Synchronized get() {
            if (!updated) {
                return lastAVG
            }
            cleanup()
            val data = data.toSynchronizedList()
            if (data.size == 0) {
                return 0
            }

            var total = 0L
            for ((_, value) in data) {
                total += value
            }

            lastAVG = total / data.size
            updated = false
            return lastAVG
        }

    override fun cleanup() {
        val time = System.nanoTime()

        var indexOffset = 0
        for ((index, pair) in data.toSynchronizedList().withIndex()) {
            val (addTime, _) = pair
            val addDelta = time - addTime
            if (addDelta - nanos >= 0L) {
                // remove
                data.removeAt(index - indexOffset)
                indexOffset++
                updated = true
            } else {
                break
            }
        }
    }

    override fun add(value: Long) {
        cleanup()
        val time = System.nanoTime()
        data += Pair(time, value)
        updated = true
    }
}
