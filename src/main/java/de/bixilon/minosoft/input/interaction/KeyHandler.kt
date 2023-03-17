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
import de.bixilon.kutil.concurrent.time.TimeWorker
import de.bixilon.kutil.concurrent.time.TimeWorkerTask
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

abstract class KeyHandler {
    private var task: TimeWorkerTask? = null
    var isPressed: Boolean = false
        private set

    private fun queueTick() {
        val task = TimeWorkerTask(ProtocolDefinition.TICK_TIME, runOnce = false, executionPriority = ThreadPool.HIGH) { onTick() }
        task.lastExecution = millis() // TODO: remove this workaround, kutil 1.21
        this.task = task
        TimeWorker += task
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
        TimeWorker -= task
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
