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

package de.bixilon.minosoft.input.interaction

import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

abstract class KeyHandler {
    private var task: RepeatedTask? = null
    var isPressed: Boolean = false
        private set

    private fun queueTick() {
        val task = RepeatedTask(ProtocolDefinition.TICK_TIME, maxDelay = ProtocolDefinition.TICK_TIME, priority = ThreadPool.HIGH) { onTick() }
        this.task = task
        TaskScheduler += task
    }

    fun press() {
        if (isPressed) return
        this.isPressed = true
        onPress()
        queueTick()
    }

    fun release() {
        if (!isPressed) return
        val task = this.task ?: Broken()
        TaskScheduler -= task
        this.task = null
        this.isPressed = false
        this.onRelease()
    }

    protected abstract fun onPress()
    protected abstract fun onRelease()
    protected abstract fun onTick()


    fun change(pressed: Boolean) {
        if (pressed) press() else release()
    }
}
