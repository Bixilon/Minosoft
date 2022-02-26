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
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.c2s.play.container.ContainerClickC2SP

class SlotSwapContainerAction(
    val sourceId: Int,
    val target: SwapTargets,
) : ContainerAction {

    override fun invoke(connection: PlayConnection, containerId: Int, container: Container) {
        val targetId = container.getSlotSwap(target) ?: return
        container.lock.lock()
        try {
            val source = container.slots[sourceId]
            val target = container.slots[targetId]

            if (source == null && target == null) {
                return
            }
            container._set(this.sourceId, target)
            container._set(targetId, source)

            connection.sendPacket(ContainerClickC2SP(containerId, container.serverRevision, sourceId, 2, this.target.button, container.createAction(this), mapOf(sourceId to target, targetId to source), source))

        } finally {
            container.lock.unlock()
        }
    }

    override fun revert(connection: PlayConnection, containerId: Int, container: Container) {
        val targetId = container.getSlotSwap(target) ?: return
        container.lock.lock()
        val target = container.slots[targetId]
        val source = container.slots[sourceId]
        container._set(sourceId, target)
        container._set(targetId, source)
        container.lock.unlock()
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
