/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.chunk

import de.bixilon.minosoft.data.mappings.Dimension
import de.bixilon.minosoft.data.mappings.biomes.Biome
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.world.ChunkData
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.biome.source.XZBiomeArray
import de.bixilon.minosoft.data.world.light.DummyLightAccessor
import de.bixilon.minosoft.data.world.palette.Palette.Companion.choosePalette
import de.bixilon.minosoft.protocol.protocol.InByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*
import java.util.*


object ChunkUtil {

    fun readChunkPacket(buffer: InByteBuffer, dimension: Dimension, sectionBitMask: BitSet, addBitMask: BitSet? = null, isFullChunk: Boolean, containsSkyLight: Boolean): ChunkData? {
        if (buffer.versionId < V_15W35A) { // ToDo: was this really changed in 62?
            return readLegacyChunk(buffer, dimension, sectionBitMask, addBitMask, isFullChunk, containsSkyLight)
        }
        return readPaletteChunk(buffer, dimension, sectionBitMask, isFullChunk, containsSkyLight)
    }

    private fun readLegacyChunkWithAddBitSet(buffer: InByteBuffer, dimension: Dimension, sectionBitMask: BitSet, addBitMask: BitSet, isFullChunk: Boolean, containsSkyLight: Boolean): ChunkData {
        val chunkData = ChunkData()
        chunkData.lightAccessor = DummyLightAccessor // ToDo

        val totalBytes = ProtocolDefinition.BLOCKS_PER_SECTION * sectionBitMask.cardinality()
        val halfBytes = totalBytes / 2


        val blockData = buffer.readBytes(totalBytes)
        val blockMetaData = buffer.readBytes(halfBytes)
        val light = buffer.readBytes(halfBytes)
        var skyLight: ByteArray? = null
        if (containsSkyLight) {
            skyLight = buffer.readBytes(halfBytes)
        }
        val addBlockData = buffer.readBytes(addBitMask.cardinality() * (ProtocolDefinition.BLOCKS_PER_SECTION / 2))
        if (isFullChunk) {
            chunkData.biomeSource = readLegacyBiomeArray(buffer)
        }

        // parse data
        var arrayPosition = 0
        val sectionMap: MutableMap<Int, ChunkSection> = mutableMapOf()
        for (sectionHeight in dimension.lowestSection until dimension.highestSection) {
            if (!sectionBitMask.get(sectionHeight)) {
                continue
            }

            val blocks = arrayOfNulls<BlockState>(ProtocolDefinition.BLOCKS_PER_SECTION)

            for (blockNumber in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
                var blockId = (blockData[arrayPosition].toInt() and 0xFF) shl 4
                var blockMeta = 0
                // get block meta and shift and add (merge) id if needed
                if (arrayPosition % 2 == 0) {
                    // high bits
                    blockMeta = blockMetaData[arrayPosition / 2].toInt() and 0x0F
                    if (addBitMask.get(sectionHeight)) {
                        blockId = (blockId shl 4) or (addBlockData[arrayPosition / 2].toInt() ushr 4)
                    }
                } else {
                    // low 4 bits
                    blockMeta = blockMetaData[arrayPosition / 2].toInt() ushr 4 and 0xF

                    if (addBitMask.get(sectionHeight)) {
                        blockId = blockId shl 4 or (addBlockData[arrayPosition / 2].toInt() and 0xF)
                    }
                }
                arrayPosition++

                blockId = blockId or blockMeta
                if (blockId == ProtocolDefinition.NULL_BLOCK_ID) {
                    continue
                }

                blocks[blockNumber] = buffer.connection.mapping.getBlockState(blockId) ?: continue
            }
            sectionMap[dimension.lowestSection + sectionHeight] = ChunkSection(blocks)
        }
        chunkData.blocks = sectionMap
        return chunkData
    }

