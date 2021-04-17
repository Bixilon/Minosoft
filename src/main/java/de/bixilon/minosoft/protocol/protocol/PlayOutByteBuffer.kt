/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.protocol

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i

class PlayOutByteBuffer(override val connection: PlayConnection) : OutByteBuffer(connection) {
    val versionId = connection.version.versionId


    fun writeByteArray(data: ByteArray) {
        if (versionId < ProtocolVersions.V_14W21A) {
            writeShort(data.size.toShort())
        } else {
            writeVarInt(data.size)
        }
        super.writeUnprefixedByteArray(data)
    }

    fun writePosition(position: Vec3i?) {
        if (position == null) {
            writeLong(0L)
            return
        }
        if (versionId < ProtocolVersions.V_18W43A) {
            writeLong(position.x.toLong() shl 38 or (position.z.toLong()) or (position.y.toLong() shl 26))
            return
        }
        writeLong((position.x).toLong() shl 38 or ((position.z).toLong() shl 12) or (position.y).toLong())
    }

    fun writeItemStack(itemStack: ItemStack?) {
        if (versionId < ProtocolVersions.V_1_13_2_PRE1) {
            if (itemStack == null) {
                writeShort(-1)
                return
            }
            writeShort(connection.mapping.itemRegistry.getId(itemStack.item))
            writeByte(itemStack.itemCount)
            writeShort(itemStack.itemMetadata)
            writeNBT(itemStack.getNBT())
            return
        }
        if (itemStack == null) {
            writeBoolean(false)
            return
        }
        writeVarInt(connection.mapping.itemRegistry.getId(itemStack.item))
        writeByte(itemStack.itemCount)
        writeNBT(itemStack.getNBT())
    }

    fun writeEntityId(entityId: Int) {
        if (versionId < ProtocolVersions.V_14W04A) {
            writeInt(entityId)
        } else {
            writeVarInt(entityId)
        }
    }
}
