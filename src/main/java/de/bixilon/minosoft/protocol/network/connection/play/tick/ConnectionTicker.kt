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

package de.bixilon.minosoft.protocol.network.connection.play.tick

import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.time.TimeWorker
import de.bixilon.kutil.concurrent.time.TimeWorkerTask
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class ConnectionTicker(private val connection: PlayConnection) {
    private val lock = SimpleLock()
    private val tasks: MutableSet<TimeWorkerTask> = mutableSetOf()


    fun init() {
        connection::state.observe(this) {
            if (it != PlayConnectionStates.PLAYING) {
                unregister()
            } else {
                register()
            }
        }
    }


    private fun register() {
        lock.lock()
        _unregister()
        tasks += TimeWorkerTask(INTERVAL, maxDelayTime = MAX_DELAY) {
            connection.world.entities.tick()
        }
        tasks += TimeWorkerTask(INTERVAL, maxDelayTime = MAX_DELAY) {
            connection.world.tick()
        }
        tasks += TimeWorkerTask(INTERVAL, maxDelayTime = MAX_DELAY) {
            connection.world.randomTick()
        }
        for (task in tasks) {
            TimeWorker += task
        }
        lock.unlock()
    }

    private fun _unregister() {
        for (task in tasks) {
            TimeWorker -= task
        }
        tasks.clear()
    }

    private fun unregister() {
        lock.lock()
        _unregister()
        lock.unlock()
    }

    private companion object {
        const val INTERVAL = ProtocolDefinition.TICK_TIME
        const val MAX_DELAY = INTERVAL / 2
    }
}
