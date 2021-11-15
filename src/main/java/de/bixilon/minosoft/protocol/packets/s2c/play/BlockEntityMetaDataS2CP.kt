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

import de.bixilon.minosoft.data.entities.block.DefaultBlockEntityMetaDataFactory
import de.bixilon.minosoft.modding.event.events.BlockEntityMetaDataChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import glm_.vec3.Vec3i

class BlockEntityMetaDataS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val position: Vec3i = if (buffer.versionId < ProtocolVersions.V_14W03B) {
        buffer.readShortBlockPosition()
    } else {
        buffer.readBlockPosition()
    }
    val type = buffer.connection.registries.blockEntityMetaDataTypeRegistry[buffer.readUnsignedByte()].resourceLocation
    val nbt = buffer.readNBT().asCompound()

    override fun handle(connection: PlayConnection) {
        connection.world.getBlockEntity(position)?.updateNBT(nbt) ?: let {
            val blockEntity = DefaultBlockEntityMetaDataFactory.buildBlockEntity(DefaultBlockEntityMetaDataFactory[type]!!, connection)
            blockEntity.updateNBT(nbt)
            connection.world.setBlockEntity(position, blockEntity)
        }
        connection.fireEvent(BlockEntityMetaDataChangeEvent(connection, this))
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Block entity meta data (position=$position, type=$type, nbt=$nbt)" }
    }
}
