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

package de.bixilon.minosoft.data.container.click

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntArrayList

class FastMoveContainerAction(
    val slot: Int,
) : ContainerAction {
    // ToDo: Action reverting

    override fun invoke(connection: PlayConnection, containerId: Int, container: Container) {
        // ToDo: minecraft always sends a packet
        val source = container.slots[slot] ?: return
        container.lock.lock()
        try {
            val sourceSection = container.getSection(slot) ?: Int.MAX_VALUE

            // loop over all sections and get the lowest slot in the lowest section that fits best
            val targets: MutableList<IntArrayList> = mutableListOf()
            for ((index, section) in container.sections.withIndex()) {
                if (index == sourceSection) {
                    // we don't want to swap into the same section, that is just useless
                    // ToDo: Is this vanilla behavior?
                    continue
                }
                if (section.isEmpty()) {
                    continue
                }
                val list = IntArrayList()
                targets += list
                for (slot in section) {
                    val content = container.slots[slot]
                    if (content != null && !source.matches(content)) { // only check slots that are not empty
                        continue
                    }
                    val type = container.getSlotType(slot) ?: continue
                    if (!type.canPut(container, slot, source)) {
                        // this item is not allowed in this slot (e.g. blocks in armor slot)
                        continue
                    }
                    list += slot
                }
            }
            val changes: Int2ObjectOpenHashMap<ItemStack> = Int2ObjectOpenHashMap()
            sections@ for (list in targets) {
                for (slot in list.intIterator()) {
                    val content = container.slots[slot]
                    if (content == null) {
                        changes[slot] = source
                        changes[this.slot] = null
                        container._set(slot, source)
                        container._set(this.slot, null)
                        break@sections
                    }
                    val countToPut = source.item._count - (source.item.item.maxStackSize - content.item._count)
                    source.item._count -= countToPut
                    content.item._count += countToPut
                    changes[slot] = content
                    changes[this.slot] = source // duplicated
                    if (source.item._count <= 0) {
                        break
                    }
                }
            }

            connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, this.slot, 1, 0, container.createAction(this), changes, null))
        } finally {
            container.commit()
            container._validate()
        }
    }
}
