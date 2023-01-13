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

package de.bixilon.minosoft.data.container.actions.types

import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.actions.ContainerAction
import de.bixilon.minosoft.data.container.sections.ContainerSection
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
        container.lock()
        try {
            val sourceSection = container.getSection(slot) ?: Int.MAX_VALUE

            // loop over all sections and get the lowest slot in the lowest section that fits best
            val targets: MutableList<Pair<ContainerSection, IntArrayList>> = mutableListOf()
            for ((index, section) in container.sections.withIndex()) {
                if (index == sourceSection) {
                    // we don't want to swap into the same section, that is just useless
                    continue
                }
                if (section.count == 0) {
                    continue
                }
                val list = IntArrayList()
                targets += Pair(section, list)
                for (slot in section.iterator()) {
                    val content = container[slot]
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
            val maxStack = source.item.item.maxStackSize
            val changes: Int2ObjectOpenHashMap<ItemStack> = Int2ObjectOpenHashMap()
            sections@ for ((type, list) in targets) {
                val putting = if (type.fillReversed) list.reversed().iterator() else list.intIterator()
                for (slot in putting) {
                    val content = container[slot] ?: continue // filling will be done one step afterwards
                    val countToPut = if (source.item.count + content.item.count > maxStack) maxStack - content.item.count else source.item.count
                    source.item.count -= countToPut
                    content.item.count += countToPut
                    changes[slot] = content
                    if (source.item.count <= 0) {
                        changes[this.slot] = null
                        break@sections
                    }
                    changes[this.slot] = source
                }
            }

            sections@ for ((type, list) in targets) {
                if (source.item.count <= 0) {
                    break
                }
                val putting = if (type.fillReversed) list.reversed().iterator() else list.intIterator()
                for (slot in putting) {
                    val content = container[slot]
                    if (content != null) {
                        continue
                    }
                    changes[slot] = source
                    changes[this.slot] = null
                    container[slot] = source
                    container[this.slot] = null
                    break@sections
                }
            }

            connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, this.slot, 1, 0, container.createAction(this), changes, null))
        } finally {
            container.commit()
        }
    }
}
