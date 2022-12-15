/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.minosoft.util.collections.floats.FloatListUtil.copy
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.FloatBuffer

class FragmentedArrayFloatList(
    initialSize: Int = FloatListUtil.DEFAULT_INITIAL_SIZE,
) : AbstractFloatList(), DirectArrayFloatList {
    var fragments: MutableList<FloatBuffer> = mutableListOf()
    override var limit: Int = 0
        private set
    override var size: Int = 0
        private set
    override val isEmpty: Boolean
        get() = size == 0
    private var unloaded = false

    private val nextGrowStep = when {
        initialSize <= 0 -> FloatListUtil.DEFAULT_INITIAL_SIZE
        initialSize <= 100 -> 100
        else -> initialSize
    }

    private var output: FloatArray? = null
    private var buffer: FloatBuffer? = null


    override fun ensureSize(needed: Int) {
        grow(needed)
    }

    private fun grow(size: Int): FloatBuffer {
        checkFinished()
        if (limit - this.size >= size) {
            return this.fragments.last()
        }
        val grow = if (nextGrowStep < size) {
            (size / nextGrowStep + 1) * nextGrowStep
        } else {
            nextGrowStep
        }
        return forceGrow(grow)
    }

    private fun forceGrow(size: Int): FloatBuffer {
        val last = fragments.lastOrNull()
        val buffer = memAllocFloat(size)
        fragments += buffer
        limit += size
        return buffer
    }

    override fun add(value: Float) {
        val buffer = grow(1)
        if (buffer.position() >= buffer.limit()) {
            println()
        }
        buffer.put(value)
        size += 1
        invalidateOutput()
    }

    override fun add(array: FloatArray) {
        if (array.isEmpty()) return
        invalidateOutput()

        var offset = 0
        size += array.size
        if (fragments.isNotEmpty()) {
            val fragment = fragments.last()
            val remaining = fragment.limit() - fragment.position()
            val copy = minOf(array.size, remaining)
            fragment.put(array, 0, copy)
            offset += copy

            if (array.size <= remaining) {
                // everything copied
                return
            }
        }
        val length = array.size - offset
        val next = grow(length)
        next.put(array, offset, length)
        next.position(length)
    }

    override fun add(buffer: FloatBuffer) {
        if (buffer.position() == 0) return
        invalidateOutput()

        var offset = 0
        val position = buffer.position()
        size += position
        if (fragments.isNotEmpty()) {
            val fragment = fragments.last()
            val remaining = fragment.limit() - fragment.position()
            val copy = minOf(position, remaining)
            buffer.copy(0, fragment, fragment.position(), copy)
            offset += copy

            if (position <= remaining) {
                // everything copied
                return
            }
        }
        val length = position - offset
        val next = grow(length)
        buffer.copy(offset, next, 0, length)
        next.position(length)
    }

    override fun add(floatList: AbstractFloatList) {
        when (floatList) {
            is FragmentedArrayFloatList -> {
                // TODO: add dirty method (just adding their fragments to our list of fragments)
                for (buffer in floatList.fragments) {
                    add(buffer)
                }
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
        for (buffer in fragments) {
            val position = buffer.position()
            buffer.position(0)
            buffer.get(output, offset, position)
            offset += position
            buffer.position(position)
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
        for (buffer in fragments) {
            memFree(buffer)
        }
        this.output = null
        val buffer = this.buffer
        if (buffer != null) {
            memFree(buffer)
            this.buffer = null
        }
        fragments.clear()
    }

    override fun clear() {
        size = 0
        for (buffer in fragments) {
            buffer.clear()
        }
        if (output != null) {
            output = null
        }
        invalidateOutput()
    }

    override fun finish() {
        finished = true
        if (fragments.isEmpty()) {
            return
        }
        val last = fragments.removeLast()
        val next = memAllocFloat(last.position())
        last.copy(next)
        fragments += next
    }

    protected fun finalize() {
        if (unloaded) {
            return
        }
        for (buffer in fragments) {
            memFree(buffer)
        }
        fragments.clear()
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
        if (fragments.size == 1) {
            return fragments.first()
        }
        val buffer = memAllocFloat(size)
        for (fragment in fragments) {
            fragment.copy(buffer)
        }
        this.buffer = buffer
        return buffer
    }
}
