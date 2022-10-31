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

import de.bixilon.kutil.exception.ExceptionUtil.catchAll
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.BufferOverflowException
import java.nio.FloatBuffer

class DirectArrayFloatList(
    initialSize: Int = DEFAULT_INITIAL_SIZE,
) : AbstractFloatList() {
    var buffer: FloatBuffer = memAllocFloat(initialSize)
        private set
    override var limit: Int = buffer.limit()
        private set
    override val size: Int
        get() = buffer.position()
    override val isEmpty: Boolean
        get() = size == 0
    private var unloaded = false

    private val nextGrowStep = when {
        initialSize <= 0 -> DEFAULT_INITIAL_SIZE
        initialSize <= 100 -> 100
        else -> initialSize
    }

    private var output: FloatArray = FloatArray(0)
    private var outputUpToDate = false

    override fun ensureSize(needed: Int) {
        checkFinished()
        if (limit - size >= needed) {
            return
        }
        var newSize = limit
        newSize += if (nextGrowStep < needed) {
            (needed / nextGrowStep + 1) * nextGrowStep
        } else {
            nextGrowStep
        }
        grow(newSize)
    }

    private fun grow(size: Int) {
        val oldBuffer = buffer
        buffer = memAllocFloat(size)
        limit = size
        if (FLOAT_PUT_METHOD == null) { // Java < 16
            for (i in 0 until oldBuffer.position()) {
                buffer.put(oldBuffer.get(i))
            }
        } else {
            FLOAT_PUT_METHOD.invoke(buffer, 0, oldBuffer, 0, oldBuffer.position())
            buffer.position(oldBuffer.position())
        }
        memFree(oldBuffer)
    }

    override fun add(value: Float) {
        ensureSize(1)
        buffer.put(value)
        if (outputUpToDate) {
            outputUpToDate = false
        }
    }

    override fun addAll(floats: FloatArray) {
        ensureSize(floats.size)
        try {
            buffer.put(floats)
        } catch (exception: BufferOverflowException) {
            ensureSize(floats.size)

            exception.printStackTrace()
        }
        if (outputUpToDate) {
            outputUpToDate = false
        }
    }

    override fun addAll(floatList: AbstractFloatList) {
        if (floatList is DirectArrayFloatList) {
            ensureSize(floatList.size)
            if (FLOAT_PUT_METHOD == null) { // Java < 16
                for (i in 0 until floatList.buffer.position()) {
                    buffer.put(floatList.buffer.get(i))
                }
            } else {
                FLOAT_PUT_METHOD.invoke(buffer, buffer.position(), floatList.buffer, 0, floatList.buffer.position())
                buffer.position(buffer.position() + floatList.buffer.position())
            }
        } else {
            addAll(floatList.toArray())
        }
    }

    private fun checkOutputArray() {
        if (outputUpToDate) {
            return
        }
        val position = buffer.position()
        output = FloatArray(position)
        buffer.position(0)
        buffer.get(output, 0, position)
        buffer.position(position)
        outputUpToDate = true
    }

    override fun toArray(): FloatArray {
        checkOutputArray()
        return output
    }

    fun unload() {
        check(!unloaded) { "Already unloaded!" }
        unloaded = true
        finished = true // Is unloaded
        memFree(buffer)
    }

    override fun clear() {
        buffer.clear()
        if (output.isNotEmpty()) {
            output = FloatArray(0)
        }
        outputUpToDate = false
    }

    override fun finish() {
        finished = true
        val oldBuffer = buffer
        buffer = memAllocFloat(oldBuffer.position())
        limit = buffer.limit()
        if (FLOAT_PUT_METHOD == null) { // Java < 16
            for (i in 0 until oldBuffer.position()) {
                buffer.put(oldBuffer.get(i))
            }
        } else {
            FLOAT_PUT_METHOD.invoke(buffer, 0, oldBuffer, 0, oldBuffer.position())
            buffer.position(buffer.limit())
        }
        memFree(oldBuffer)
    }

    protected fun finalize() {
        if (unloaded) {
            return
        }
        memFree(buffer)
    }


    private companion object {
        private val FLOAT_PUT_METHOD = catchAll { FloatBuffer::class.java.getMethod("put", Int::class.java, FloatBuffer::class.java, Int::class.java, Int::class.java) }
        private const val DEFAULT_INITIAL_SIZE = 1000
    }
}
