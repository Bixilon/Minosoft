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

package de.bixilon.minosoft.util.collections.floats

import de.bixilon.kutil.collections.primitive.floats.FloatList
import de.bixilon.kutil.reflection.ReflectionUtil.field
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memRealloc
import java.nio.FloatBuffer


class BufferFloatList(
    initialSize: Int = FloatListUtil.DEFAULT_INITIAL_SIZE,
) : FloatList {
    private var buffer = memAllocFloat(initialSize)
    override val capacity get() = buffer.capacity()
    override val size get() = buffer.position()

    private val nextGrowStep = when {
        initialSize <= 0 -> FloatListUtil.DEFAULT_INITIAL_SIZE
        initialSize <= 64 -> 64
        else -> initialSize
    }

    override fun clear() {
        buffer.position(0)
    }

    private fun grow(size: Int) {
        this.buffer = memRealloc(buffer, size)
        this.buffer.limit(size)
    }

    override fun ensureSize(needed: Int) {
        if (capacity - size >= needed) {
            return
        }
        var newSize = capacity
        while (newSize - size < needed) {
            newSize += nextGrowStep
        }
        grow(newSize)
    }


    override fun add(value: Float) {
        ensureSize(1)
        buffer.put(value)
    }

    override fun add(floats: FloatArray, offset: Int, length: Int) {
        ensureSize(length)
        buffer.put(floats, offset, length)
    }

    override fun add(buffer: FloatBuffer, offset: Int, length: Int) {
        ensureSize(length)

        val limit = buffer.limit()
        val position = buffer.position()
        buffer.limit(offset + length); buffer.position(offset)

        this.buffer.put(buffer)

        buffer.limit(limit); buffer.position(position)
    }

    override operator fun get(index: Int): Float {
        assert(index >= 0 && index < this.size)
        return buffer[index]
    }

    override fun get(destination: FloatArray, sourceOffset: Int, destinationOffset: Int, length: Int) {
        assert(sourceOffset >= 0 && sourceOffset + length <= this.size)

        val position = this.buffer.position()
        this.buffer.position(sourceOffset)

        this.buffer.get(destination, destinationOffset, length)

        this.buffer.position(position)
    }

    override fun get(destination: FloatBuffer, sourceOffset: Int, length: Int) {
        assert(sourceOffset >= 0 && sourceOffset + length <= this.size)

        val position = this.buffer.position()

        this.buffer.limit(sourceOffset + length)
        this.buffer.position(sourceOffset)

        destination.put(this.buffer)

        this.buffer.limit(size)
        this.buffer.position(position)
    }


    override fun toUnsafeNativeBuffer() = this.buffer


    override fun add(value1: Float, value2: Float) {
        ensureSize(2)
        buffer.put(value1); buffer.put(value2)
    }

    override fun add(value1: Float, value2: Float, value3: Float) {
        ensureSize(3)
        buffer.put(value1); buffer.put(value2); buffer.put(value3)
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float) {
        ensureSize(4)
        buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4)
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float) {
        ensureSize(5)
        buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4)
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float, value6: Float) {
        ensureSize(6)
        buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4); buffer.put(value5); buffer.put(value6)
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float, value6: Float, value7: Float) {
        ensureSize(7)
        buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4); buffer.put(value5); buffer.put(value6); buffer.put(value7)
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float, value6: Float, value7: Float, value8: Float) {
        ensureSize(8)
        buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4); buffer.put(value5); buffer.put(value6); buffer.put(value7); buffer.put(value8)
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float, value6: Float, value7: Float, value8: Float, value9: Float) {
        ensureSize(9)
        buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4); buffer.put(value5); buffer.put(value6); buffer.put(value7); buffer.put(value8); buffer.put(value9)

    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float, value6: Float, value7: Float, value8: Float, value9: Float, value10: Float) {
        ensureSize(10)
        buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4); buffer.put(value5); buffer.put(value6); buffer.put(value7); buffer.put(value8); buffer.put(value9); buffer.put(value10)
    }


    override fun free() {
        BUFFER[this] = null
    }

    private companion object {
        private val BUFFER = BufferFloatList::buffer.field
    }
}
