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
import de.bixilon.minosoft.data.container.transaction.ContainerTransaction
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP
import de.bixilon.minosoft.protocol.packets.c2s.play.item.ItemStackCreateC2SP

class SlotSwapContainerAction(
    val sourceId: Int,
    val target: SwapTargets,
) : ContainerAction {

    override fun execute(session: PlaySession, container: Container, transaction: ContainerTransaction) {
        val targetId = container.getSlotSwap(target) ?: return
        val source = transaction[sourceId]
        val target = transaction[targetId]

        if (source == null && target == null) return
        transaction[this.sourceId] = target
        transaction[targetId] = source

        val (id, changes) = transaction.commit()

        if (session.player.gamemode == Gamemodes.CREATIVE && container is PlayerInventory) {
            session.connection += ItemStackCreateC2SP(this.sourceId, target)
            session.connection += ItemStackCreateC2SP(targetId, source)
        } else {
            session.connection += ContainerClickC2SP(container.id, container.serverRevision, sourceId, 2, this.target.button, id, changes, source)
        }
    }

    enum class SwapTargets(val button: Int) {
        HOTBAR_1(0),
        HOTBAR_2(1),
        HOTBAR_3(2),
        HOTBAR_4(3),
        HOTBAR_5(4),
        HOTBAR_6(5),
        HOTBAR_7(6),
        HOTBAR_8(7),
        HOTBAR_9(8),

        OFFHAND(40),
        ;
    }
}
