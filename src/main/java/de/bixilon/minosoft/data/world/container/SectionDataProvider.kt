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

package de.bixilon.minosoft.data.world.container

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.iterator.EmptyIterator
import de.bixilon.kutil.concurrent.lock.Lock
import de.bixilon.minosoft.data.world.positions.InSectionPosition
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

open class SectionDataProvider<T>(
    var lock: Lock?,
    val checkSize: Boolean = false,
) : Iterable<T> {
    protected var data: Array<Any?>? = null
        private set
    var count: Int = 0
        private set
    val isEmpty: Boolean
        get() = count == 0
    var minPosition = InSectionPosition(ProtocolDefinition.SECTION_WIDTH_X, ProtocolDefinition.SECTION_HEIGHT_Y, ProtocolDefinition.SECTION_WIDTH_Z)
        private set
    var maxPosition = InSectionPosition(0, 0, 0)
        private set

    @Suppress("UNCHECKED_CAST")
    open operator fun get(position: InSectionPosition) = data?.get(position.index) as T
    open operator fun get(x: Int, y: Int, z: Int) = this[InSectionPosition(x, y, z)]

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

        this.minPosition = InSectionPosition(minX, minY, minZ)
        this.maxPosition = InSectionPosition(maxX, maxY, maxZ)
        this.count = count
        if (count == 0) {
            this.data = null
        }
    }


    open fun unsafeSet(position: InSectionPosition, value: T): T? {
        var data = data
        val previous = data?.get(position.index)
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
        data[position.index] = value

        if (checkSize) {

            if (value == null) {
                if ((minPosition.x == position.x && minPosition.y == position.y && minPosition.z == position.z) || (maxPosition.x == position.x && maxPosition.y == position.y && maxPosition.z == position.z)) {
                    recalculate()
                }
            } else {
                if (minPosition.x > position.x) minPosition = minPosition.with(x = position.x)
                if (minPosition.y > position.y) minPosition = minPosition.with(y = position.y)
                if (minPosition.z > position.z) minPosition = minPosition.with(z = position.z)

                if (maxPosition.x < position.x) maxPosition = maxPosition.with(x = position.x)
                if (maxPosition.y < position.y) maxPosition = maxPosition.with(y = position.y)
                if (maxPosition.z < position.z) maxPosition = maxPosition.with(z = position.z)
            }
        }
        return previous.unsafeCast()
    }

    operator fun set(x: Int, y: Int, z: Int, value: T) = this.set(InSectionPosition(x, y, z), value)
    open operator fun set(position: InSectionPosition, value: T): T? {
        lock?.lock()
        val previous = unsafeSet(position, value)
        lock?.unlock()
        return previous
    }


    @Suppress("UNCHECKED_CAST")
    fun setData(data: Array<T>) {
        lock?.lock()
        check(data.size == ProtocolDefinition.BLOCKS_PER_SECTION) { "Size does not match!" }
        this.data = data as Array<Any?>
        recalculate()
        lock?.unlock()
    }


    fun clear() {
        lock?.lock()
        this.data = null
        recalculate()
        lock?.unlock()
    }

    @Suppress("UNCHECKED_CAST")
    override fun iterator(): Iterator<T> {
        val data = this.data ?: return EmptyIterator.unsafeCast()
        if (this.isEmpty) return EmptyIterator.unsafeCast()

        return data.iterator().unsafeCast()
    }
}
