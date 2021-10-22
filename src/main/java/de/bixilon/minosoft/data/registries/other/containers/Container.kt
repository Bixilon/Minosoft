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

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.container.ContainerRevisionChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap

open class Container(
    protected val connection: PlayConnection,
    val type: ContainerType,
    val title: ChatComponent? = null,
    val hasTitle: Boolean = false,
) : Iterable<Map.Entry<Int, ItemStack>> {
    protected val slots: MutableMap<Int, ItemStack> = synchronizedMapOf()
    var revision = 0L // ToDo: This has nothing todo with minecraft (1.17+)
        @Synchronized set(value) {
            if (++field != value) {
                error("Can not set a custom revision!")
            }
            connection.fireEvent(ContainerRevisionChangeEvent(connection, EventInitiators.UNKNOWN, this, value))
        }

    fun validate() {
        var changes = false
        for ((slot, itemStack) in slots.toSynchronizedMap()) {
            if (itemStack.count <= 0 || itemStack.durability < 0) {
                slots.remove(slot)
                changes = true
            }
        }
        if (changes) {
            revision++
        }
    }

    operator fun get(slotId: Int): ItemStack? {
        return slots[slotId]
    }

    operator fun set(slotId: Int, itemStack: ItemStack?) {
        if (itemStack == null) {
            slots.remove(slotId) ?: return
        } else {
            slots[slotId] = itemStack // ToDo: Check for changes
        }
        revision++
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
