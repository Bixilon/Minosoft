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


import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.minosoft.data.world.chunk.ChunkSize
import de.bixilon.minosoft.data.world.chunk.update.block.ChunkLocalBlockUpdate
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class BlocksS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val chunkPosition: ChunkPosition
    val update: Array<ChunkLocalBlockUpdate.LocalUpdate?>

    init {
        when {
            buffer.versionId < ProtocolVersions.V_14W26C -> {
                chunkPosition = if (buffer.versionId < ProtocolVersions.V_1_7_5) ChunkPosition(buffer.readVarInt(), buffer.readVarInt()) else buffer.readChunkPosition()
                val size = buffer.readUnsignedShort()
                buffer.readInt() // data size, always 4*size
                update = arrayOfNulls(size)
                for (i in 0 until size) {
                    val combined = buffer.readInt()
                    update[i] = ChunkLocalBlockUpdate.LocalUpdate(
                        InChunkPosition(
                            x = (combined ushr 16 and 0xFF),
                            y = (combined ushr 24 and 0x0F),
                            z = (combined ushr 28 and 0x0F),
                        ),
                        buffer.session.registries.blockState.getOrNull(combined and 0xFFFF),
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
                        InChunkPosition(position ushr 4 and 0x0F, y, position and 0x0F),
                        buffer.session.registries.blockState.getOrNull(blockId),
                    )
                }
            }

            else -> {
                val raw = buffer.readLong()
                chunkPosition = ChunkPosition((raw shr 42).toInt(), (raw shl 22 shr 42).toInt())
                val yOffset = ((raw shl 44 shr 44) * ChunkSize.SECTION_HEIGHT_Y).toInt()
                if (buffer.versionId > ProtocolVersions.V_1_16_2_PRE3 && buffer.versionId < ProtocolVersions.V_23W17A) {
                    buffer.readBoolean() // ignore light updates
                }
                val data = buffer.readVarLongArray()
                update = arrayOfNulls(data.size)
                for ((index, entry) in data.withIndex()) {
                    val position = InChunkPosition(
                        (entry shr 8 and 0x0F).toInt(),
                        (entry and 0x0F).toInt() + yOffset,
                        (entry shr 4 and 0xF).toInt()
                    )
                    val state = buffer.session.registries.blockState.getOrNull((entry ushr 12).toInt())

                    update[index] = ChunkLocalBlockUpdate.LocalUpdate(
                        position,
                        state,
                    )
                }
            }
        }
    }

    override fun handle(session: PlaySession) {
        val chunk = session.world.chunks[chunkPosition] ?: return
        chunk.apply(this.update.cast().toSet())
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_IN, level = LogLevels.VERBOSE) { "Blocks (chunkPosition=${chunkPosition}, size=${update.size})" }
    }
}
