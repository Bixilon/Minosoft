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

class ArrayFloatList(
    private val initialSize: Int = DEFAULT_INITIAL_SIZE,
) {
    private var data: FloatArray = FloatArray(initialSize)
    var finalized: Boolean = false
        private set
    val limit: Int
        get() = data.size
    var size = 0
        private set
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
        size = 0
        outputUpToDate = false
        output = FloatArray(0)
    }

    private fun ensureSize(needed: Int) {
        checkFinalized()
        if (limit - size >= needed) {
            return
        }
        var newSize = data.size
        while (newSize - size < needed) {
            newSize += nextGrowStep
        }
        val oldData = data
        data = FloatArray(newSize)
        System.arraycopy(oldData, 0, data, 0, oldData.size)
    }

    fun add(float: Float) {
        ensureSize(1)
        data[size++] = float
        outputUpToDate = false
    }

    fun addAll(floats: FloatArray) {
        ensureSize(floats.size)
        System.arraycopy(floats, 0, data, size, floats.size)
        size += floats.size
        outputUpToDate = false
    }

    fun addAll(floatList: ArrayFloatList) {
        ensureSize(floatList.size)
        val source = if (floatList.finalized) {
            floatList.output
        } else {
            floatList.data
        }
        System.arraycopy(source, 0, data, size, floatList.size)
        size += floatList.size
    }

    private fun checkOutputArray() {
        if (outputUpToDate) {
            return
        }
        output = FloatArray(size)
        System.arraycopy(data, 0, output, 0, size)
        outputUpToDate = true
    }

    fun toArray(): FloatArray {
        checkOutputArray()
        return output
    }

    fun finalize() {
        finalized = true
        checkOutputArray()
        data = FloatArray(0)
    }


    private companion object {
        private const val DEFAULT_INITIAL_SIZE = 1000
    }
}
