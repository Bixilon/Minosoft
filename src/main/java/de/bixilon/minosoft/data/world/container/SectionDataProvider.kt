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

import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.ReadWriteLock
import glm_.vec3.Vec3i

class SectionDataProvider<T>(
    data: Array<T>? = null,
    val checkSize: Boolean = false,
) : Iterable<T> {
    protected var data: Array<Any?>? = data?.unsafeCast()
        private set
    protected val lock = ReadWriteLock() // lock while reading (blocks writing)
    var count: Int = 0
        private set
    val isEmpty: Boolean
        get() = count == 0
    lateinit var minPosition: Vec3i
        private set
    lateinit var maxPosition: Vec3i
        private set

    init {
        if (data != null) {
            recalculate()
        } else {
            minPosition = Vec3i.EMPTY
            maxPosition = Vec3i.EMPTY
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int): T {
        lock.acquire()
        val value = data?.get(index) as T
        lock.release()
        return value
    }

    @Suppress("UNCHECKED_CAST")
    fun unsafeGet(index: Int): T {
        return data?.get(index) as T
    }

    @Suppress("UNCHECKED_CAST")
    fun unsafeGet(x: Int, y: Int, z: Int): T {
        return data?.get(y shl 8 or (z shl 4) or x) as T
    }


    private fun recalculate() {
        val data = data
        if (data == null) {
            count = 0
            return
        }
        var count = 0

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

            if (x > maxX) {
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
        if (count == 0) {
            this.data = null
        }
    }

    operator fun get(x: Int, y: Int, z: Int): T {
        return get(y shl 8 or (z shl 4) or x)
    }

    operator fun set(x: Int, y: Int, z: Int, value: T) {
        set(y shl 8 or (z shl 4) or x, value)
    }

    operator fun set(index: Int, value: T) {
        lock()
        var data = data
        val previous = data?.get(index)
        if (value == null) {
            if (previous == null) {
                unlock()
                return
            }
            count--
            if (count == 0) {
                this.data = null
                unlock()
                return
            }
        } else if (previous == null) {
            count++
        }
        if (data == null) {
            data = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION)
            this.data = data
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

    fun copy(): SectionDataProvider<T> {
        acquire()
        val clone = SectionDataProvider<T>(data?.clone()?.unsafeCast())
        release()

        return clone
    }

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<T> {
        return (data?.iterator() ?: EMPTY_ITERATOR) as Iterator<T>
    }


    companion object {
        private val EMPTY_ITERATOR = listOf<Any>().iterator()
    }
}
