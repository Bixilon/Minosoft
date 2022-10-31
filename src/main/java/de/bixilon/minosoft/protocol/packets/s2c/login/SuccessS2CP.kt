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
package de.bixilon.minosoft.protocol.packets.s2c.login

import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

@LoadPacket(state = ProtocolStates.LOGIN, threadSafe = false)
class SuccessS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val uuid: UUID = (buffer.versionId < ProtocolVersions.V_20W12A).decide({ buffer.readUUIDString() }, { buffer.readUUID() })
    val name: String = buffer.readString()
    val properties: PlayerProperties? = if (buffer.versionId >= ProtocolVersions.V_22W17A) buffer.readPlayerProperties() else null

    override fun handle(connection: PlayConnection) {
        connection.network.state = ProtocolStates.PLAY

        val playerEntity = connection.player
        playerEntity.additional.name = name
        playerEntity.additional.displayName = ChatComponent.of(name)

        connection.world.entities.add(null, uuid, playerEntity)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Login success (uuid=$uuid, name=$name, properties=$properties)" }
    }
}
