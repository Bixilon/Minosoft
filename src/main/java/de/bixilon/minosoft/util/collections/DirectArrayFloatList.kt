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

package de.bixilon.minosoft.util.collections

import de.bixilon.minosoft.util.KUtil
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.FloatBuffer

class DirectArrayFloatList(
    initialSize: Int = DEFAULT_INITIAL_SIZE,
) {
    var buffer: FloatBuffer = memAllocFloat(initialSize) // ToDo: Clear when disconnected
        private set
    var finalized: Boolean = false
        private set
    val capacity: Int
        get() = buffer.capacity()
    val size: Int
        get() = buffer.position()
    val isEmpty: Boolean
        get() = size == 0
    private var unloaded = false

    private val nextGrowStep = when {
        initialSize <= 0 -> DEFAULT_INITIAL_SIZE
        initialSize <= 50 -> 50
        else -> initialSize
    }

    private var output: FloatArray = FloatArray(0)
    private var outputUpToDate = false

    private fun checkFinalized() {
        if (finalized) {
            throw IllegalStateException("ArrayFloatList is already finalized!")
        }
    }

    private fun ensureSize(needed: Int) {
        checkFinalized()
        if (capacity - size >= needed) {
            return
        }
        var newSize = capacity
        while (newSize - size < needed) {
            newSize += nextGrowStep
        }
        val oldBuffer = buffer
        buffer = memAllocFloat(newSize)
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

    fun add(float: Float) {
        ensureSize(1)
        buffer.put(float)
        outputUpToDate = false
    }

    fun addAll(floats: FloatArray) {
        ensureSize(floats.size)
        buffer.put(floats)
        outputUpToDate = false
    }

    fun addAll(floatList: DirectArrayFloatList) {
        ensureSize(floatList.size)
        if (FLOAT_PUT_METHOD == null) { // Java < 16
            for (i in 0 until floatList.buffer.position()) {
                buffer.put(floatList.buffer.get(i))
            }
        } else {
            FLOAT_PUT_METHOD.invoke(buffer, buffer.position(), floatList.buffer, 0, floatList.buffer.position())
            buffer.position(buffer.position() + floatList.buffer.position())
        }
    }

    fun addAll(floatList: ArrayFloatList) {
        ensureSize(floatList.size)
        buffer.put(floatList.toArray())
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

    fun toArray(): FloatArray {
        checkOutputArray()
        return output
    }

    fun unload() {
        check(!unloaded) { "Already unloaded!" }
        unloaded = true
        finalized = true // Is unloaded
        memFree(buffer)
    }

    fun finish() {
        finalized = true
        val oldBuffer = buffer
        buffer = memAllocFloat(oldBuffer.position())
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
        private val FLOAT_PUT_METHOD = KUtil.tryCatch { FloatBuffer::class.java.getMethod("put", Int::class.java, FloatBuffer::class.java, Int::class.java, Int::class.java) }
        private const val DEFAULT_INITIAL_SIZE = 1000
    }
}
