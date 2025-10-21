/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.s2c.play.container

import de.bixilon.kutil.concurrent.lock.LockUtil.locked
import de.bixilon.minosoft.data.container.IncompleteContainer
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_17_1_PRE1
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class ContainerItemS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val containerId = buffer.readUnsignedByte()
    val revision: Int = if (buffer.versionId >= V_1_17_1_PRE1) buffer.readVarInt() else -1
    val slot = buffer.readShort().toInt()
    val stack = buffer.readItemStack()

    override fun handle(session: PlaySession) {
        val container = session.player.items.containers[containerId] ?: return

        container.lock.locked {
            if (slot < 0) {
                container.floating = stack
            } else {
                container.items[slot] = stack
            }
            container.serverRevision = revision
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Container item (containerId=$containerId, revision=$revision, slot=$slot, stack=$stack)" }
    }
}
