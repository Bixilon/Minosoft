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

package de.bixilon.minosoft.gui.rendering.util.allocator

import de.bixilon.kutil.concurrent.lock.locks.reentrant.ReentrantLock
import java.lang.ref.WeakReference


@Deprecated("kutil 1.27.1")
abstract class TemporaryAllocator<T> {
    private val lock = ReentrantLock()
    private val list: ArrayList<WeakReference<T>> = ArrayList()

    private fun cleanup() {
        if (list.size < 10) return
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

    fun free(array: T) {
        lock.lock()
        cleanup()
        list.add(0, WeakReference(array))
        lock.unlock()
    }

    private fun find(size: Int): T? {
        if (this.list.isEmpty()) return null
        lock.lock()

        var array: T? = null
        val iterator = list.iterator()
        while (iterator.hasNext()) {
            val reference = iterator.next()
            val entry = reference.get()
            if (entry == null) {
                iterator.remove()
                continue
            }
            if (getSize(entry) >= size) {
                array = entry
                iterator.remove()
                break
            }
        }
        lock.unlock()

        return array
    }

    fun allocate(size: Int): T {
        val array = find(size)
        if (array != null) return array

        // println("Allocating array of size $size")

        return create(size)
    }

    protected abstract fun getSize(value: T): Int

    protected abstract fun create(size: Int): T
}
