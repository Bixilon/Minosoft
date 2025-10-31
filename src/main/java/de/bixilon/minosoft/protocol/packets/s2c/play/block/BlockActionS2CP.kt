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
package de.bixilon.minosoft.protocol.packets.s2c.play.block

import de.bixilon.minosoft.data.entities.block.BlockActionEntity
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition.FLATTENING_VERSION
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class BlockActionS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position = if (buffer.versionId < ProtocolVersions.V_14W03B) buffer.readShortBlockPosition() else buffer.readBlockPosition()
    val type = buffer.readByte().toInt()
    val data = buffer.readByte().toInt()
    val block = if (buffer.versionId < FLATTENING_VERSION) buffer.readRegistryItem(buffer.session.registries.blockState)?.block else buffer.readRegistryItem(buffer.session.registries.block)

    override fun handle(session: PlaySession) {
        val entity = session.world.getBlockEntity(position) ?: return

        if (entity !is BlockActionEntity) {
            Log.log(LogMessageType.NETWORK_IN, LogLevels.WARN) { "Block entity $entity can not accept block entity actions (type=$type, data=$data)!" }
            return
        }

        entity.setBlockActionData(type, data)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Block action (position=$position, type=$type, data=$data, block=$block)" }
    }
}
