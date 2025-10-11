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

package de.bixilon.minosoft.protocol.network.session.play.tick

import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.kutil.concurrent.schedule.RepeatedTask
import de.bixilon.kutil.concurrent.schedule.TaskScheduler
import de.bixilon.kutil.concurrent.schedule.TaskScheduler.runLater
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.minosoft.config.DebugOptions
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.world.time.WorldTime
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.protocol.network.session.play.tick.TickUtil.INTERVAL

class SessionTicker(private val session: PlaySession) {
    private val tasks: MutableSet<RepeatedTask> = mutableSetOf()
    private val lock = Lock.lock()
    private var registered = false


    fun init() {
        addDefault()
        session::state.observe(this) {
            if (it != PlaySessionStates.PLAYING) {
                unregister()
            } else {
                // Ticks are postponed 10 ticks
                // When joining/respawning the lock on chunks, etc is the performance bottleneck and makes the game laggy.
                runLater(TickUtil.TIME_PER_TICK * 10) { register() }
            }
        }
    }

    private fun addDefault() {
        tasks += RepeatedTask(INTERVAL) {
            session.world.entities.tick()
        }
        tasks += RepeatedTask(INTERVAL) {
            session.world.tick()
        }
        tasks += RepeatedTask(INTERVAL) {
            session.world.randomDisplayTick()
        }

        if (DebugOptions.LIGHT_DEBUG_MODE || DebugOptions.INFINITE_TORCHES) {
            tasks += RepeatedTask(INTERVAL) { session.player.items.inventory.items[44] = ItemStack(session.registries.item["minecraft:torch"]!!, Int.MAX_VALUE) }
        }
        if (DebugOptions.SIMULATE_TIME) {
            tasks += RepeatedTask(INTERVAL) {
                val time = session.world.time.time
                val offset = if (time in 11800..13300 || (time < 300 || time > 22800)) {
                    20
                } else {
                    500
                }
                session.world.time = WorldTime(time + offset, session.world.time.age + offset)
            }
        }
    }


    private fun register() {
        if (registered) {
            return
        }
        lock.lock()
        if (registered || session.state != PlaySessionStates.PLAYING) {
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
        val task = RepeatedTask(INTERVAL, runnable = runnable)
        this.tasks += task
        if (registered) {
            TaskScheduler += task
        }
        lock.unlock()
    }

    operator fun plusAssign(runnable: Runnable) = register(runnable)
}
