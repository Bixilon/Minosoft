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
import de.bixilon.minosoft.data.container.ContainerUtil.slotsOf
import de.bixilon.minosoft.data.container.actions.ContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.transaction.ContainerTransaction
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemStackCreateC2SP

class DropContainerAction(
    val count: ContainerCounts,
) : ContainerAction {


    override fun invoke(session: PlaySession, container: Container, transaction: ContainerTransaction) {
        val previous = container.floating.count
        if (count == ContainerCounts.ALL) {
            floatingItem.item._count = 0
        } else {
            floatingItem.item._count-- // don't use decrease, item + container is already locked
        }
        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            session.connection += ItemStackCreateC2SP(-1, if (count == ContainerCounts.ALL) floatingItem.copy(count = previous) else floatingItem.copy(count = 1))
        } else {
            session.connection += ContainerClickC2SP(container.id, container.serverRevision, null, 0, count.ordinal, container.actions.createId(this), slotsOf(), null)
        }
    }

    enum class ContainerCounts {
        ALL,
        PART,
        ;
    }
}
