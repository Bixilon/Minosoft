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

import de.bixilon.minosoft.util.KUtil.clean
import org.lwjgl.system.MemoryUtil.memAllocFloat
import java.nio.FloatBuffer

class ArrayFloatList(
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

    fun clear() {
        checkFinalized()
        buffer.clean()
        outputUpToDate = false
        output = FloatArray(0)
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
        buffer.put(0, oldBuffer, 0, oldBuffer.position())
        buffer.position(oldBuffer.position())
        oldBuffer.clean()
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

    fun addAll(floatList: ArrayFloatList) {
        ensureSize(floatList.size)
        buffer.put(buffer.position(), floatList.buffer, 0, floatList.buffer.position())
        buffer.position(buffer.position() + floatList.buffer.position())
    }

    private fun checkOutputArray() {
        if (outputUpToDate) {
            return
        }
        output = FloatArray(size)
        buffer.get(output, 0, buffer.position())
        outputUpToDate = true
    }

    fun toArray(): FloatArray {
        checkOutputArray()
        return output
    }

    fun finish() {
        finalized = true
        val oldBuffer = buffer
        buffer = memAllocFloat(oldBuffer.position())
        buffer.put(0, oldBuffer, 0, oldBuffer.position())
        buffer.position(buffer.limit())
        oldBuffer.clean()
    }


    private companion object {
        private const val DEFAULT_INITIAL_SIZE = 1000
    }
}
