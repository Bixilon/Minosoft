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

import de.bixilon.minosoft.data.world.ChunkData

import de.bixilon.minosoft.data.world.biome.source.SpatialBiomeArray
import de.bixilon.minosoft.modding.event.events.BlockEntityMetaDataChangeEvent
import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.packets.ClientboundPacket
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.chunk.ChunkUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.nbt.tag.CompoundTag
import glm_.vec2.Vec2i
import glm_.vec3.Vec3i
import java.util.*

class PacketChunkData() : ClientboundPacket() {
    private val blockEntities = HashMap<Vec3i, BlockEntityMetaData>()
    lateinit var chunkPosition: Vec2i
    var chunkData: ChunkData? = ChunkData()
        private set
    var heightMap: CompoundTag? = null
    private var isFullChunk = false

    constructor(buffer: InByteBuffer) : this() {
        val dimension = buffer.connection.player.world.dimension!!
        chunkPosition = Vec2i(buffer.readInt(), buffer.readInt())
        if (buffer.versionId < ProtocolVersions.V_20W45A) {
            isFullChunk = !buffer.readBoolean()
        }
        if (buffer.versionId < ProtocolVersions.V_14W26A) {
            val sectionBitMask = BitSet.valueOf(buffer.readBytes(2))
            val addBitMask = BitSet.valueOf(buffer.readBytes(2))

            // decompress chunk data
            val decompressed: InByteBuffer = if (buffer.versionId < ProtocolVersions.V_14W28A) {
                Util.decompress(buffer.readBytes(buffer.readInt()), buffer.connection)
            } else {
                buffer
            }
            ChunkUtil.readChunkPacket(decompressed, dimension, sectionBitMask, addBitMask, !isFullChunk, dimension.hasSkyLight)?.let {
                chunkData!!.replace(it)
            } ?: let {
                // unload chunk
                chunkData = null
            }
            return
        }
        val sectionBitMask: BitSet = when {
            buffer.versionId < ProtocolVersions.V_15W34C -> {
                BitSet.valueOf(buffer.readBytes(2))
            }
            buffer.versionId < ProtocolVersions.V_15W36D -> {
                BitSet.valueOf(buffer.readBytes(4))
            }
            buffer.versionId < ProtocolVersions.V_21W03A -> {
                BitSet.valueOf(longArrayOf(buffer.readVarInt().toLong()))
            }
            else -> {
                BitSet.valueOf(buffer.readLongArray())
            }
        }
        if (buffer.versionId >= ProtocolVersions.V_1_16_PRE7 && buffer.versionId < ProtocolVersions.V_1_16_2_PRE2) {
            isFullChunk = buffer.readBoolean()
        }
        if (buffer.versionId >= ProtocolVersions.V_18W44A) {
            heightMap = buffer.readNBT() as CompoundTag
        }
        if (!isFullChunk) {
            chunkData!!.biomeSource = SpatialBiomeArray(buffer.readBiomeArray())
        }
        val size = buffer.readVarInt()
        val lastPos = buffer.position
        if (size > 0) {
            ChunkUtil.readChunkPacket(buffer, dimension, sectionBitMask, null, !isFullChunk, dimension.hasSkyLight)?.let {
                chunkData!!.replace(it)
            } ?: let {
                chunkData = null
            }
            // set position of the byte buffer, because of some reasons HyPixel makes some weird stuff and sends way to much 0 bytes. (~ 190k), thanks @pokechu22
            buffer.position = size + lastPos
        }
        if (buffer.versionId >= ProtocolVersions.V_1_9_4) {
            val blockEntitiesCount = buffer.readVarInt()
            for (i in 0 until blockEntitiesCount) {
                val tag = buffer.readNBT() as CompoundTag
                val data = BlockEntityMetaData.getData(buffer.connection, null, tag) ?: continue
                blockEntities[Vec3i(tag.getNumberTag("x").asInt, tag.getNumberTag("y").asInt, tag.getNumberTag("z").asInt)] = data
            }
        }
        return
    }

    override fun handle(connection: Connection) {
        for ((position, blockEntityMetaData) in blockEntities) {
            connection.fireEvent(BlockEntityMetaDataChangeEvent(connection, position, null, blockEntityMetaData))
        }


        chunkData?.blocks?.let {
            VersionTweaker.transformSections(it, connection.version.versionId)
        }
        chunkData?.let {
            connection.fireEvent(ChunkDataChangeEvent(connection, this))
            val chunk = connection.player.world.getOrCreateChunk(chunkPosition)
            chunk.setData(chunkData!!)
            connection.player.world.setBlockEntityData(blockEntities)
            connection.renderer.renderWindow.worldRenderer.prepareChunk(chunkPosition, chunk)
        } ?: let {
            connection.player.world.unloadChunk(chunkPosition)
            connection.renderer.renderWindow.worldRenderer.unloadChunk(chunkPosition)
        }
    }

    override fun log() {
        Log.protocol(String.format("[IN] Chunk packet received (position=%s)", chunkPosition))
    }
}
