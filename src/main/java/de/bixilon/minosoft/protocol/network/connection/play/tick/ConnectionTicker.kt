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
import de.bixilon.kutil.concurrent.time.TimeWorker.runLater
import de.bixilon.kutil.concurrent.time.TimeWorkerTask
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class ConnectionTicker(private val connection: PlayConnection) {
    private val tasks: MutableSet<TimeWorkerTask> = mutableSetOf()
    private val lock = SimpleLock()
    private var registered = false


    fun init() {
        connection::state.observe(this) {
            if (it != PlayConnectionStates.PLAYING) {
                unregister()
            } else {
                // Ticks are postponed 10 ticks
                // When joining/respawning the lock on chunks, etc is the performance bottleneck and makes the game laggy.
                runLater(10 * ProtocolDefinition.TICK_TIME) { register() }
            }
        }
    }


    private fun register() {
        if (registered) {
            return
        }
        lock.lock()
        if (registered || connection.state != PlayConnectionStates.PLAYING) {
            lock.unlock()
            return
        }
        tasks += TimeWorkerTask(INTERVAL, maxDelayTime = MAX_DELAY) {
            connection.world.entities.tick()
        }
        tasks += TimeWorkerTask(INTERVAL, maxDelayTime = MAX_DELAY) {
            connection.world.tick()
        }
        tasks += TimeWorkerTask(INTERVAL, maxDelayTime = MAX_DELAY) {
            connection.world.randomTick()
        }

        if (DebugOptions.LIGHT_DEBUG_MODE || DebugOptions.INFINITE_TORCHES) {
            tasks += TimeWorkerTask(INTERVAL, maxDelayTime = MAX_DELAY) { connection.player.inventory[44] = ItemStack(connection.registries.itemRegistry["minecraft:torch"]!!, Int.MAX_VALUE) }
        }
        if (DebugOptions.SIMULATE_TIME) {
            tasks += TimeWorkerTask(INTERVAL, maxDelayTime = MAX_DELAY) {
                val time = connection.world.time.time
                val offset = if (time in 11800..13300 || (time < 300 || time > 22800)) {
                    20
                } else {
                    500
                }
                connection.world.time = WorldTime(time + offset, connection.world.time.age + offset)
            }
        }

        for (task in tasks) {
            TimeWorker += task
        }
        registered = true
        lock.unlock()
    }

    private fun unregister() {
        if (!registered) {
            return
        }
        lock.lock()

        for (task in tasks) {
            TimeWorker -= task
        }
        tasks.clear()
        registered = false
        lock.unlock()
    }

    private companion object {
        const val INTERVAL = ProtocolDefinition.TICK_TIME
        const val MAX_DELAY = INTERVAL / 2
    }
}
