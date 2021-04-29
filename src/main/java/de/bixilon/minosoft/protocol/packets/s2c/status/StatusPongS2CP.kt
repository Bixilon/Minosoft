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

import de.bixilon.minosoft.data.player.tab.PingBars
import de.bixilon.minosoft.modding.event.events.ServerListPongEvent
import de.bixilon.minosoft.modding.event.events.StatusPongEvent
import de.bixilon.minosoft.protocol.network.connection.StatusConnection
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class StatusPongS2CP(buffer: InByteBuffer) : StatusS2CPacket() {
    val pingId: Long = buffer.readLong()

    override fun handle(connection: StatusConnection) {
        connection.fireEvent(StatusPongEvent(connection, this))
        val ping = connection.connectionStatusPing ?: return
        if (ping.pingId != pingId) {
            Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.WARN) { "Unknown status pong (pingId=$pingId, expected=${ping.pingId})" }
            return
        }
        val pingDeltaTime = System.currentTimeMillis() - ping.sendingTime
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.INFO) { "Status pong (time=$pingDeltaTime, bars=${PingBars.byPing(pingDeltaTime)})" }
        connection.disconnect()
        // ToDo: Log.info(String.format("Server is running on version %s (versionId=%d, protocolId=%d), reconnecting...", connection.getVersion().getVersionName(), connection.getVersion().getVersionId(), connection.getVersion().getProtocolId()));
        val pongEvent = ServerListPongEvent(connection, pingId, pingDeltaTime)
        connection.pong = pongEvent
        connection.fireEvent(pongEvent)
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Status pong (pingId=$pingId)" }
    }
}
