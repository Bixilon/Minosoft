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

class HeapArrayFloatList(
    initialSize: Int = DEFAULT_INITIAL_SIZE,
) : AbstractFloatList() {
    private var data: FloatArray = FloatArray(initialSize)
    override val limit: Int
        get() = data.size
    override var size = 0
    override val isEmpty: Boolean
        get() = size == 0

    private val nextGrowStep = when {
        initialSize <= 0 -> DEFAULT_INITIAL_SIZE
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
        outputUpToDate = false
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
        if (outputUpToDate) {
            outputUpToDate = false
        }
    }

    override fun addAll(floats: FloatArray) {
        ensureSize(floats.size)
        System.arraycopy(floats, 0, data, size, floats.size)
        size += floats.size
        if (outputUpToDate) {
            outputUpToDate = false
        }
    }

    override fun addAll(floatList: AbstractFloatList) {
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
        if (outputUpToDate) {
            outputUpToDate = false
        }
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


    private companion object {
        private const val DEFAULT_INITIAL_SIZE = 1000
    }
}
