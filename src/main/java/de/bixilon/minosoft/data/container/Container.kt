/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container

import de.bixilon.kutil.concurrent.lock.thread.ThreadLock
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.observer.map.MapObserver.Companion.observedMap
import de.bixilon.minosoft.data.container.actions.ContainerActions
import de.bixilon.minosoft.data.container.actions.types.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.sections.ContainerSection
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.stack.property.HolderProperty
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.container.ContainerCloseEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.CloseContainerC2SP
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

abstract class Container(
    val connection: PlayConnection,
    val type: ContainerType,
    val title: ChatComponent? = null,
) : Iterable<Map.Entry<Int, ItemStack>> {
    @Deprecated("Should not be accessed directly")
    val slots: MutableMap<Int, ItemStack> by observedMap(Int2ObjectOpenHashMap())
    val lock = ThreadLock()
    var propertiesRevision by observed(0L)
    var revision by observed(0L)
    var serverRevision = 0
    var floatingItem: ItemStack? by observed(null)
    val actions = ContainerActions(this)

    val id: Int?
        get() = connection.player.containers.getKey(this)

    open val sections: Array<ContainerSection> get() = emptyArray()

    init {
        this::floatingItem.observe(this) { it?.holder?.container = this }
    }

    var edit: ContainerEdit? = null

    fun validate() {
        lock.lock()

        if (floatingItem?._valid == false) {
            floatingItem = null
            edit?.addChange()
        }
        val iterator = slots.iterator()
        for ((slot, stack) in iterator) {
            if (stack._valid) {
                continue
            }
            stack.holder?.container = null
            iterator.remove()
            edit?.addChange()
        }

        internalCommit()
    }

    open fun getSlotType(slotId: Int): SlotType? = DefaultSlotType
    open fun getSlotSwap(slot: SlotSwapContainerAction.SwapTargets): Int? = null

    open fun getSection(slotId: Int): Int? {
        for ((index, section) in sections.withIndex()) {
            if (slotId in section) {
                return index
            }
        }
        return null
    }

    operator fun get(slotId: Int): ItemStack? {
        try {
            lock.acquire()
            return slots[slotId]
        } finally {
            lock.release()
        }
    }


    protected open fun onRemove(slotId: Int, stack: ItemStack) = Unit

    private fun _remove(slotId: Int): ItemStack? {
        val stack = slots.remove(slotId) ?: return null
        onRemove(slotId, stack)
        stack.holder?.container = null
        return stack
    }

    fun remove(slotId: Int): ItemStack? {
        lock.lock()
        val remove = _remove(slotId)
        if (remove != null) {
            edit?.addChange()
        }
        internalCommit()
        return remove
    }

    operator fun minusAssign(slotId: Int) {
        remove(slotId)
    }

    open operator fun set(slotId: Int, itemStack: ItemStack?) {
        lock.lock()
        if (_set(slotId, itemStack)) {
            edit?.addChange()
        }
        internalCommit()
    }

    protected open fun onSet(slotId: Int, stack: ItemStack?) = Unit

    protected fun _set(slotId: Int, stack: ItemStack?): Boolean {
        val previous = slots[slotId]
        if (stack == null) {
            // remove item
            if (previous == null) {
                return false
            }
            _remove(slotId)
            return true
        }
        if (previous == stack) {
            return false
        }
        slots[slotId] = stack // ToDo: Check for changes
        var holder = stack.holder
        if (holder == null) {
            holder = HolderProperty(connection, this)
            stack.holder = holder
        } else {
            holder.container = this
        }
        onSet(slotId, stack)

        return true
    }

    fun set(vararg slots: Pair<Int, ItemStack?>) {
        if (slots.isEmpty()) {
            return
        }
        lock.lock()
        for ((slotId, itemStack) in slots) {
            if (_set(slotId, itemStack)) {
                edit?.addChange()
            }
        }
        internalCommit()
    }

    fun _clear() {
        for (stack in slots.values) {
            stack.holder?.container = null
        }
        if (slots.isNotEmpty()) {
            edit?.addChange()
        }
    }

    fun clear() {
        lock.lock()
        _clear()
        internalCommit()
    }

    fun close(force: Boolean = false) {
        connection.events.fire(ContainerCloseEvent(connection, this))
        onClose()

        val id = id ?: return

        if (id != PlayerInventory.CONTAINER_ID || this is ClientContainer) {
            connection.player.containers -= id
        }


        if (force && connection.player.openedContainer == this) {
            connection.player.openedContainer = null
            connection.sendPacket(CloseContainerC2SP(id))
        }
    }

    protected open fun onClose() {
        floatingItem = null // ToDo: Does not seem correct
    }

    override fun iterator(): Iterator<Map.Entry<Int, ItemStack>> {
        return slots.iterator()
    }

    fun lock() {
        lock.lock()
        if (edit == null) {
            edit = ContainerEdit()
        }
    }

    fun internalCommit() {
        val edit = this.edit
        lock.unlock()
        if (edit == null) {
            revision++
        }
    }

    fun commit() {
        val edit = this.edit ?: throw IllegalStateException("Not in bulk edit mode!")
        validate()
        lock.unlock()
        for (slot in edit.slots) {
            slot.revision++
        }
        revision++
    }

    open fun readProperty(property: Int, value: Int) = Unit
}
