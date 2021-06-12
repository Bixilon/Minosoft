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

package de.bixilon.minosoft.util.chunk

import de.bixilon.minosoft.data.registries.Dimension
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.world.ChunkData
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.biome.source.XZBiomeArray
import de.bixilon.minosoft.data.world.light.DummyLightAccessor
import de.bixilon.minosoft.data.world.palette.Palette.Companion.choosePalette
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import java.util.*


object ChunkUtil {

    fun readChunkPacket(buffer: PlayInByteBuffer, dimension: Dimension, sectionBitMask: BitSet, addBitMask: BitSet? = null, isFullChunk: Boolean, containsSkyLight: Boolean): ChunkData? {
        if (buffer.versionId < V_15W35A) { // ToDo: was this really changed in 62?
            return readLegacyChunk(buffer, dimension, sectionBitMask, addBitMask, isFullChunk, containsSkyLight)
        }
        return readPaletteChunk(buffer, dimension, sectionBitMask, isFullChunk, containsSkyLight)
    }

    private fun readLegacyChunkWithAddBitSet(buffer: PlayInByteBuffer, dimension: Dimension, sectionBitMask: BitSet, addBitMask: BitSet, isFullChunk: Boolean, containsSkyLight: Boolean): ChunkData {
        val chunkData = ChunkData()
        chunkData.lightAccessor = DummyLightAccessor // ToDo

        val totalBytes = ProtocolDefinition.BLOCKS_PER_SECTION * sectionBitMask.cardinality()
        val halfBytes = totalBytes / 2


        val blockData = buffer.readByteArray(totalBytes)
        val blockMetaData = buffer.readByteArray(halfBytes)
        val light = buffer.readByteArray(halfBytes)
        var skyLight: ByteArray? = null
        if (containsSkyLight) {
            skyLight = buffer.readByteArray(halfBytes)
        }
        val addBlockData = buffer.readByteArray(addBitMask.cardinality() * (ProtocolDefinition.BLOCKS_PER_SECTION / 2))
        if (isFullChunk) {
            chunkData.biomeSource = readLegacyBiomeArray(buffer)
        }

        // parse data
        var arrayPosition = 0
        val sectionMap: MutableMap<Int, ChunkSection> = synchronizedMapOf()
        for ((sectionIndex, sectionHeight) in (dimension.lowestSection until dimension.highestSection).withIndex()) {
            if (!sectionBitMask[sectionIndex]) {
                continue
            }

            val blocks = arrayOfNulls<BlockState>(ProtocolDefinition.BLOCKS_PER_SECTION)

            for (blockNumber in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
                var blockId = (blockData[arrayPosition].toInt() and 0xFF) shl 4
                var blockMeta: Int
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

                blocks[blockNumber] = buffer.connection.registries.getBlockState(blockId) ?: continue
            }
            sectionMap[sectionHeight] = ChunkSection(blocks)
        }
        chunkData.blocks = sectionMap
        return chunkData
    }

    fun readLegacyChunk(buffer: PlayInByteBuffer, dimension: Dimension, sectionBitMask: BitSet, addBitMask: BitSet? = null, isFullChunk: Boolean, containsSkyLight: Boolean = false): ChunkData? {
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


        val light = buffer.readByteArray(totalHalfEntries)
        var skyLight: ByteArray? = null
        if (containsSkyLight) {
            skyLight = buffer.readByteArray(totalHalfEntries)
        }
        if (isFullChunk) {
            chunkData.biomeSource = readLegacyBiomeArray(buffer)
        }

        var arrayPos = 0
        val sectionMap: MutableMap<Int, ChunkSection> = synchronizedMapOf()
        for ((sectionIndex, sectionHeight) in (dimension.lowestSection until dimension.highestSection).withIndex()) { // max sections per chunks in chunk column
            if (!sectionBitMask[sectionIndex]) {
                continue
            }
            val blocks = arrayOfNulls<BlockState>(ProtocolDefinition.BLOCKS_PER_SECTION)
            for (blockNumber in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
                val blockId = blockData[arrayPos++]
                val block = buffer.connection.registries.getBlockState(blockId) ?: continue
                blocks[blockNumber] = block
            }
            sectionMap[sectionHeight] = ChunkSection(blocks)
        }
        chunkData.blocks = sectionMap
        return chunkData
    }

    fun readPaletteChunk(buffer: PlayInByteBuffer, dimension: Dimension, sectionBitMask: BitSet, isFullChunk: Boolean, containsSkyLight: Boolean = false): ChunkData {
        val chunkData = ChunkData()
        val sectionMap: MutableMap<Int, ChunkSection> = synchronizedMapOf()

        for ((sectionIndex, sectionHeight) in (dimension.lowestSection until sectionBitMask.length()).withIndex()) { // max sections per chunks in chunk column
            if (!sectionBitMask[sectionIndex]) {
                continue
            }
            if (buffer.versionId >= V_18W43A) {
                buffer.readShort() // block count
            }

            val palette = choosePalette(buffer.readUnsignedByte(), buffer)

            val individualValueMask = (1 shl palette.bitsPerBlock) - 1

            val data = buffer.readLongArray()

            val blocks = arrayOfNulls<BlockState>(ProtocolDefinition.BLOCKS_PER_SECTION)
            for (blockNumber in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
                var blockId: Long = if (buffer.versionId < V_1_16) { // ToDo: When did this changed? is just a guess
                    val startLong = blockNumber * palette.bitsPerBlock / Long.SIZE_BITS
                    val startOffset = blockNumber * palette.bitsPerBlock % Long.SIZE_BITS
                    val endLong = ((blockNumber + 1) * palette.bitsPerBlock - 1) / Long.SIZE_BITS

                    if (startLong == endLong) {
                        data[startLong] ushr startOffset
                    } else {
                        val endOffset = Long.SIZE_BITS - startOffset
                        data[startLong] ushr startOffset or (data[endLong] shl endOffset)
                    }
                } else {
                    val startLong = blockNumber / (Long.SIZE_BITS / palette.bitsPerBlock)
                    val startOffset = blockNumber % (Long.SIZE_BITS / palette.bitsPerBlock) * palette.bitsPerBlock
                    data[startLong] ushr startOffset
                }

                blockId = blockId and individualValueMask.toLong()

                val block = palette.blockById(blockId.toInt()) ?: continue
                blocks[blockNumber] = block
            }

            if (buffer.versionId < V_18W43A) {
                val light = buffer.readByteArray(ProtocolDefinition.BLOCKS_PER_SECTION / 2)
                if (containsSkyLight) {
                    val skyLight = buffer.readByteArray(ProtocolDefinition.BLOCKS_PER_SECTION / 2)
                }
                // ToDo
                chunkData.lightAccessor = DummyLightAccessor
            }
            sectionMap[sectionHeight] = ChunkSection(blocks)
        }

        chunkData.blocks = sectionMap
        if (buffer.versionId < V_19W36A && isFullChunk) {
            chunkData.biomeSource = readLegacyBiomeArray(buffer)
        }

        return chunkData
    }


    private fun readLegacyBiomeArray(buffer: PlayInByteBuffer): XZBiomeArray {
        val biomes: MutableList<Biome> = mutableListOf()
        for (i in 0 until ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z) {
            biomes.add(i, buffer.connection.registries.biomeRegistry[if (buffer.versionId < V_1_13_2) { // ToDo: Was V_15W35A, but this can't be correct
                buffer.readUnsignedByte()
            } else {
                buffer.readInt()
            }])
        }
        return XZBiomeArray(biomes.toTypedArray())
    }
}
