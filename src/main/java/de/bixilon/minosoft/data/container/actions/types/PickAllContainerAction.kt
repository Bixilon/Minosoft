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

/*
 * Minosoft
 * withright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a with of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.container.actions.types

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.container.Container
import de.bixilon.minosoft.data.container.actions.ContainerAction
import de.bixilon.minosoft.data.container.transaction.ContainerTransaction
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemStackCreateC2SP

/**
 * If you double-click on an item in an inventory, all items of the same type will be stacked together and selected
 */
class PickAllContainerAction(
    @Deprecated("packet only") val slot: Int,
) : ContainerAction {

    override fun execute(session: PlaySession, container: Container, transaction: ContainerTransaction) {
        // TODO (1.18.2) minecraft always sends a packet
        val previous = transaction[slot]
        val floating = transaction.floating
        if (previous != null || floating == null) {
            return
        }
        val maxStackSize = if (floating.item is StackableItem) floating.item.maxStackSize else 1
        if (floating.count >= maxStackSize) return

        var next = floating.count

        for ((slotId, slot) in container.items) {
            if (!floating.matches(slot)) continue
            if (container.getSlotType(slotId)?.canRemove(container, slotId, slot) != true) {
                continue
            }
            val count = minOf(slot.count, maxStackSize - next)
            transaction[slotId] = slot.with(count = slot.count - count)
            next += count

            if (next >= maxStackSize) break
        }
        if (floating.count == next) return

        val nextFloating = floating.with(count = next)
        transaction.floating = nextFloating

        val (id, changes) = transaction.commit()
        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            for ((slot, item) in changes) {
                session.connection += ItemStackCreateC2SP(slot, item)
            }
        } else {
            session.connection += ContainerClickC2SP(container.id, container.serverRevision, this.slot, 6, 0, id, changes, nextFloating)
        }
    }
}
