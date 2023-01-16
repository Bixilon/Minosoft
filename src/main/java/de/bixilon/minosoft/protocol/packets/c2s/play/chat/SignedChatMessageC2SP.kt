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
package de.bixilon.minosoft.protocol.packets.c2s.play.chat

import de.bixilon.minosoft.data.chat.signature.Acknowledgement
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.time.Instant

@LoadPacket(threadSafe = false)
class SignedChatMessageC2SP(
    val message: ByteArray,
    val time: Instant = Instant.now(),
    val salt: Long,
    val signature: ByteArray? = null,
    val previewed: Boolean = false,
    val acknowledgement: Acknowledgement?,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        if (buffer.versionId == ProtocolVersions.V_22W17A) {
            buffer.writeInstant(time)
        }
        buffer.writeByteArray(message)
        if (buffer.versionId >= ProtocolVersions.V_22W18A) {
            buffer.writeInstant(time)
        }
        buffer.writeLong(salt)

        if (buffer.versionId >= ProtocolVersions.V_22W42A) {
            buffer.writeOptional(signature) { buffer.writeSignatureData(it) }
        } else {
            buffer.writeSignatureData(signature ?: KUtil.EMPTY_BYTE_ARRAY)
        }

        if (buffer.versionId >= ProtocolVersions.V_22W19A && buffer.versionId < ProtocolVersions.V_22W42A) {
            buffer.writeBoolean(previewed)
        }
        if (buffer.versionId >= ProtocolVersions.V_1_19_1_PRE5) {
            buffer.writeAcknowledgement(acknowledgement!!)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_OUT, LogLevels.VERBOSE) { "Signed chat message (message=$message, time=$time, signature=$signature, previewed=$previewed)" }
    }
}
