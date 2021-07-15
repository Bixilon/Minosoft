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

import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList

class Queue {
    private val queue: MutableList<Runnable> = synchronizedListOf()

    @Synchronized
    fun add(runnable: Runnable) {
        queue += runnable
    }

    operator fun plusAssign(runnable: Runnable) {
        add(runnable)
    }

    fun work(maxJobs: Int = Int.MAX_VALUE) {
        var jobsDone = 0
        for (runnable in queue.toSynchronizedList()) {
            this.queue.remove(runnable)
            if (jobsDone == maxJobs) {
                break
            }
            runnable.run()
            jobsDone++
        }
    }

    fun timeWork(time: Long) {
        check(time > 0L) { "Can not have <= 0 time to do jobs!" }
        val start = System.currentTimeMillis()
        for (runnable in queue.toSynchronizedList()) {
            this.queue.remove(runnable)
            if (System.currentTimeMillis() - start >= time) {
                break
            }
            runnable.run()
        }
    }

    fun clear() {
        queue.clear()
    }
}
