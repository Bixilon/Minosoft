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
package de.bixilon.minosoft.protocol.packets.c2s.common

import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_20_3_PRE1
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

class ResourcepackC2SP(
    val uuid: UUID?,
    val hash: String,
    val status: ResourcePackStates,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        if (buffer.versionId >= V_1_20_3_PRE1) {
            buffer.writeUUID(uuid!!)
        }
        if (buffer.versionId < ProtocolVersions.V_1_10_PRE1) {
            buffer.writeString(hash)
        }
        buffer.writeVarInt(status.ordinal)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_OUT, LogLevels.VERBOSE) { "Resourcepack (hash=$hash, status=$status)" }
    }

    enum class ResourcePackStates {
        SUCCESSFULLY,
        DECLINED,
        FAILED_DOWNLOAD,
        ACCEPTED,
        INVALID_URL,
        FAILED_RELOAD,
        DISCARDED,
        ;
    }
}
