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
package de.bixilon.minosoft.protocol.packets.s2c.status

import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.connection.status.StatusPongReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnectionStates
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class StatusPongS2CP(buffer: InByteBuffer) : StatusS2CPacket() {
    val pingId: Long = buffer.readLong()

    override fun handle(connection: StatusConnection) {
        val pingQuery = connection.pingQuery ?: return
        if (pingQuery.pingId != pingId) {
            Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.WARN) { "Unknown status pong (pingId=$pingId, expected=${pingQuery.pingId})" }
           // return ToDo: feather-rs is sending a wrong ping id back
        }
        val latency = System.currentTimeMillis() - pingQuery.time
        connection.disconnect()
        // ToDo: Log.info(String.format("Server is running on version %s (versionId=%d, protocolId=%d), reconnecting...", connection.getVersion().getVersionName(), connection.getVersion().getVersionId(), connection.getVersion().getProtocolId()));
        val pongEvent = StatusPongReceiveEvent(connection, EventInitiators.SERVER, pingId, latency)
        connection.lastPongEvent = pongEvent
        connection.fireEvent(pongEvent)
        connection.state = StatusConnectionStates.PING_DONE
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Status pong (pingId=$pingId)" }
    }
}
