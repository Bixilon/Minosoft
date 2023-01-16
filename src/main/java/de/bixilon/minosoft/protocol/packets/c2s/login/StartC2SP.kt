/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.c2s.login

import de.bixilon.minosoft.data.entities.entities.player.local.LocalPlayerEntity
import de.bixilon.minosoft.protocol.PlayerPublicKey
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

@LoadPacket(state = ProtocolStates.LOGIN)
class StartC2SP(
    val username: String,
    val sessionId: UUID,
    val publicKey: PlayerPublicKey?,
    val profileUUID: UUID? = null,
) : PlayC2SPacket {

    constructor(player: LocalPlayerEntity, sessionId: UUID) : this(player.name, sessionId, player.privateKey?.playerKey, player.uuid)

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeString(username)
        if (buffer.versionId < ProtocolVersions.V_22W43A) {
            if (buffer.versionId >= ProtocolVersions.V_22W42A) {
                buffer.writeUUID(sessionId)
            }
            if (buffer.versionId >= ProtocolVersions.V_22W17A) {
                buffer.writeOptional(publicKey) { buffer.writePublicKey(it) }
            }
        }
        if (buffer.versionId >= ProtocolVersions.V_1_19_1_PRE2) {
            buffer.writeOptional(profileUUID) { buffer.writeUUID(it) }
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Login start (username=$username, publicKey=$publicKey)" }
    }
}
