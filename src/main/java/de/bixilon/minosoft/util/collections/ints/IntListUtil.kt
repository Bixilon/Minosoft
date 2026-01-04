/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.collections.ints

import de.bixilon.kutil.collections.primitive.ints.HeapIntList
import de.bixilon.minosoft.util.collections.MemoryOptions
import java.nio.IntBuffer

object IntListUtil {
    const val PREFER_FRAGMENTED = false // realloc is SO fast, the kernel just swaps the page table entries.

    const val DEFAULT_INITIAL_SIZE = 1024

    fun direct(initialSize: Int = DEFAULT_INITIAL_SIZE, fragmented: Boolean = PREFER_FRAGMENTED) = when {
        !MemoryOptions.native -> HeapIntList(initialSize)
        else -> BufferIntList(initialSize)
    }


    fun IntBuffer.copy(sourceOffset: Int, destination: IntBuffer, destinationOffset: Int, length: Int) {
        if (length == 0) return

        val sourceLimit = this.limit()
        val sourcePosition = this.position()

        val destinationLimit = destination.limit()
        val destinationPositon = destination.position()

        this.limit(sourceOffset + length); this.position(sourceOffset)
        destination.limit(destinationOffset + length); destination.position(destinationOffset)

        destination.put(this)

        this.limit(sourceLimit); this.position(sourcePosition)
        destination.limit(destinationLimit); destination.position(destinationPositon)
    }
}
