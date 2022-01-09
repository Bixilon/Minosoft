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
package de.bixilon.minosoft.modding.event.events.container

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.registries.other.containers.Container
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.container.ContainerItemS2CP

class ContainerSlotChangeEvent(
    connection: PlayConnection,
    initiator: EventInitiators,
    val container: Container,
    val slot: Int,
    val itemStack: ItemStack?,
) : PlayConnectionEvent(connection, initiator) {

    constructor(connection: PlayConnection, packet: ContainerItemS2CP) : this(connection, EventInitiators.SERVER, connection.player.containers[packet.containerId]!!, packet.slot, packet.itemStack)
}
