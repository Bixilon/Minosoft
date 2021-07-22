/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.util.KUtil.toSynchronizedList


class CountUpAndDownLatch @JvmOverloads constructor(count: Int, var parent: CountUpAndDownLatch? = null) {
    private val lock = Object()
    private val children: MutableSet<CountUpAndDownLatch> = mutableSetOf()
    private var rawCount = 0
        set(value) {
            val diff = value - field
            check(value >= 0) { "Can not set count (previous=$rawCount, value=$value)" }
            if (diff > 0) {
                total += diff
            }
            field = value
        }

    var count: Int
        get() {
            synchronized(lock) {
                return rawCount
            }
        }
        set(value) {
            val diff: Int
            synchronized(lock) {
                diff = value - rawCount
                rawCount = value
            }
            parent?.plus(diff) ?: notify()
        }

    var total: Int = count
        get() {
            synchronized(lock) {
                return field
            }
        }
        private set(value) {
            check(value >= 0) { "Total can not be < 0: $value" }
            synchronized(lock) {
                check(value >= field) { "Total can not decrement! (current=$field, wanted=$value)" }
                field = value
            }
        }


    init {
        check(parent !== this)
        parent?.addChild(this)
        this.count += count
    }

    fun addChild(latch: CountUpAndDownLatch) {
        synchronized(lock) {
            latch.parent = this
            children += latch
        }
    }

    @JvmOverloads
    fun await(timeout: Long = 0L) {
        while (true) {
            synchronized(lock) {
                if (rawCount == 0) {
                    return
                }
                lock.wait(timeout)
            }
        }
    }

    @JvmName(name = "customNotify")
    private fun notify(`this`: CountUpAndDownLatch = this) {
        synchronized(lock) {
            lock.notifyAll()
        }
        for (child in children.toSynchronizedList()) {
            if (child === `this`) {
                continue
            }
            child.notify(this)
        }
        if (`this` === parent) {
            return
        }
        parent?.notify(this)
    }

    operator fun inc(): CountUpAndDownLatch {
        plus(1)
        return this
    }

    operator fun dec(): CountUpAndDownLatch {
        minus(1)
        return this
    }

    fun plus(value: Int): CountUpAndDownLatch {
        synchronized(lock) {
            rawCount += value
        }
        parent?.plus(value) ?: notify()
        return this
    }

    fun minus(value: Int): CountUpAndDownLatch {
        return plus(-value)
    }


    fun waitForChange() {
        val lastCount = count
        val lastTotal = total
        while (lastCount == count && lastTotal == total) {
            synchronized(lock) {
                lock.wait()
            }
        }
    }

    fun awaitWithChange() {
        if (total == 0) {
            waitForChange()
        }
        await()
    }

    override fun toString(): String {
        return String.format("%d / %d", count, total)
    }
}
