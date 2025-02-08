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

package de.bixilon.minosoft.data.world.container.palette.data.array

import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantLock
import java.lang.ref.WeakReference


object LongArrayAllocator {
    private val lock = ReentrantLock()
    private val list: ArrayList<WeakReference<LongArray>> = ArrayList()

    private fun cleanup() {
        lock.lock()
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val reference = iterator.next()
            if (reference.get() == null) {
                iterator.remove()
            }
        }
        lock.unlock()
    }

    fun free(array: LongArray) {
        lock.lock()
        cleanup()
        list.add(0, WeakReference(array))
        lock.unlock()
    }

    fun claim(size: Int): LongArray {
        lock.lock()

        var array: LongArray? = null
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val reference = iterator.next()
            array = reference.get()
            if (array == null) {
                iterator.remove()
                continue
            }
            if (array.size >= size) {
                break
            }
        }
        lock.unlock()

        if (array != null) return array

        // println("Allocating long array of size $size")

        return LongArray(size)
    }
}
