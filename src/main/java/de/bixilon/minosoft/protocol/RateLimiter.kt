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

package de.bixilon.minosoft.protocol

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.synchronizedListOf
import de.bixilon.minosoft.util.KUtil.synchronizedSetOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedList
import java.util.concurrent.locks.ReentrantLock

typealias RateAction = (() -> Unit)

class RateLimiter(
    val limit: Int = ProtocolDefinition.TICKS_PER_SECOND,
    val inTime: Long = 1000,
    val allowForcePerform: Boolean = true,
    val dependencies: MutableSet<RateLimiter> = synchronizedSetOf(),
) {
    private val lock = ReentrantLock()
    private var toDo: RateAction? = null
        @Synchronized get
        @Synchronized set
    private var executions: MutableList<Long> = synchronizedListOf()

    val upToDate: Boolean
        get() {
            lock.lock()
            val upToDate = toDo == null
            lock.unlock()
            return upToDate
        }

    val canWork: Boolean
        get() {
            cleanup(true)
            return executions.size < limit
        }
    private val _canWork: Boolean
        get() {
            cleanup(false)
            return executions.size < limit
        }

    /**
     * Tries to perform a specific action
     *
     * @return If the action could be performed or has to wait
     */
    fun perform(action: RateAction): Boolean {
        lock.lock()
        if (!_canWork) {
            toDo = action
            return false
        }

        internalPerform(action)
        lock.unlock()
        return true
    }

    operator fun plusAssign(action: RateAction) {
        perform(action)
    }

    private fun internalPerform(action: RateAction) {
        for (dependency in dependencies) {
            if (!dependency.upToDate) {
                dependency.work()
            }
            check(dependency.upToDate) { "RateLimiter dependency is not upToDate!" }
        }
        toDo = null
        action.invoke()
        val time = System.currentTimeMillis()
        executions += time
    }

    fun forcePerform(action: RateAction) {
        check(allowForcePerform) { "RateLimiter does not allow force performing!" }
        lock.lock()
        internalPerform(action)
        lock.unlock()
    }

    fun work() {
        lock.lock()
        cleanup(false)
        if (!_canWork) {
            return
        }
        toDo?.let { internalPerform(it) }
        lock.unlock()
    }

    private fun cleanup(lock: Boolean) {
        if (lock) {
            this.lock.lock()
        }
        val executions = executions.toSynchronizedList()
        val time = System.currentTimeMillis()

        for (execution in executions) {
            val addDelta = time - execution
            if (addDelta - inTime >= 0L) {
                // remove
                this.executions.removeFirst()
            } else {
                break
            }
        }
        if (lock) {
            this.lock.unlock()
        }
    }
}