    fun readLegacyChunk(buffer: InByteBuffer, dimension: Dimension, sectionBitMask: BitSet, addBitMask: BitSet? = null, isFullChunk: Boolean, containsSkyLight: Boolean = false): ChunkData? {
        if (sectionBitMask.length() == 0 && isFullChunk) {
            // unload chunk
            return null
        }

        if (buffer.versionId < V_14W26A) {
            return readLegacyChunkWithAddBitSet(buffer, dimension, sectionBitMask, addBitMask!!, isFullChunk, containsSkyLight)
        }
        val chunkData = ChunkData()
        chunkData.lightAccessor = DummyLightAccessor

        val totalEntries: Int = ProtocolDefinition.BLOCKS_PER_SECTION * sectionBitMask.cardinality()
        val totalHalfEntries = totalEntries / 2

        val blockData = buffer.readUnsignedShortsLE(totalEntries) // blocks >>> 4, data & 0xF


        val light = buffer.readBytes(totalHalfEntries)
        var skyLight: ByteArray? = null
        if (containsSkyLight) {
            skyLight = buffer.readBytes(totalHalfEntries)
        }
        if (isFullChunk) {
            chunkData.biomeSource = readLegacyBiomeArray(buffer)
        }

        var arrayPos = 0
        val sectionMap: MutableMap<Int, ChunkSection> = mutableMapOf()
        for (sectionHeight in dimension.lowestSection until dimension.highestSection) { // max sections per chunks in chunk column
            if (!sectionBitMask.get(sectionHeight)) {
                continue
            }
            val blocks = arrayOfNulls<BlockState>(ProtocolDefinition.BLOCKS_PER_SECTION)
            for (blockNumber in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
                val blockId = blockData[arrayPos++]
                val block = buffer.connection.mapping.getBlockState(blockId) ?: continue
                blocks[blockNumber] = block
            }
            sectionMap[dimension.lowestSection + sectionHeight] = ChunkSection(blocks)
        }
        chunkData.blocks = sectionMap
        return chunkData
    }

    fun readPaletteChunk(buffer: InByteBuffer, dimension: Dimension, sectionBitMask: BitSet, isFullChunk: Boolean, containsSkyLight: Boolean = false): ChunkData {
        val chunkData = ChunkData()
        val sectionMap: MutableMap<Int, ChunkSection> = mutableMapOf()

        for (sectionHeight in dimension.lowestSection until sectionBitMask.length()) { // max sections per chunks in chunk column
            if (!sectionBitMask[sectionHeight]) {
                continue
            }
            if (buffer.versionId >= V_18W43A) {
                buffer.readShort() // block count
            }

            val palette = choosePalette(buffer.readUnsignedByte().toInt())
            palette.read(buffer)
            val individualValueMask = (1 shl palette.bitsPerBlock) - 1

            val data = buffer.readLongArray()

            val blocks = arrayOfNulls<BlockState>(ProtocolDefinition.BLOCKS_PER_SECTION)
            for (blockNumber in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {

                var blockId: Long = if (buffer.versionId < V_1_16) { // ToDo: When did this changed? is just a guess
                    val startLong = blockNumber * palette.bitsPerBlock / java.lang.Long.SIZE
                    val startOffset = blockNumber * palette.bitsPerBlock % java.lang.Long.SIZE
                    val endLong = ((blockNumber + 1) * palette.bitsPerBlock - 1) / java.lang.Long.SIZE

                    if (startLong == endLong) {
                        data[startLong] ushr startOffset
                    } else {
                        val endOffset = java.lang.Long.SIZE - startOffset
                        data[startLong] ushr startOffset or (data[endLong] shl endOffset)
                    }
                } else {
                    val startLong = blockNumber / (java.lang.Long.SIZE / palette.bitsPerBlock)
                    val startOffset = blockNumber % (java.lang.Long.SIZE / palette.bitsPerBlock) * palette.bitsPerBlock
                    data[startLong] ushr startOffset
                }

                blockId = blockId and individualValueMask.toLong()

                val block = palette.blockById(blockId.toInt()) ?: continue
                blocks[blockNumber] = block
            }

            if (buffer.versionId < V_18W43A) {
                val light = buffer.readBytes(ProtocolDefinition.BLOCKS_PER_SECTION / 2)
                if (containsSkyLight) {
                    val skyLight = buffer.readBytes(ProtocolDefinition.BLOCKS_PER_SECTION / 2)
                }
                // ToDo
            }
            sectionMap[dimension.lowestSection + sectionHeight] = ChunkSection(blocks)
        }

        chunkData.blocks = sectionMap
        if (buffer.versionId < V_19W36A && isFullChunk) {
            chunkData.biomeSource = readLegacyBiomeArray(buffer)
        }

        return chunkData
    }


    private fun readLegacyBiomeArray(buffer: InByteBuffer): XZBiomeArray {
        val biomes: MutableList<Biome> = mutableListOf()
        for (i in 0 until ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z) {
            biomes.add(i, buffer.connection.mapping.biomeRegistry.get(if (buffer.versionId < V_15W35A) {
                buffer.readUnsignedByte().toInt()
            } else {
                buffer.readInt()
            }))
        }
        return XZBiomeArray(biomes.toTypedArray())
    }
}
