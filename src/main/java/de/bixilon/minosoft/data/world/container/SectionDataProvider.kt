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

package de.bixilon.minosoft.data.world.container

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.SemaphoreLock

open class SectionDataProvider<T>(
    data: Array<Any?> = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION),
) : Iterable<T> {
    protected var data = data
        private set
    protected val lock = SemaphoreLock() // lock while reading (blocks writing)
    var count: Int = 0
        private set

    init {
        recalculateCount()
    }

    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int): T {
        lock.acquire()
        val value = data[index] as T
        lock.release()
        return value
    }

    private fun recalculateCount() {
        var count = 0
        for (value in data) {
            if (value == null) {
                continue
            }
            count++
        }
        this.count = count
    }

    operator fun get(x: Int, y: Int, z: Int): T {
        return get(y shl 8 or (z shl 4) or x)
    }

    operator fun set(x: Int, y: Int, z: Int, value: T) {
        set(y shl 8 or (z shl 4) or x, value)
    }

    operator fun set(index: Int, value: T) {
        lock()
        val previous = data[index]
        if (value == null) {
            if (previous == null) {
                return
            }
            count--
        } else if (previous == null) {
            count++
        }
        data[index] = value
        unlock()
    }

    fun acquire() {
        lock.acquire()
    }

    fun release() {
        lock.release()
    }

    fun lock() {
        lock.lock()
    }

    fun unlock() {
        lock.unlock()
    }


    @Suppress("UNCHECKED_CAST")
    @Synchronized
    fun setData(data: Array<T>) {
        lock()
        check(data.size == ProtocolDefinition.BLOCKS_PER_SECTION) { "Size does not match!" }
        this.data = data as Array<Any?>
        recalculateCount()
        unlock()
    }

    open fun copy(): SectionDataProvider<T> {
        acquire()
        val clone = SectionDataProvider<T>(data.clone())
        release()

        return clone
    }

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<T> {
        return data.iterator() as Iterator<T>
    }
}
