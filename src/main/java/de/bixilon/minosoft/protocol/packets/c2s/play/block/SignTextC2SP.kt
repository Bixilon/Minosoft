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
package de.bixilon.minosoft.protocol.packets.c2s.play.block

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.block.sign.SignBlockEntity
import de.bixilon.minosoft.data.entities.block.sign.SignSides
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class SignTextC2SP(
    val position: Vec3i,
    val side: SignSides,
    val lines: Array<ChatComponent>,
) : PlayC2SPacket {

    init {
        check(lines.size == SignBlockEntity.LINES) { "A sign has ${SignBlockEntity.LINES} lines (not ${lines.size})!" }
    }

    override fun write(buffer: PlayOutByteBuffer) {
        if (buffer.versionId < ProtocolVersions.V_14W04A) {
            buffer.writeByteBlockPosition(position)
        } else {
            buffer.writeBlockPosition(position)
        }
        if (buffer.versionId >= ProtocolVersions.V_23W12A) {
            buffer.writeBoolean(if (side == SignSides.FRONT) true else false)
        }
        for (line in lines) {
            buffer.writeString(line.message)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_OUT, LogLevels.VERBOSE) { "Sign text (position=$position)" }
    }
}
