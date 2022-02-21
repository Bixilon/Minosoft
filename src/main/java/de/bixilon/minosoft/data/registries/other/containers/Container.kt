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

package de.bixilon.minosoft.data.registries.other.containers

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.kutil.concurrent.lock.SimpleLock
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.data.inventory.stack.ItemStack
import de.bixilon.minosoft.data.inventory.stack.property.HolderProperty
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class Container(
    protected val connection: PlayConnection,
    val type: ContainerType,
    val title: ChatComponent? = null,
    val hasTitle: Boolean = false,
) : Iterable<Map.Entry<Int, ItemStack>> {
    protected val slots: MutableMap<Int, ItemStack> = mutableMapOf()
    val lock = SimpleLock()
    var revision by watched(0L) // ToDo: This has nothing todo with minecraft (1.17+)
    var serverRevision = 0

    fun _validate() {
        var itemsRemoved = 0
        for ((slot, itemStack) in slots.toSynchronizedMap()) {
            if (itemStack._valid) {
                continue
            }
            _remove(slot)
            itemStack.holder?.container = null
            itemsRemoved++
        }
        if (itemsRemoved > 0) {
            revision++
        }
    }

    fun validate() {
        lock.lock()
        _validate()
        lock.unlock()
    }

    operator fun get(slotId: Int): ItemStack? {
        try {
            lock.acquire()
            return slots[slotId]
        } finally {
            lock.release()
        }
    }

    fun _remove(slotId: Int): ItemStack? {
        val stack = slots.remove(slotId) ?: return null
        stack.holder?.container = null
        return stack
    }

    fun remove(slotId: Int): ItemStack? {
        lock.lock()
        val remove = _remove(slotId)
        lock.unlock()
        if (remove != null) {
            revision++
        }
        return remove
    }

    operator fun set(slotId: Int, itemStack: ItemStack?) {
        try {
            lock.lock()
            if (!_set(slotId, itemStack)) {
                return
            }
        } finally {
            lock.unlock()
        }

        revision++
    }

    private fun _set(slotId: Int, itemStack: ItemStack?): Boolean {
        val previous = slots[slotId]
        if (itemStack == null) {
            if (previous == null) {
                return false
            }
            _remove(slotId)
            return true
        }
        if (previous == itemStack) {
            return false
            }
            slots[slotId] = itemStack // ToDo: Check for changes
            var holder = itemStack.holder
            if (holder == null) {
                holder = HolderProperty(connection, this)
                itemStack.holder = holder
            } else {
                holder.container = this
            }

            return true
    }

    fun set(vararg slots: Pair<Int, ItemStack?>) {
        if (slots.isEmpty()) {
            return
        }
        lock.lock()
        var changes = 0
        for ((slotId, itemStack) in slots) {
            if (_set(slotId, itemStack)) {
                changes++
            }
        }
        lock.unlock()
        if (changes > 0) {
            revision++
        }
    }

    fun clear() {
        lock.lock()
        val size = slots.size
        if (size == 0) {
            lock.unlock()
            return
        }
        for (stack in slots.values) {
            stack.holder?.container = null
        }
        slots.clear()
        lock.unlock()
        revision++
    }

    override fun iterator(): Iterator<Map.Entry<Int, ItemStack>> {
        return slots.toSynchronizedMap().iterator()
    }
}
