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

package de.bixilon.minosoft.protocol.network.session.status

import de.bixilon.kutil.concurrent.schedule.QueuedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.concurrent.schedule.TaskScheduler.runLater
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.unit.UnitFormatter.format
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import java.util.concurrent.TimeoutException

class TimeoutHandler(val session: StatusSession) {
    private var task: QueuedTask? = null
    private var cancelled = false

    init {
        watch()
    }

    private fun watch() {
        session::state.observe(this) {
            if (it != StatusSessionStates.PING_DONE && it != StatusSessionStates.ERROR) return@observe
            cancel()
        }
    }

    fun cancel() {
        if (cancelled) return
        cancelled = true
        val task = this.task ?: return
        TaskScheduler -= task
        this.task = null
    }

    fun register() {
        cancel()
        cancelled = false

        task = runLater(ProtocolDefinition.SOCKET_TIMEOUT) {
            if (cancelled) return@runLater
            if (session.state == StatusSessionStates.ERROR) return@runLater
            if (session.state == StatusSessionStates.PING_DONE) return@runLater

            session.error = TimeoutException("Session timed out after ${ProtocolDefinition.SOCKET_TIMEOUT.format()}") // implicit disconnect
        }
    }
}
