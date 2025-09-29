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

package de.bixilon.minosoft.input.interaction

import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.minosoft.protocol.network.session.play.tick.TickUtil

abstract class KeyHandler {
    private var task: RepeatedTask? = null

    private fun queueTick() {
        var skip = true // first tick is scheduled instantly, avoid double ticking
        val task = RepeatedTask(TickUtil.INTERVAL, priority = ThreadPool.Priorities.HIGH) {
            if (skip) {
                skip = false
                return@RepeatedTask
            }
            if (task == null) return@RepeatedTask
            onTick()
        }
        this.task = task
        TaskScheduler += task
    }

    fun press() {
        val task = task
        if (task != null) return
        onPress()
        queueTick()
    }

    fun release() {
        val task = this.task ?: return
        TaskScheduler -= task
        this.task = null
        this.onRelease()
    }

    protected abstract fun onPress()
    protected abstract fun onTick()
    protected abstract fun onRelease()
}
