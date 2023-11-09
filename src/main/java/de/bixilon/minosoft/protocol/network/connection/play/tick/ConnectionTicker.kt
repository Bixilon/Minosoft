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

package de.bixilon.minosoft.protocol.network.connection.play.tick

import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.concurrent.schedule.TaskScheduler.runLater
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class ConnectionTicker(private val connection: PlayConnection) {
    private val tasks: MutableSet<RepeatedTask> = mutableSetOf()
    private val lock = SimpleLock()
    private var registered = false


    fun init() {
        addDefault()
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

    private fun addDefault() {
        tasks += RepeatedTask(INTERVAL, maxDelay = MAX_DELAY) {
            connection.world.entities.tick()
        }
        tasks += RepeatedTask(INTERVAL, maxDelay = MAX_DELAY) {
            connection.world.tick()
        }
        tasks += RepeatedTask(INTERVAL, maxDelay = MAX_DELAY) {
            connection.world.randomDisplayTick()
        }

        if (DebugOptions.LIGHT_DEBUG_MODE || DebugOptions.INFINITE_TORCHES) {
            tasks += RepeatedTask(INTERVAL, maxDelay = MAX_DELAY) { connection.player.items.inventory[44] = ItemStack(connection.registries.item["minecraft:torch"]!!, Int.MAX_VALUE) }
        }
        if (DebugOptions.SIMULATE_TIME) {
            tasks += RepeatedTask(INTERVAL, maxDelay = MAX_DELAY) {
                val time = connection.world.time.time
                val offset = if (time in 11800..13300 || (time < 300 || time > 22800)) {
                    20
                } else {
                    500
                }
                connection.world.time = WorldTime(time + offset, connection.world.time.age + offset)
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

        for (task in tasks) {
            TaskScheduler += task
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
            TaskScheduler -= task
        }
        registered = false
        lock.unlock()
    }

    fun register(runnable: Runnable) {
        lock.lock()
        val task = RepeatedTask(INTERVAL, maxDelay = MAX_DELAY, runnable = runnable)
        this.tasks += task
        if (registered) {
            TaskScheduler += task
        }
        lock.unlock()
    }

    operator fun plusAssign(runnable: Runnable) = register(runnable)

    private companion object {
        const val INTERVAL = ProtocolDefinition.TICK_TIME
        const val MAX_DELAY = INTERVAL / 2
    }
}
