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
import de.bixilon.kutil.exception.Unreachable
import de.bixilon.kutil.reflection.ReflectionUtil.field
import de.bixilon.minosoft.util.collections.floats.FloatListUtil.copy
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.FloatBuffer

class FragmentedFloatList(
    growStep: Int = FloatListUtil.DEFAULT_INITIAL_SIZE,
) : FloatList {
    private val complete: MutableList<FloatBuffer> = ArrayList()
    private val incomplete: MutableList<FloatBuffer> = ArrayList()
    override var capacity = 0
        private set
    override var size = 0
        private set

    private val nextGrowStep = when {
        growStep <= 0 -> FloatListUtil.DEFAULT_INITIAL_SIZE
        growStep <= 128 -> 128
        else -> growStep
    }

    override fun ensureSize(needed: Int) {
        if (needed == 0) return
        tryGrow(needed)
    }

    private fun tryGrow(size: Int): FloatBuffer {
        if (capacity - this.size >= size) {
            return this.incomplete[0]
        }
        return grow(size)
    }

    private fun grow(size: Int): FloatBuffer {
        val grow = if (nextGrowStep < size) {
            (size / nextGrowStep + 1) * nextGrowStep
        } else {
            nextGrowStep
        }
        return forceGrow(grow)
    }

    private fun forceGrow(size: Int): FloatBuffer {
        val buffer = memAllocFloat(size)
        incomplete += buffer
        capacity += size
        return buffer
    }

    override fun add(value: Float) {
        val buffer = tryGrow(1)
        buffer.put(value)
        size += 1
        tryPush(buffer)
        assert()
    }

    private fun tryPush(fragment: FloatBuffer): Boolean {
        if (fragment.position() != fragment.limit()) {
            return false
        }
        complete += fragment
        incomplete -= fragment
        return true
    }

    private inline fun add(offset: Int, length: Int, function: (sourceOffset: Int, destination: FloatBuffer, destinationOffset: Int, length: Int) -> Unit) {
        if (length == 0) return
        var offset = offset
        var left = length

        var indexOffset = 0 // avoid ConcurrentModificationException when pushing list
        for (index in 0 until incomplete.size) {
            val fragment = incomplete[index + indexOffset]

            val copy = minOf(left, fragment.remaining())

            val position = fragment.position()
            function.invoke(offset, fragment, position, copy)
            fragment.position(position + copy)

            offset += copy
            left -= copy

            this.size += copy // tryPush needs the current size
            if (tryPush(fragment)) indexOffset-- // fragment not anymore in the list (shifted)

            if (left == 0) {
                // everything copied
                assert()
                return
            }
        }

        val next = tryGrow(left)

        function.invoke(offset, next, 0, left)
        next.position(left)

        this.size += left
        tryPush(next)
        assert()
    }

    override fun add(array: FloatArray, offset: Int, length: Int) {
        add(offset, length, function = { sourceOffset, destination, destinationOffset, length -> array.copy(sourceOffset, destination, destinationOffset, length) })
    }

    override fun add(buffer: FloatBuffer, offset: Int, length: Int) {
        add(offset, length, function = { sourceOffset, destination, destinationOffset, length -> buffer.copy(sourceOffset, destination, destinationOffset, length) })
    }

    override fun add(list: FloatList) = when (list) {
        is FragmentedFloatList -> {
            ensureSize(list.size)
            list.forEach { add(it, 0, it.position()) }
        }

        else -> super.add(list)
    }

    @Deprecated("Slow!")
    override operator fun get(index: Int): Float {
        assert(index >= 0 && index < this.size)
        var left = index
        forEach {
            if (left >= it.limit()) {
                left -= it.limit()
                return@forEach
            }

            return it.get(left)
        }

        Unreachable()
    }

    override fun get(destination: FloatArray, sourceOffset: Int, destinationOffset: Int, length: Int) {
        assert(sourceOffset >= 0 && sourceOffset + length <= this.size)

        super.get(destination, sourceOffset, destinationOffset, length) // TODO
    }

    override fun get(destination: FloatBuffer, sourceOffset: Int, length: Int) {
        assert(sourceOffset >= 0 && sourceOffset + length <= this.size)

        super.get(destination, sourceOffset, length) // TODO
    }

    override fun free() {
        forEach { memFree(it) }
        COMPLETE[this] = null
        INCOMPLETE[this] = null
    }

    override fun clear() {
        size = 0
        forEach { it.clear() }
        incomplete.addAll(0, complete)
        complete.clear()
    }

    private inline fun forEach(callable: (FloatBuffer) -> Unit) {
        this.complete.forEach(callable)
        this.incomplete.forEach(callable)
    }

    private fun assert() {
        if (!ASSERT) return
        val expected = size
        var actual = 0
        forEach { actual += it.position() }
        assert(expected == actual) { "Buffer size mismatch: expected=$expected, actual=$actual" }
    }


    private fun batchAdd(value: Float, buffer: FloatBuffer, left: Int): FloatBuffer {
        buffer.put(value)
        if (!tryPush(buffer)) {
            return buffer
        }
        if (left == 0) return buffer
        return this.incomplete.firstOrNull() ?: grow(left)
    }

    override fun add(value1: Float, value2: Float) {
        var buffer = tryGrow(1)
        buffer = batchAdd(value1, buffer, 1)
        batchAdd(value2, buffer, 0)

        size += 2
        assert()
    }

    override fun add(value1: Float, value2: Float, value3: Float) {
        var buffer = tryGrow(1)
        buffer = batchAdd(value1, buffer, 2)
        buffer = batchAdd(value2, buffer, 1)
        batchAdd(value3, buffer, 0)

        size += 3
        assert()
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float) {
        var buffer = tryGrow(1)
        buffer = batchAdd(value1, buffer, 3)
        buffer = batchAdd(value2, buffer, 2)
        buffer = batchAdd(value3, buffer, 1)
        batchAdd(value4, buffer, 0)

        size += 4
        assert()
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float) {
        var buffer = tryGrow(1)
        buffer = batchAdd(value1, buffer, 4)
        buffer = batchAdd(value2, buffer, 3)
        buffer = batchAdd(value3, buffer, 2)
        buffer = batchAdd(value4, buffer, 1)
        batchAdd(value5, buffer, 0)

        size += 5
        assert()
    }

    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float, value6: Float) {
        size += 6

        var buffer = this.incomplete.firstOrNull() ?: tryGrow(1)
        val left = buffer.limit() - buffer.position()
        if (left >= 6) {
            buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4); buffer.put(value5); buffer.put(value6)
            if (left == 6) {
                tryPush(buffer)
            }
        } else {
            buffer = batchAdd(value1, buffer, 5)
            buffer = batchAdd(value2, buffer, 4)
            buffer = batchAdd(value3, buffer, 3)
            buffer = batchAdd(value4, buffer, 2)
            buffer = batchAdd(value5, buffer, 1)
            batchAdd(value6, buffer, 0)
        }
        assert()
    }


    override fun add(value1: Float, value2: Float, value3: Float, value4: Float, value5: Float, value6: Float, value7: Float) {
        size += 7

        var buffer = this.incomplete.firstOrNull() ?: tryGrow(1)
        val left = buffer.limit() - buffer.position()
        if (left >= 7) {
            buffer.put(value1); buffer.put(value2); buffer.put(value3); buffer.put(value4); buffer.put(value5); buffer.put(value6); buffer.put(value7)
            if (left == 7) {
                tryPush(buffer)
            }
        } else {
            buffer = batchAdd(value1, buffer, 6)
            buffer = batchAdd(value2, buffer, 5)
            buffer = batchAdd(value3, buffer, 4)
            buffer = batchAdd(value4, buffer, 3)
            buffer = batchAdd(value5, buffer, 2)
            buffer = batchAdd(value6, buffer, 1)
            batchAdd(value7, buffer, 0)
        }
        assert()
    }

    private companion object {
        val ASSERT = FragmentedFloatList::class.java.desiredAssertionStatus()
        val COMPLETE = FragmentedFloatList::complete.field
        val INCOMPLETE = FragmentedFloatList::incomplete.field
    }
}
