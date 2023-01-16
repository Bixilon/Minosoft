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

package de.bixilon.minosoft.util.collections.floats

import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.util.collections.floats.FloatListUtil.copy
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.FloatBuffer

class FragmentedArrayFloatList(
    growStep: Int = FloatListUtil.DEFAULT_INITIAL_SIZE,
) : AbstractFloatList(), DirectArrayFloatList {
    var complete: MutableList<FloatBuffer> = ArrayList()
    var incomplete: MutableList<FloatBuffer> = ArrayList()
    override var limit: Int = 0
        private set
    override var size: Int = 0
        private set
    override val isEmpty: Boolean
        get() = size == 0
    private var unloaded = false

    private val nextGrowStep = when {
        growStep <= 0 -> FloatListUtil.DEFAULT_INITIAL_SIZE
        growStep <= 100 -> 100
        else -> growStep
    }

    private var output: FloatArray? = null
    private var buffer: FloatBuffer? = null


    override fun ensureSize(needed: Int) {
        if (needed == 0) {
            return
        }
        grow(needed)
    }

    private fun grow(size: Int): FloatBuffer {
        checkFinished()
        if (limit - this.size >= size) {
            return this.incomplete.first()
        }
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
        limit += size
        return buffer
    }

    override fun add(value: Float) {
        val buffer = grow(1)
        buffer.put(value)
        size += 1
        tryPush(buffer)
        invalidateOutput()
    }

    private fun tryPush(fragment: FloatBuffer): Boolean {
        if (fragment.position() != fragment.limit()) {
            return false
        }
        complete += fragment
        incomplete -= fragment
        return true
    }

    override fun add(array: FloatArray) {
        if (array.isEmpty()) return
        invalidateOutput()

        var offset = 0
        var indexOffset = 0
        for (index in 0 until incomplete.size) {
            val fragment = incomplete[index + indexOffset]
            val remaining = fragment.limit() - fragment.position()
            val copy = minOf(array.size - offset, remaining)
            fragment.put(array, offset, copy)
            offset += copy
            if (tryPush(fragment)) indexOffset--


            if (array.size == offset) {
                // everything copied
                size += array.size
                // verifyPosition()
                return
            }
        }
        size += offset
        val length = array.size - offset
        val next = grow(length)
        next.put(array, offset, length)
        size += length
        next.position(length)
        tryPush(next)
        // verifyPosition()
    }

    override fun add(buffer: FloatBuffer) {
        if (buffer.position() == 0) return
        invalidateOutput()

        var offset = 0
        val position = buffer.position()
        var indexOffset = 0
        for (index in 0 until incomplete.size) {
            val fragment = incomplete[index + indexOffset]
            val remaining = fragment.limit() - fragment.position()
            val copy = minOf(position - offset, remaining)
            buffer.copy(offset, fragment, fragment.position(), copy)
            offset += copy
            if (tryPush(fragment)) indexOffset--

            if (position == offset) {
                // everything copied
                size += position
                // verifyPosition()
                return
            }
        }
        size += offset
        val length = position - offset
        val next = grow(length)
        buffer.copy(offset, next, 0, length)
        next.position(length)
        size += length
        tryPush(next)
        // verifyPosition()
    }

    override fun add(floatList: AbstractFloatList) {
        when (floatList) {
            is FragmentedArrayFloatList -> {
                // TODO: add dirty method (just adding their fragments to our list of fragments)
                floatList.forEach(this::add)
            }

            is DirectArrayFloatList -> add(floatList.toBuffer())
            else -> add(floatList.toArray())
        }
        invalidateOutput()
    }

    private fun checkOutputArray(): FloatArray {
        this.output?.let { return it }
        val output = FloatArray(size)
        var offset = 0
        forEach {
            val position = it.position()
            it.position(0)
            it.get(output, offset, position)
            offset += position
            it.position(position)
        }
        this.output = output
        return output
    }

    override fun toArray(): FloatArray {
        return checkOutputArray()
    }

    override fun unload() {
        check(!unloaded) { "Already unloaded!" }
        unloaded = true
        finished = true // Is unloaded
        forEach { memFree(it) }
        this.output = null
        val buffer = this.buffer
        if (buffer != null) {
            memFree(buffer)
            this.buffer = null
        }
        complete.clear()
        incomplete.clear()
    }

    override fun clear() {
        size = 0
        forEach { it.clear() }
        incomplete.addAll(0, complete)
        complete.clear()
        invalidateOutput()
    }

    override fun finish() {
        finished = true
        if (complete.size + incomplete.size == 0) {
            return
        }
        for (fragment in incomplete) {
            if (fragment.position() == 0) {
                continue
            }
            val next = memAllocFloat(fragment.position())
            fragment.copy(next)
            complete += next
        }
    }

    protected fun finalize() {
        if (unloaded) {
            return
        }
        unload()
    }

    private fun invalidateOutput() {
        if (this.output != null) {
            this.output = null
        }

        if (this.buffer != null) {
            this.buffer = null
        }
    }

    override fun toBuffer(): FloatBuffer {
        this.buffer?.let { return it }
        if (complete.size + incomplete.size == 1) {
            return complete.firstOrNull() ?: incomplete.first()
        }
        val buffer = memAllocFloat(size)
        forEach { it.copy(buffer) }
        if (buffer.position() != this.size) {
            // TODO: this should never happen, remove this check
            Broken("Position mismatch: ${buffer.position()}, expected $size")
        }
        this.buffer = buffer
        return buffer
    }

    inline fun forEach(callable: (FloatBuffer) -> Unit) {
        for (buffer in this.complete) {
            callable(buffer)
        }
        for (buffer in this.incomplete) {
            callable(buffer)
        }
    }

    private fun verifyPosition() {
        val expected = size
        var actual = 0
        forEach { actual += it.position() }
        if (expected != actual) {
            Broken("Buffer size mismatch: expected=$expected, actual=$actual")
        }
    }
}
