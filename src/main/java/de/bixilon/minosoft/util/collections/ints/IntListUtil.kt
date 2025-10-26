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

package de.bixilon.minosoft.util.collections.ints

import de.bixilon.kutil.collections.primitive.ints.HeapIntList

object IntListUtil {
    const val PREFER_FRAGMENTED = false // realloc is SO fast, the kernel just swaps the page table entries.
    const val ALLOW_NATIVE = true

    const val DEFAULT_INITIAL_SIZE = 1024

    fun direct(initialSize: Int = DEFAULT_INITIAL_SIZE, fragmented: Boolean = PREFER_FRAGMENTED) = when {
        !ALLOW_NATIVE -> HeapIntList(initialSize)
        else -> BufferIntList(initialSize)
    }
}
