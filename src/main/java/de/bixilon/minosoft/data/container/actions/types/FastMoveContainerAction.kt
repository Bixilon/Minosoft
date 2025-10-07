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

package de.bixilon.minosoft.data.container.actions.types

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.actions.ContainerAction
import de.bixilon.minosoft.data.container.sections.ContainerSection
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.transaction.ContainerTransaction
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemStackCreateC2SP
import it.unimi.dsi.fastutil.ints.IntArrayList
import kotlin.collections.iterator

class FastMoveContainerAction(
    val slot: Int,
) : ContainerAction {

    private fun getTargets(sourceSection: ContainerSection?, source: ItemStack, container: Container): Map<ContainerSection, IntArrayList> {
        // loop over all sections and get the lowest slot in the lowest section that fits best

        val targets: MutableMap<ContainerSection, IntArrayList> = mutableMapOf()
        for (section in container.sections) {
            if (section == sourceSection) continue // we don't want to swap into the same section, that is just useless
            if (section.count == 0) continue
            val list = IntArrayList()

            for (slot in section) {
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
            if (list.isEmpty) continue
            targets[section] = list
        }

        return targets
    }

    private fun merge(container: Container, source: ItemStack, transaction: ContainerTransaction, targets: Map<ContainerSection, IntArrayList>) {
        val maxStack = if (source.item is StackableItem) source.item.maxStackSize else 1

        for ((section, list) in targets) {
            val putting = if (section.fillReversed) list.reversed().iterator() else list.intIterator()
            for (slot in putting) {
                val content = container[slot] ?: continue // filling will be done one step afterwards
                val count = if (source.count + content.count > maxStack) maxStack - content.count else source.count
                if (count == 0) continue

                source.count -= count
                content.count += count
                transaction[slot] = content
                if (source.count <= 0) {
                    transaction[this.slot] -= null
                    return
                }
                transaction[this.slot] = source
            }
        }
    }

    private fun move(container: Container, source: ItemStack, transaction: ContainerTransaction, targets: Map<ContainerSection, IntArrayList>) {
        if (source.count <= 0) return

        for ((section, list) in targets) {
            val putting = if (section.fillReversed) list.reversed().iterator() else list.intIterator()
            for (slot in putting) {
                val content = container[slot]
                if (content != null) continue

                transaction[slot] = source
                transaction[this.slot] = null
                return
            }
        }
    }

    override fun invoke(session: PlaySession, container: Container, transaction: ContainerTransaction) {
        // ToDo: minecraft always sends a packet
        val source = container.slots[slot] ?: return

        val targets = getTargets(container.getSection(slot), source, container)

        merge(container, source, transaction, targets)
        move(container, source, transaction, targets)

        val (id, changes) = transaction.commit()
        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            for ((slot, item) in changes) {
                session.connection += ItemStackCreateC2SP(slot, item)
            }
        } else {
            session.connection += ContainerClickC2SP(containerId, container.serverRevision, this.slot, 1, 0, id, changes, null)
        }
    }
}
