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
import glm_.vec3.Vec3i

open class SectionDataProvider<T>(
    data: Array<Any?> = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION),
    val checkSize: Boolean = false,
) : Iterable<T> {
    protected var data = data
        private set
    protected val lock = SemaphoreLock() // lock while reading (blocks writing)
    var count: Int = 0
        private set
    val isEmpty: Boolean
        get() = count == 0
    lateinit var minPosition: Vec3i
        private set
    lateinit var maxPosition: Vec3i
        private set

    init {
        recalculate()
    }

    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int): T {
        lock.acquire()
        val value = data[index] as T
        lock.release()
        return value
    }

    @Suppress("UNCHECKED_CAST")
    fun unsafeGet(index: Int): T {
        return data[index] as T
    }

    @Suppress("UNCHECKED_CAST")
    fun unsafeGet(x: Int, y: Int, z: Int): T {
        return data[y shl 8 or (z shl 4) or x] as T
    }

    private fun recalculate() {
        var count = 0
        var value: Any?

        var minX = 16
        var minY = 16
        var minZ = 16

        var maxX = 0
        var maxY = 0
        var maxZ = 0

        var x: Int
        var y: Int
        var z: Int
        for (index in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
            data[index] ?: continue
            count++
            if (!checkSize) {
                continue
            }
            x = index and 0x0F
            z = (index shr 4) and 0x0F
            y = index shr 8

            if (x < minX) {
                minX = x
            }
            if (y < minY) {
                minY = y
            }
            if (z < minZ) {
                minZ = z
            }

            if (x < maxX) {
                maxX = x
            }
            if (y > maxY) {
                maxY = y
            }
            if (z > maxZ) {
                maxZ = z
            }

        }
        this.minPosition = Vec3i(minX, minY, minZ)
        this.maxPosition = Vec3i(maxX, maxY, maxZ)
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
                unlock()
                return
            }
            count--
        } else if (previous == null) {
            count++
        }
        data[index] = value

        if (checkSize) {
            val x = index and 0x0F
            val z = (index shr 4) and 0x0F
            val y = index shr 8

            if ((minPosition.x == x && minPosition.y == y && minPosition.z == z) || (maxPosition.x == x && maxPosition.y == y && maxPosition.z == z)) {
                recalculate()
            }
        }
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
        recalculate()
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
