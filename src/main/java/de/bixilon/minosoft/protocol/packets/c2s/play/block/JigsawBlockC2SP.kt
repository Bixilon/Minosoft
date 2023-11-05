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
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W13A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W43A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class JigsawBlockC2SP(
    val position: Vec3i,
    val targetPool: String,
    val finalState: String,
    var attachmentType: String,
    var name: String,
    var target: String,
    var jointType: String,
    var selectionPriority: Int,
    var placementPriority: Int,
) : PlayC2SPacket {

    override fun write(buffer: PlayOutByteBuffer) {
        buffer.writeBlockPosition(position)
        if (buffer.versionId < V_20W13A) {
            buffer.writeString(attachmentType)
        } else {
            buffer.writeString(name)
            buffer.writeString(target)
        }
        buffer.writeString(targetPool)
        buffer.writeString(finalState)

        if (buffer.versionId >= V_20W13A) {
            buffer.writeString(jointType)
        }
        if (buffer.versionId >= V_23W43A) {
            buffer.writeVarInt(selectionPriority)
            buffer.writeVarInt(placementPriority)
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_OUT, LogLevels.VERBOSE) { "Jigsaw block (position=$position, targetPool=$targetPool, finalState=$finalState, attachmentType=$attachmentType, name=$name, target=$target, jointType=$jointType)" }
    }
}
