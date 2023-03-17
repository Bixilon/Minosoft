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

package de.bixilon.minosoft.data.world.container

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

open class SectionDataProvider<T>(
    data: Array<T>? = null,
    val checkSize: Boolean = false,
    calculateInitial: Boolean = true,
) : Iterable<T> {
    protected var data: Array<Any?>? = data?.unsafeCast()
        private set
    protected val lock = SimpleLock() // lock while reading (blocks writing)
    var count: Int = 0
        private set
    val isEmpty: Boolean
        get() = count == 0
    lateinit var minPosition: Vec3i
        private set
    lateinit var maxPosition: Vec3i
        private set

    init {
        if (data != null && calculateInitial) {
            recalculate()
        } else {
            minPosition = Vec3i.EMPTY
            maxPosition = Vec3i.EMPTY
        }
    }

    @Suppress("UNCHECKED_CAST")
    operator fun get(index: Int): T {
        return data?.get(index) as T
    }

    operator fun get(x: Int, y: Int, z: Int): T {
        return get(ChunkSection.getIndex(x, y, z))
    }

    @Suppress("UNCHECKED_CAST")
    @Deprecated("no locking", ReplaceWith("this[index]"))
    fun unsafeGet(index: Int): T {
        return this[index]
    }

    @Suppress("UNCHECKED_CAST")
    @Deprecated("no locking", ReplaceWith("this[x, y, z]"))
    fun unsafeGet(x: Int, y: Int, z: Int): T {
        return this[x, y, z]
    }


    protected open fun recalculate() {
        val data = data
        if (data == null) {
            count = 0
            return
        }
        var count = 0

        var minX = ProtocolDefinition.SECTION_WIDTH_X
        var minY = ProtocolDefinition.SECTION_HEIGHT_Y
        var minZ = ProtocolDefinition.SECTION_WIDTH_Z

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

    operator fun set(x: Int, y: Int, z: Int, value: T): T? {
        return set(ChunkSection.getIndex(x, y, z), value)
    }

    fun unsafeSet(x: Int, y: Int, z: Int, value: T): T? {
        return unsafeSet(ChunkSection.getIndex(x, y, z), value)
    }

    open fun unsafeSet(index: Int, value: T): T? {
        var data = data
        val previous = data?.get(index)
        if (value == null) {
            if (previous == null) {
                return null
            }
            count--
            if (count == 0) {
                this.data = null
                return previous.unsafeCast()
            }
        } else if (previous == null) {
            count++
        }
        if (data == null) {
            data = arrayOfNulls<Any?>(ProtocolDefinition.BLOCKS_PER_SECTION).unsafeCast()!!
            this.data = data
        }
        data[index] = value

        if (checkSize) {
            val x = index and 0x0F
            val z = (index shr 4) and 0x0F
            val y = index shr 8

            if (value == null) {
                if ((minPosition.x == x && minPosition.y == y && minPosition.z == z) || (maxPosition.x == x && maxPosition.y == y && maxPosition.z == z)) {
                    recalculate()
                }
            } else {
                if (minPosition.x > x) minPosition.x = x
                if (minPosition.y > y) minPosition.y = y
                if (minPosition.z > z) minPosition.z = z

                if (maxPosition.x < x) maxPosition.x = x
                if (maxPosition.y < y) maxPosition.y = y
                if (maxPosition.z < z) maxPosition.z = z
            }
        }
        return previous.unsafeCast()
    }

    open operator fun set(index: Int, value: T): T? {
        lock()
        val previous = unsafeSet(index, value)
        unlock()
        return previous
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

    fun clear() {
        lock()
        this.data = null
        recalculate()
        unlock()
    }

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<T> {
        return (data?.iterator() ?: EMPTY_ITERATOR) as Iterator<T>
    }


    companion object {
        private val EMPTY_ITERATOR = emptyArray<Any>().iterator() // TODO: kutil 1.20.2
    }
}
