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

import de.bixilon.kutil.collections.CollectionUtil.synchronizedBiMapOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedMap
import de.bixilon.kutil.collections.map.bi.SynchronizedBiMap
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.kutil.observer.map.MapObserver.Companion.observedMap
import de.bixilon.minosoft.data.container.click.ContainerAction
import de.bixilon.minosoft.data.container.click.SlotSwapContainerAction
import de.bixilon.minosoft.data.container.sections.ContainerSection
import de.bixilon.minosoft.data.container.slots.DefaultSlotType
import de.bixilon.minosoft.data.container.slots.SlotType
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.stack.property.HolderProperty
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.containers.ContainerFactory
import de.bixilon.minosoft.data.registries.containers.ContainerType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.CloseContainerC2SP
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

open class Container(
    val connection: PlayConnection,
    val type: ContainerType,
    val title: ChatComponent? = null,
) : Iterable<Map.Entry<Int, ItemStack>> {
    @Deprecated("Should not be accessed directly")
    val slots: MutableMap<Int, ItemStack> by observedMap(Int2ObjectOpenHashMap())
    val lock = SimpleLock()
    var propertiesRevision by observed(0L)
    var revision by observed(0L)
    var serverRevision = 0
    private var lastActionId = 0
    var actions: SynchronizedBiMap<Int, ContainerAction> = synchronizedBiMapOf()
    var floatingItem: ItemStack? by observed(null)

    val id: Int?
        get() = connection.player.containers.getKey(this)

    open val sections: Array<ContainerSection> get() = emptyArray()

    init {
        this::floatingItem.observe(this) { it?.holder?.container = this }
    }

    fun _validate() {
        var itemsRemoved = 0
        if (floatingItem?._valid == false) {
            floatingItem = null
            itemsRemoved++
        }
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

    open fun _remove(slotId: Int): ItemStack? {
        val stack = slots.remove(slotId) ?: return null
        stack.holder?.container = null
        return stack
    }

    open fun remove(slotId: Int): ItemStack? {
        lock.lock()
        val remove = _remove(slotId)
        lock.unlock()
        if (remove != null) {
            revision++
        }
        return remove
    }

    open operator fun set(slotId: Int, itemStack: ItemStack?) {
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

    open fun _set(slotId: Int, itemStack: ItemStack?): Boolean {
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

    fun _clear() {
        for (stack in slots.values) {
            stack.holder?.container = null
        }
        slots.clear()
    }

    fun clear() {
        lock.lock()
        _clear()
        lock.unlock()
        revision++
    }

    @Synchronized
    fun createAction(action: ContainerAction): Int {
        val nextId = lastActionId++
        actions[nextId] = action
        return nextId
    }

    @Synchronized
    fun invokeAction(action: ContainerAction) {
        action.invoke(connection, id ?: return, this)
    }

    fun acknowledgeAction(actionId: Int) {
        actions.remove(actionId)
    }

    fun revertAction(actionId: Int) {
        actions.remove(actionId)?.let { revertAction(it) }
    }

    fun revertAction(action: ContainerAction) {
        action.revert(connection, id ?: return, this)
    }

    fun onClose() {
        floatingItem = null // ToDo: Does not seem correct
        val id = id ?: return

        if (id != PlayerInventory.CONTAINER_ID) {
            connection.player.containers -= id
        }
        // minecraft behavior, when opening the inventory an open packet is never sent, but a close is

        if (connection.player.openedContainer == this) {
            connection.sendPacket(CloseContainerC2SP(id))
        }
    }

    override fun iterator(): Iterator<Map.Entry<Int, ItemStack>> {
        return slots.iterator()
    }

    fun commit() {
        lock.unlock()
        revision++
    }

    open fun readProperty(property: Int, value: Int) = Unit

    companion object : ContainerFactory<Container> {
        override val identifier: ResourceLocation = minecraft("container")

        override fun build(connection: PlayConnection, type: ContainerType, title: ChatComponent?): Container {
            return Container(connection, type, title)
        }
    }
}
