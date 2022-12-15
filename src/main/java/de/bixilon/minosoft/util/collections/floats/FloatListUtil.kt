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

import de.bixilon.kutil.exception.ExceptionUtil
import org.lwjgl.system.MemoryUtil.memAllocFloat
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.FloatBuffer

object FloatListUtil {
    const val PREFER_FRAGMENTED = false

    val FLOAT_PUT_METHOD = ExceptionUtil.catchAll { FloatBuffer::class.java.getMethod("put", Int::class.java, FloatBuffer::class.java, Int::class.java, Int::class.java) }
    const val DEFAULT_INITIAL_SIZE = 1000

    fun direct(initialSize: Int = DEFAULT_INITIAL_SIZE): AbstractFloatList {
        return if (PREFER_FRAGMENTED) FragmentedArrayFloatList(initialSize) else BufferedArrayFloatList(initialSize)
    }

    fun FloatBuffer.finish(): FloatBuffer {
        val buffer = memAllocFloat(position())
        this.copy(buffer)
        memFree(this)
        return buffer
    }

    fun FloatBuffer.copy(sourceOffset: Int, destination: FloatBuffer, destinationOffset: Int, length: Int) {
        if (length == 0) {
            return
        }
        if (FLOAT_PUT_METHOD == null) { // Java < 16
            for (i in 0 until length) {
                destination.put(destinationOffset + i, this.get(sourceOffset + i))
            }
            destination.position(destination.position() + length)
            return
        }
        FLOAT_PUT_METHOD.invoke(destination, destinationOffset, this, sourceOffset, length)
        destination.position(destination.position() + length)
    }

    fun FloatBuffer.copy(destination: FloatBuffer) {
        copy(0, destination, destination.position(), position())
    }
}
