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
package de.bixilon.minosoft.protocol.packets.s2c.play.block


import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = false)
class BlocksS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val chunkPosition: ChunkPosition
    val update: Array<ChunkLocalBlockUpdate.LocalUpdate?>

    init {
        when {
            buffer.versionId < ProtocolVersions.V_14W26C -> {
                chunkPosition = if (buffer.versionId < ProtocolVersions.V_1_7_5) Vec2i(buffer.readVarInt(), buffer.readVarInt()) else buffer.readChunkPosition()
                val size = buffer.readUnsignedShort()
                buffer.readInt() // data size, always 4*size
                update = arrayOfNulls(size)
                for (i in 0 until size) {
                    val raw = buffer.readInt()
                    val meta = (raw and 0xF)
                    val blockId = (raw and 0xFFF0 ushr 4)
                    val y = (raw and 0xFF0000 ushr 16)
                    val z = (raw and 0x0F000000 ushr 24)
                    val x = (raw and -0x10000000 ushr 28)
                    update[i] = ChunkLocalBlockUpdate.LocalUpdate(
                        Vec3i(x, y, z),
                        buffer.connection.registries.blockState.getOrNull((blockId shl 4) or meta),
                    )
                }
            }

            buffer.versionId < ProtocolVersions.V_20W28A -> {
                chunkPosition = buffer.readChunkPosition()
                val size = buffer.readVarInt()
                update = arrayOfNulls(size)
                for (i in 0 until size) {
                    val position = buffer.readByte().toInt()
                    val y = buffer.readUnsignedByte()
                    val blockId = buffer.readVarInt()
                    update[i] = ChunkLocalBlockUpdate.LocalUpdate(
                        Vec3i(position and 0xF0 ushr 4 and 0xF, y, position and 0x0F),
                        buffer.connection.registries.blockState.getOrNull(blockId),
                    )
                }
            }

            else -> {
                val raw = buffer.readLong()
                chunkPosition = Vec2i((raw shr 42).toInt(), (raw shl 22 shr 42).toInt())
                val yOffset = (raw shl 44 shr 44) * ProtocolDefinition.SECTION_HEIGHT_Y
                if (buffer.versionId > ProtocolVersions.V_1_16_2_PRE3) {
                    buffer.readBoolean() // ignore light updates
                }
                val data = buffer.readVarLongArray()
                update = arrayOfNulls(data.size)
                for ((index, entry) in data.withIndex()) {
                    val position = Vec3i(
                        (entry shr 8 and 0x0F).toInt(),
                        (entry and 0x0F).toInt() + yOffset,
                        (entry shr 4 and 0xF).toInt()
                    )
                    val state = buffer.connection.registries.blockState.getOrNull((entry ushr 12).toInt())

                    update[index] = ChunkLocalBlockUpdate.LocalUpdate(
                        position,
                        state,
                    )
                }
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        val chunk = connection.world.chunks[chunkPosition] ?: return
        chunk.apply(this.update.cast().toSet())
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Blocks (chunkPosition=${chunkPosition}, size=${update.size})" }
    }
}
