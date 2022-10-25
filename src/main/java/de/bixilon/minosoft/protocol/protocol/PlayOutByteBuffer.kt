/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.chat.signature.Acknowledgement
import de.bixilon.minosoft.data.chat.signature.ChatSignatureProperties
import de.bixilon.minosoft.data.chat.signature.LastSeenMessageList
import de.bixilon.minosoft.data.chat.signature.lastSeen.LastSeenMessage
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.protocol.PlayerPublicKey
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W43A
import java.time.Instant
import java.util.*

class PlayOutByteBuffer(val connection: PlayConnection) : OutByteBuffer() {
    val versionId = connection.version.versionId


    override fun writeByteArray(data: ByteArray) {
        if (versionId < ProtocolVersions.V_14W21A) {
            writeShort(data.size.toShort())
        } else {
            writeVarInt(data.size)
        }
        super.writeBareByteArray(data)
    }

    fun writePosition(position: Vec3i?) {
        when {
            position == null -> writeLong(0L) // 0,0,0
            versionId < V_18W43A -> writeLong(position.x.toLong() and 0x3FFFFFF shl 38 or (position.z.toLong() and 0x3FFFFFF) or (position.y.toLong() and 0xFFF shl 26))
            else -> writeLong((position.x.toLong() and 0x3FFFFFF shl 38) or ((position.z).toLong() and 0x3FFFFFF shl 12) or (position.y.toLong() and 0xFFF))
        }
    }

    fun writeItemStack(itemStack: ItemStack?) {
        if (versionId < ProtocolVersions.V_1_13_2_PRE1) {
            if (itemStack == null || !itemStack._valid) {
                writeShort(-1)
                return
            }
            writeShort(connection.registries.itemRegistry.getId(itemStack.item.item))
            writeByte(itemStack.item.count)
            writeShort(itemStack._durability?.durability ?: 0) // ToDo: This is meta data in general and not just durability
            writeNBT(itemStack.getNBT())
            return
        }
        val valid = itemStack?._valid == true
        writeBoolean(valid)
        if (!valid) {
            return
        }
        itemStack!!
        writeVarInt(connection.registries.itemRegistry.getId(itemStack.item.item))
        writeByte(itemStack.item.count)
        writeNBT(itemStack.getNBT())
    }

    fun writeEntityId(entityId: Int) {
        if (versionId < ProtocolVersions.V_14W04A) {
            writeInt(entityId)
        } else {
            writeVarInt(entityId)
        }
    }

    fun writeNBT(nbt: Any?) {
        return writeNBT(nbt, versionId < ProtocolVersions.V_14W28B)
    }

    fun writePublicKey(key: PlayerPublicKey) {
        if (versionId <= ProtocolVersions.V_22W18A) { // ToDo: find version
            writeNBT(key.toNbt())
        } else {
            writeInstant(key.expiresAt)
            writeByteArray(key.publicKey.encoded)
            writeByteArray(key.signature)
        }
    }

    fun writeSignatureData(signature: ByteArray) {
        if (versionId < ProtocolVersions.V_22W42A) {
            writeByteArray(signature)
            return
        }
        check(signature.size == ChatSignatureProperties.SIGNATURE_SIZE) { "Signature size mismatch!" }
        writeBareByteArray(signature)
    }

    fun writeInstant(instant: Instant) {
        if (versionId >= ProtocolVersions.V_22W19A) {
            writeLong(instant.toEpochMilli())
        } else {
            writeLong(instant.epochSecond)
        }
    }

    fun writeLastSeenMessage(lastSeenMessage: LastSeenMessage) {
        writeUUID(lastSeenMessage.profile)
        writeByteArray(lastSeenMessage.signature)
    }

    fun writeLastSeenMessageList(list: LastSeenMessageList) {
        writeArray(list.messages) { writeLastSeenMessage(it) }
    }

    fun writeAcknowledgement(acknowledgement: Acknowledgement) {
        if (versionId < ProtocolVersions.V_22W42A) {
            writeLastSeenMessageList(acknowledgement.lastSeen)
            writeOptional(acknowledgement.lastReceived) { writeLastSeenMessage(it) }
            return
        }
        writeVarInt(acknowledgement.offset)
        writeBitSet(acknowledgement.acknowledged, 20)
    }

    fun writeBitSet(bitSet: BitSet, size: Int) {
        if (bitSet.length() > size) {
            throw IllegalArgumentException("Bit set is larger than size")
        }
        val array = bitSet.toByteArray()
        val bytes = -Math.floorDiv(-size, Byte.SIZE_BITS)
        for (index in 0 until bytes) {
            if (index >= array.size) {
                writeByte(0)
                continue
            }
            writeByte(array[index])
        }
    }
}
