/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.packets.clientbound.play

import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.tweaker.VersionTweaker
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inChunkSectionPosition
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight


import de.bixilon.minosoft.modding.event.events.MultiBlockChangeEvent
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.logging.Log
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import java.util.*

class PacketMultiBlockChange : ClientboundPacket() {
    val blocks = HashMap<Vec3i, BlockState?>()
    lateinit var chunkPosition: Vec2i
        private set

    override fun read(buffer: InByteBuffer): Boolean {
        if (buffer.versionId < ProtocolVersions.V_14W26C) {
            chunkPosition = if (buffer.versionId < ProtocolVersions.V_1_7_5) {
                Vec2i(buffer.readVarInt(), buffer.readVarInt())
            } else {
                buffer.readChunkPosition()
            }
            val count = buffer.readShort()
            val dataSize = buffer.readInt() // should be count * 4
            for (i in 0 until count) {
                val raw = buffer.readInt()
                val meta = (raw and 0xF)
                val blockId = (raw and 0xFFF0 ushr 4)
                val y = (raw and 0xFF0000 ushr 16)
                val z = (raw and 0x0F000000 ushr 24)
                val x = (raw and -0x10000000 ushr 28)
                blocks[Vec3i(x, y, z)] = buffer.connection.mapping.getBlockState((blockId shl 4) or meta)
            }
            return true
        }
        if (buffer.versionId < ProtocolVersions.V_20W28A) {
            chunkPosition = Vec2i(buffer.readInt(), buffer.readInt())
            val count = buffer.readVarInt()
            for (i in 0 until count) {
                val position = buffer.readByte().toInt()
                val y = buffer.readByte()
                val blockId = buffer.readVarInt()
                blocks[Vec3i(position and 0xF0 ushr 4 and 0xF, y.toInt(), position and 0xF)] = buffer.connection.mapping.getBlockState(blockId)
            }
            return true
        }
        val rawPos = buffer.readLong()
        chunkPosition = Vec2i((rawPos shr 42).toInt(), (rawPos shl 22 shr 42).toInt())
        val yOffset = (rawPos.toInt() and 0xFFFFF) * 16
        if (buffer.versionId > ProtocolVersions.V_1_16_2_PRE3) {
            buffer.readBoolean() // ToDo
        }
        val count = buffer.readVarInt()
        for (i in 0 until count) {
            val data = buffer.readVarLong()
            blocks[Vec3i((data shr 8 and 0xF).toInt(), yOffset + (data shr 4 and 0xF).toInt(), (data and 0xF).toInt())] = buffer.connection.mapping.getBlockState((data ushr 12).toInt())
        }
        return true
    }

    override fun handle(connection: Connection) {
        val chunk = connection.player.world.getChunk(chunkPosition) ?: return // thanks mojang
        if (!chunk.isFullyLoaded) {
            return
        }
        connection.fireEvent(MultiBlockChangeEvent(connection, this))
        chunk.setRawBlocks(blocks)

        // tweak
        if (!connection.version.isFlattened()) {
            for ((key, value) in blocks) {
                val block = VersionTweaker.transformBlock(value!!, chunk.sections!!, key.inChunkSectionPosition, key.sectionHeight)
                if (block === value) {
                    continue
                }
                chunk.setBlockState(key, block)
            }
        }
        val sectionHeights = HashSet<Int>()
        for ((key) in blocks) {
            sectionHeights.add(key.sectionHeight)
        }
        for (sectionHeight in sectionHeights) {
            connection.renderer.renderWindow.worldRenderer.prepareChunkSection(chunkPosition, sectionHeight)
        }
    }

    override fun log() {
        Log.protocol(String.format("[IN] Multi block change received at %s (size=%d)", chunkPosition, blocks.size))
    }
}
