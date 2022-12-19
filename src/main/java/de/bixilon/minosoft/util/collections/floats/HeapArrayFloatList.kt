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

import java.nio.FloatBuffer

@Deprecated("Kutil")
class HeapArrayFloatList(
    initialSize: Int = FloatListUtil.DEFAULT_INITIAL_SIZE,
) : AbstractFloatList() {
    private var data: FloatArray = FloatArray(initialSize)
    override val limit: Int
        get() = data.size
    override var size = 0
    override val isEmpty: Boolean
        get() = size == 0

    private val nextGrowStep = when {
        initialSize <= 0 -> FloatListUtil.DEFAULT_INITIAL_SIZE
        initialSize <= 50 -> 50
        else -> initialSize
    }

    private var output: FloatArray = FloatArray(0)
    private var outputUpToDate = false

    private fun checkFinalized() {
        if (finished) {
            throw IllegalStateException("ArrayFloatList is already finalized!")
        }
    }

    override fun clear() {
        checkFinalized()
        size = 0
        invalidateOutput()
        output = FloatArray(0)
    }

    override fun ensureSize(needed: Int) {
        checkFinalized()
        if (limit - size >= needed) {
            return
        }
        var newSize = data.size
        while (newSize - size < needed) {
            newSize += nextGrowStep
        }
        grow(newSize)
    }

    private fun grow(size: Int) {
        val oldData = data
        data = FloatArray(size)
        System.arraycopy(oldData, 0, data, 0, oldData.size)
    }

    override fun add(value: Float) {
        ensureSize(1)
        data[size++] = value
        invalidateOutput()
    }

    override fun add(array: FloatArray) {
        ensureSize(array.size)
        System.arraycopy(array, 0, data, size, array.size)
        size += array.size
        invalidateOutput()
    }

    override fun add(buffer: FloatBuffer) {
        val position = buffer.position()
        ensureSize(position)
        for (i in 0 until position) {
            data[size + i] = buffer.get(i)
        }
        size += position
        invalidateOutput()
    }

    override fun add(floatList: AbstractFloatList) {
        ensureSize(floatList.size)
        val source: FloatArray = if (floatList is HeapArrayFloatList) {
            if (floatList.finished) {
                floatList.output
            } else {
                floatList.data
            }
        } else {
            floatList.toArray()
        }
        System.arraycopy(source, 0, data, size, floatList.size)
        size += floatList.size
        invalidateOutput()
    }

    private fun checkOutputArray() {
        if (outputUpToDate) {
            return
        }
        output = FloatArray(size)
        System.arraycopy(data, 0, output, 0, size)
        outputUpToDate = true
    }

    override fun toArray(): FloatArray {
        checkOutputArray()
        return output
    }

    override fun finish() {
        finished = true
        checkOutputArray()
        data = FloatArray(0)
    }

    private fun invalidateOutput() {
        if (outputUpToDate) {
            outputUpToDate = false
        }
    }
}
