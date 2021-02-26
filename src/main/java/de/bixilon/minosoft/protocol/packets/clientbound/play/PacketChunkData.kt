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

import de.bixilon.minosoft.data.entities.block.BlockEntityMetaData
import de.bixilon.minosoft.data.mappings.tweaker.VersionTweaker
import de.bixilon.minosoft.data.world.BlockPosition
import de.bixilon.minosoft.data.world.ChunkData
import de.bixilon.minosoft.data.world.ChunkLocation
import de.bixilon.minosoft.data.world.biome.NoiseBiomeAccessor
import de.bixilon.minosoft.modding.event.events.BlockEntityMetaDataChangeEvent
import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.ChunkUtil
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.nbt.tag.CompoundTag
import java.util.*

class PacketChunkData : ClientboundPacket() {
    private val blockEntities = HashMap<BlockPosition, BlockEntityMetaData>()
    lateinit var location: ChunkLocation
    val chunkData: ChunkData = ChunkData()
    var heightMap: CompoundTag? = null
    private var shouldMerge = false

    override fun read(buffer: InByteBuffer): Boolean {
        val dimension = buffer.connection.player.world.dimension
        location = ChunkLocation(buffer.readInt(), buffer.readInt())
        if (buffer.versionId < ProtocolVersions.V_20W45A) {
            shouldMerge = !buffer.readBoolean()
        }
        if (buffer.versionId < ProtocolVersions.V_14W26A) {
            val sectionBitMasks = longArrayOf(buffer.readUnsignedShort().toLong())
            val addBitMask = buffer.readUnsignedShort()

            // decompress chunk data
            val decompressed: InByteBuffer = if (buffer.versionId < ProtocolVersions.V_14W28A) {
                Util.decompress(buffer.readBytes(buffer.readInt()), buffer.connection)
            } else {
                buffer
            }
            chunkData.replace(ChunkUtil.readChunkPacket(decompressed, dimension, sectionBitMasks, addBitMask, !shouldMerge, dimension!!.hasSkyLight))
            return true
        }
        val sectionBitMasks: LongArray = when {
            buffer.versionId < ProtocolVersions.V_15W34C -> {
                longArrayOf(buffer.readUnsignedShort().toLong())
            }
            buffer.versionId < ProtocolVersions.V_15W36D -> {
                longArrayOf(buffer.readInt().toLong())
            }
            buffer.versionId < ProtocolVersions.V_21W03A -> {
                longArrayOf(buffer.readVarInt().toLong())
            }
            else -> {
                buffer.readLongArray()
            }
        }
        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE7 && buffer.versionId < ProtocolVersions.V_1_16_2_PRE2) {
            shouldMerge = buffer.readBoolean()
        }
        if (buffer.versionId >= ProtocolVersions.V_18W44A) {
            heightMap = buffer.readNBT() as CompoundTag
        }
        if (!shouldMerge) {
            chunkData.biomeAccessor = NoiseBiomeAccessor(buffer.readBiomeArray())
        }
        val size = buffer.readVarInt()
        val lastPos = buffer.position
        if (size > 0) {
            chunkData.replace(ChunkUtil.readChunkPacket(buffer, dimension, sectionBitMasks, 0, !shouldMerge, dimension!!.hasSkyLight))
            // set position of the byte buffer, because of some reasons HyPixel makes some weird stuff and sends way to much 0 bytes. (~ 190k), thanks @pokechu22
            buffer.position = size + lastPos
        }
        if (buffer.versionId >= ProtocolVersions.V_1_9_4) {
            val blockEntitiesCount = buffer.readVarInt()
            for (i in 0 until blockEntitiesCount) {
                val tag = buffer.readNBT() as CompoundTag
                val data = BlockEntityMetaData.getData(buffer.connection, null, tag) ?: continue
                blockEntities[BlockPosition(tag.getNumberTag("x").asInt, tag.getNumberTag("y").asInt, tag.getNumberTag("z").asInt)] = data
            }
        }
        return true
    }

    override fun handle(connection: Connection) {
        for ((position, blockEntityMetaData) in blockEntities) {
            connection.fireEvent(BlockEntityMetaDataChangeEvent(connection, position, null, blockEntityMetaData))
        }
        chunkData.blocks?.let {
            VersionTweaker.transformSections(it, connection.version.versionId)
        }
        connection.fireEvent(ChunkDataChangeEvent(connection, this))
        val chunk = connection.player.world.getOrCreateChunk(location)
        chunk.setData(chunkData)
        connection.player.world.setBlockEntityData(blockEntities)
        connection.renderer.renderWindow.worldRenderer.prepareChunk(location, chunk)
    }

    override fun log() {
        Log.protocol(String.format("[IN] Chunk packet received (chunk: %s)", location))
    }
}
