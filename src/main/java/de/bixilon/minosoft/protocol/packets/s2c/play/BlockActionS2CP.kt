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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.data.entities.block.BlockActionEntity
import de.bixilon.minosoft.data.entities.block.DefaultBlockEntityMetaDataFactory
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.datafixer.BlockEntityFixer.fix
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec3.Vec3i

class BlockActionS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val position: Vec3i = if (buffer.versionId < ProtocolVersions.V_14W03B) {
        buffer.readShortBlockPosition()
    } else {
        buffer.readBlockPosition()
    }
    val data1: Byte = buffer.readByte()
    val data2: Byte = buffer.readByte()
    val block: Block = buffer.connection.registries.blockRegistry[buffer.readVarInt()]

    override fun handle(connection: PlayConnection) {
        val blockEntity = connection.world.getBlockEntity(position) ?: let {
            val fixedResourceLocation = block.resourceLocation.fix()
            val factory = connection.registries.blockEntityTypeRegistry[fixedResourceLocation]?.factory
                ?: DefaultBlockEntityMetaDataFactory[fixedResourceLocation]
                ?: let {
                    Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.WARN) { "Unknown block entity $fixedResourceLocation" }
                    return
                }
            val blockEntity = factory.build(connection)
            connection.world.setBlockEntity(position, blockEntity)
            blockEntity
        }

        if (blockEntity !is BlockActionEntity) {
            Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.WARN) { "Block entity $blockEntity can not accept block entity actions!" }
            return
        }
        blockEntity.setBlockActionData(data1, data2)
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Block action (position=$position, data1=$data1, data2=$data2, block=$block)" }
    }
}
