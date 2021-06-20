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

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.PluginMessageS2CP
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer

class PluginMessageReceiveEvent(
    connection: PlayConnection,
    initiator: EventInitiators,
    val channel: ResourceLocation,
    data: PlayInByteBuffer,
) : PlayConnectionEvent(connection, initiator), CancelableEvent {
    val data: PlayInByteBuffer = data
        get() = PlayInByteBuffer(field)

    constructor(connection: PlayConnection, packet: PluginMessageS2CP) : this(connection, EventInitiators.SERVER, packet.channel, packet.data)
}
