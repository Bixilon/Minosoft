/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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
import de.bixilon.minosoft.data.container.ContainerUtil.slotsOf
import de.bixilon.minosoft.data.container.actions.ContainerAction
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP

class DropContainerAction(
    val slot: Int,
    val stack: Boolean,
) : ContainerAction {
    private var previousStack: ItemStack? = null

    override fun invoke(session: PlaySession, containerId: Int, container: Container) {
        val item = container[slot] ?: return
        if (container.getSlotType(this.slot)?.canRemove(container, slot, item) != true) {
            return
        }
        container.lock()
        previousStack = item.copy()
        if (stack) {
            item.item.count = 0
        } else {
            item.item.decreaseCount()
        }

        val actionId = container.actions.createId(this)
        session.connection.send(ContainerClickC2SP(containerId, container.serverRevision, slot, 4, if (stack) 1 else 0, actionId, slotsOf(slot to item), null))

        // TODO (1.18.2): use creative inventory packet
        container.commit()
    }

    override fun revert(session: PlaySession, containerId: Int, container: Container) {
        container[slot] = previousStack
    }
}
