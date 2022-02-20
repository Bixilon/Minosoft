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

    fun _validate() {
        var itemsRemoved = 0
        for ((slot, itemStack) in slots.toSynchronizedMap()) {
            if (itemStack._valid) {
                continue
            }
            slots.remove(slot)
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

    fun remove(slotId: Int): ItemStack? {
        try {
            lock.lock()
            val stack = slots.remove(slotId) ?: return null
            stack.holder?.container = null
            revision++
            lock.unlock()
            return stack
        } finally {
            lock.release()
        }
    }

    operator fun set(slotId: Int, itemStack: ItemStack?) {
        if (!internalSet(slotId, itemStack)) {
            return
        }

        revision++
    }

    private fun internalSet(slotId: Int, itemStack: ItemStack?): Boolean {
        lock.lock()
        try {
            val previous = slots[slotId]
            if (itemStack == null) {
                if (previous == null) {
                    return false
                }
                remove(slotId)
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
        } finally {
            lock.unlock()
        }
    }

    fun set(vararg slots: Pair<Int, ItemStack?>) {
        var changes = 0
        for ((slotId, itemStack) in slots) {
            if (internalSet(slotId, itemStack)) {
                changes++
            }
        }
        if (changes > 0) {
            revision++
        }
    }

    fun clear() {
        val size = slots.size
        if (size == 0) {
            return
        }
        slots.clear()
        revision++
    }

    override fun iterator(): Iterator<Map.Entry<Int, ItemStack>> {
        return slots.toSynchronizedMap().iterator()
    }
}
