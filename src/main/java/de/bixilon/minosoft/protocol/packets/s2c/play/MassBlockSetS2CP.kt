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


import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.modding.event.events.MassBlockSetEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i

class MassBlockSetS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val blocks: MutableMap<Vec3i, BlockState?> = mutableMapOf()
    var chunkPosition: Vec2i
        private set

    init {
        when {
            buffer.versionId < ProtocolVersions.V_14W26C -> {
                chunkPosition = if (buffer.versionId < ProtocolVersions.V_1_7_5) {
                    Vec2i(buffer.readVarInt(), buffer.readVarInt())
                } else {
                    buffer.readChunkPosition()
                }
                val count = buffer.readShort()
                val dataSize = buffer.readInt()
                check(dataSize == count * 4) { "Mass block set needs 4 bytes per block change!" }
                for (i in 0 until count) {
                    val raw = buffer.readInt()
                    val meta = (raw and 0xF)
                    val blockId = (raw and 0xFFF0 ushr 4)
                    val y = (raw and 0xFF0000 ushr 16)
                    val z = (raw and 0x0F000000 ushr 24)
                    val x = (raw and -0x10000000 ushr 28)
                    blocks[Vec3i(x, y, z)] = buffer.connection.registries.blockStateRegistry[(blockId shl 4) or meta]
                }
            }
            buffer.versionId < ProtocolVersions.V_20W28A -> {
                chunkPosition = Vec2i(buffer.readInt(), buffer.readInt())
                val count = buffer.readVarInt()
                for (i in 0 until count) {
                    val position = buffer.readByte().toInt()
                    val y = buffer.readByte()
                    val blockId = buffer.readVarInt()
                    blocks[Vec3i(position and 0xF0 ushr 4 and 0xF, y.toInt(), position and 0xF)] = buffer.connection.registries.blockStateRegistry[blockId]
                }
            }
            else -> {
                val rawPos = buffer.readLong()
                chunkPosition = Vec2i((rawPos shr 42).toInt(), (rawPos shl 22 shr 42).toInt())
                val yOffset = (rawPos.toInt() and 0xFFFFF) * ProtocolDefinition.SECTION_HEIGHT_Y
                if (buffer.versionId > ProtocolVersions.V_1_16_2_PRE3) {
                    buffer.readBoolean() // ToDo
                }
                for (data in buffer.readVarLongArray()) {
                    blocks[Vec3i((data shr 8 and 0x0F).toInt(), yOffset + (data and 0x0F).toInt(), (data shr 4 and 0xF).toInt())] = buffer.connection.registries.blockStateRegistry[(data ushr 12).toInt()]
                }
            }
        }
    }

    override fun handle(connection: PlayConnection) {
        val chunk = connection.world[chunkPosition] ?: return // thanks mojang
        if (chunk.sections == null) {
            return
        }
        chunk.setBlocks(blocks)

        // tweak
        if (!connection.version.isFlattened()) {
            for ((position, blockState) in blocks) {
                chunk[position] = blockState
            }
        }
        connection.fireEvent(MassBlockSetEvent(connection, this))
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Multi block change (chunkPosition=${chunkPosition}, count=${blocks.size})" }
    }
}
