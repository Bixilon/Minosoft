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
package de.bixilon.minosoft.protocol.packets.s2c.play.chunk

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.compression.zlib.ZlibUtil.decompress
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.json.JsonUtil.toJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.entities.block.BlockEntity
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.biome.source.SpatialBiomeArray
import de.bixilon.minosoft.data.world.chunk.ChunkData
import de.bixilon.minosoft.datafixer.rls.BlockEntityFixer.fix
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W26A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W28A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W34C
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W36D
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W44A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16_2_PRE2
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16_PRE7
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_9_4
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W45A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W03A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W37A
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.chunk.ChunkUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

@LoadPacket(lowPriority = true)
class ChunkS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val chunkPosition: Vec2i
    val chunkData: ChunkData = ChunkData()
    var unloadChunk: Boolean = false
        private set
    var heightMap: Map<String, Any>? = null
        private set
    private var isFullChunk = false
    private lateinit var readingData: ChunkReadingData

    init {
        val dimension = buffer.connection.world.dimension!!
        chunkPosition = buffer.readChunkPosition()
        if (buffer.versionId < V_20W45A) {
            isFullChunk = !buffer.readBoolean()
        }
        if (buffer.versionId < V_14W26A) { // ToDo
            val sectionBitMask = BitSet.valueOf(buffer.readByteArray(2))
            val addBitMask = BitSet.valueOf(buffer.readByteArray(2))

            // decompress chunk data
            val decompressed: PlayInByteBuffer = if (buffer.versionId < V_14W28A) {
                PlayInByteBuffer(buffer.readByteArray(buffer.readInt()).decompress(), buffer.connection)
            } else {
                buffer
            }
            val chunkData = ChunkUtil.readChunkPacket(decompressed, dimension, sectionBitMask, addBitMask, !isFullChunk, dimension.hasSkyLight)
            if (chunkData == null) {
                unloadChunk = true
            } else {
                this.chunkData.replace(chunkData)
            }
        } else {
            if (buffer.versionId in V_1_16_PRE7 until V_1_16_2_PRE2) {
                buffer.readBoolean() // ToDo: ignore old data???
            }
            val sectionBitMask = when {
                buffer.versionId < V_15W34C -> BitSet.valueOf(buffer.readByteArray(2))
                buffer.versionId < V_15W36D -> BitSet.valueOf(buffer.readByteArray(4))
                buffer.versionId < V_21W03A -> BitSet.valueOf(longArrayOf(buffer.readVarInt().toLong()))
                buffer.versionId < V_21W37A -> BitSet.valueOf(buffer.readLongArray())
                else -> null
            }
            if (buffer.versionId >= V_18W44A) {
                heightMap = buffer.readNBT()?.toJsonObject()
            }
            if (!isFullChunk && buffer.versionId < V_21W37A) {
                this.chunkData.biomeSource = SpatialBiomeArray(buffer.readBiomeArray())
            }
            readingData = ChunkReadingData(PlayInByteBuffer(buffer.readByteArray(), buffer.connection), dimension, sectionBitMask)

            // set position to expected read positions; the server sometimes sends a bunch of useless zeros (~ 190k), thanks @pokechu22

            // block entities
            when {
                buffer.versionId < V_1_9_4 -> {
                }

                buffer.versionId < V_21W37A -> {
                    val blockEntities: MutableMap<Vec3i, BlockEntity> = mutableMapOf()
                    val positionOffset = Vec3i.of(chunkPosition, dimension.minSection, Vec3i.EMPTY)
                    for (i in 0 until buffer.readVarInt()) {
                        val nbt = buffer.readNBT().asJsonObject()
                        val position = Vec3i(nbt["x"]?.toInt() ?: continue, nbt["y"]?.toInt() ?: continue, nbt["z"]?.toInt() ?: continue) - positionOffset
                        val resourceLocation = (nbt["id"]?.toResourceLocation() ?: continue).fix()
                        val type = buffer.connection.registries.blockEntityTypeRegistry[resourceLocation]
                        if (type == null) {
                            Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.WARN) { "Unknown block entity: $resourceLocation" }
                            continue
                        }
                        val entity = type.build(buffer.connection)
                        entity.updateNBT(nbt)
                        blockEntities[position] = entity
                    }
                    this.chunkData.blockEntities = blockEntities
                }

                else -> {
                    val blockEntities: MutableMap<Vec3i, BlockEntity> = mutableMapOf()

                    for (i in 0 until buffer.readVarInt()) {
                        val xz = buffer.readUnsignedByte()
                        val y = buffer.readShort()
                        val typeId = buffer.readVarInt()
                        val nbt = buffer.readNBT()?.asJsonObject()
                        val type = buffer.connection.registries.blockEntityTypeRegistry.getOrNull(typeId) ?: continue
                        val entity = type.build(buffer.connection)
                        if (nbt != null) {
                            entity.updateNBT(nbt)
                        }
                        blockEntities[Vec3i(xz shr 4, y, xz and 0x0F)] = entity
                    }
                    this.chunkData.blockEntities = blockEntities
                }
            }

            if (buffer.versionId >= V_21W37A) {
                if (StaticConfiguration.IGNORE_SERVER_LIGHT) {
                    buffer.pointer = buffer.size
                } else {
                    this.chunkData.replace(ChunkLightS2CP(buffer, chunkPosition).chunkData)
                }
            }
        }
    }

    private fun ChunkReadingData.readChunkData() {
        if (readingData.buffer.versionId < V_21W37A) {
            val chunkData = ChunkUtil.readChunkPacket(buffer, dimension, sectionBitMask!!, null, !isFullChunk, dimension.hasSkyLight)
            if (chunkData == null) {
                unloadChunk = true
            } else {
                this@ChunkS2CP.chunkData.replace(chunkData)
            }
        } else {
            this@ChunkS2CP.chunkData.replace(ChunkUtil.readPaletteChunk(buffer, dimension, null, isFullChunk = true, containsSkyLight = false))
        }
    }

    override fun handle(connection: PlayConnection) {
        if (unloadChunk) {
            connection.world.unloadChunk(chunkPosition)
            return
        }
        readingData.readChunkData()
        val chunk = connection.world.getOrCreateChunk(chunkPosition)
        chunk.setData(chunkData)
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            return
        }
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Chunk (chunkPosition=$chunkPosition)" }
    }

    private data class ChunkReadingData(
        val buffer: PlayInByteBuffer,
        val dimension: DimensionProperties,
        val sectionBitMask: BitSet?,
    )
}
