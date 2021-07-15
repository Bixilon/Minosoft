/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.modding.event.events

import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.BlockSetS2CP
import glm_.vec3.Vec3i

/**
 * Fired when one block is changed
 */
class BlockSetEvent(
    connection: PlayConnection,
    initiator: EventInitiators = EventInitiators.DEFAULT,
    val blockPosition: Vec3i,
    val blockState: BlockState?,
) : PlayConnectionEvent(connection, initiator) {

    constructor(connection: PlayConnection, packet: BlockSetS2CP) : this(connection, EventInitiators.SERVER, packet.blockPosition, packet.blockState)
}
