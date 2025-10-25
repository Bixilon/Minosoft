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

import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.minosoft.util.collections.floats.FloatListUtil.copy
import de.bixilon.minosoft.util.collections.floats.FloatListUtil.finish
import org.lwjgl.system.MemoryUtil.*
import java.nio.FloatBuffer

class BufferedArrayFloatList(
    initialSize: Int = FloatListUtil.DEFAULT_INITIAL_SIZE,
) : AbstractFloatList(), DirectArrayFloatList {
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
        initialSize <= 0 -> FloatListUtil.DEFAULT_INITIAL_SIZE
        initialSize <= 128 -> 128
        else -> initialSize
    }

    private var output: FloatArray? = null

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
        this.buffer = memRealloc(buffer, size)
        limit = size
    }

    override fun add(value: Float) {
        ensureSize(1)
        buffer.put(value)
        invalidateOutput()
    }

    override fun add(array: FloatArray) {
        ensureSize(array.size)
        buffer.put(array)
        invalidateOutput()
    }

    override fun add(buffer: FloatBuffer) {
        ensureSize(buffer.position())
        buffer.copy(this.buffer)
    }

    override fun add(floatList: AbstractFloatList) {
        ensureSize(floatList.size)
        when (floatList) {
            is FragmentedArrayFloatList -> {
                floatList.forEach { it.copy(this.buffer) }
            }

            is DirectArrayFloatList -> floatList.toBuffer().copy(this.buffer)
            else -> add(floatList.toArray())
        }
        invalidateOutput()
    }

    override fun toArray(): FloatArray {
        output?.let { return it }

        val position = buffer.position()
        val output = FloatArray(position)
        buffer.position(0)
        buffer.get(output, 0, position)
        buffer.position(position)
        this.output = output

        return output
    }

    override fun unload() {
        check(!unloaded) { "Already unloaded!" }
        unloaded = true
        finished = true // Is unloaded
        memFree(buffer)
    }

    override fun clear() {
        buffer.clear()
        invalidateOutput()
    }

    override fun finish() {
        finished = true
        limit = buffer.limit()
        this.buffer = buffer.finish()
    }

    protected fun finalize() {
        if (unloaded) {
            return
        }
        memFree(buffer)
    }

    override fun toBuffer(): FloatBuffer {
        return this.buffer // TODO: Bad! We should only hand a copy of the buffer, the caller is responsible for it now
    }

    private fun invalidateOutput() {
        output = null
    }
}
