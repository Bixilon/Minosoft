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
package de.bixilon.minosoft.protocol.packets.s2c.login

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.connection.NetworkConnection
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.channel.vanila.BrandHandler.sendBrand
import de.bixilon.minosoft.protocol.packets.c2s.login.ConfigureC2SP
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class SuccessS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val uuid: UUID = if (buffer.versionId < ProtocolVersions.V_20W12A) buffer.readUUIDString() else buffer.readUUID()
    val name: String = buffer.readString()
    val properties: PlayerProperties? = if (buffer.versionId >= ProtocolVersions.V_22W17A) buffer.readPlayerProperties() else null

    override fun handle(session: PlaySession) {
        if (session.version.hasConfigurationState) {
            session.connection.send(ConfigureC2SP())
            session.connection.unsafeCast<NetworkConnection>().state = ProtocolStates.CONFIGURATION
            session.sendBrand()
            session.settingsManager.sendClientSettings()
        } else {
            session.connection.unsafeCast<NetworkConnection>().state = ProtocolStates.PLAY
        }

        val playerEntity = session.player
        playerEntity.additional.name = name
        playerEntity.additional.displayName = ChatComponent.of(name)

        session.world.entities.add(null, uuid, playerEntity)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Login success (uuid=$uuid, name=$name, properties=$properties)" }
    }
}
