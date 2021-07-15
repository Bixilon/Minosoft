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
package de.bixilon.minosoft.protocol.packets.s2c.login

import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.LoginKickEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class LoginKickS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val reason: ChatComponent = buffer.readChatComponent()

    override fun handle(connection: PlayConnection) {
        connection.fireEvent(LoginKickEvent(connection, this))
        Log.log(LogMessageType.NETWORK_STATUS, level = LogLevels.WARN) { "Kicked from: $reason" }
        connection.disconnect()
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Login kick (reason=$reason)" }
    }
}
