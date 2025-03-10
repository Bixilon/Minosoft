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

package de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.biome.source.PalettedBiomeArray
import de.bixilon.minosoft.data.world.biome.source.XZBiomeArray
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkPrototype
import de.bixilon.minosoft.data.world.chunk.light.types.LightArray
import de.bixilon.minosoft.data.world.container.palette.PalettedContainerReader
import de.bixilon.minosoft.data.world.container.palette.palettes.BiomePaletteFactory
import de.bixilon.minosoft.data.world.container.palette.palettes.BlockStatePaletteFactory
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.light.ChunkLightS2CP.Companion.LIGHT_SIZE
import de.bixilon.minosoft.protocol.packets.s2c.play.block.chunk.light.LightPacketUtil
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W26A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W35A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W43A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W36A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_13_2
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W37A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import java.util.*


object ChunkPacketUtil {

    fun readChunkPacket(buffer: PlayInByteBuffer, dimension: DimensionProperties, sectionBitMask: BitSet, addBitMask: BitSet? = null, complete: Boolean, containsSkyLight: Boolean): ChunkPrototype? {
        if (buffer.versionId < V_15W35A) { // ToDo: was this really changed in 62?
            return readLegacyChunk(buffer, dimension, sectionBitMask, addBitMask, complete, containsSkyLight)
        }
        return readPaletteChunk(buffer, dimension, sectionBitMask, complete, containsSkyLight)
    }

    private fun readLegacyChunkWithAddBitSet(buffer: PlayInByteBuffer, dimension: DimensionProperties, sectionBitMask: BitSet, addBitMask: BitSet, complete: Boolean, containsSkyLight: Boolean): ChunkPrototype {
        val chunkData = ChunkPrototype()

        val totalBytes = ProtocolDefinition.BLOCKS_PER_SECTION * sectionBitMask.cardinality()
        val halfBytes = totalBytes / 2


        val blockData = buffer.readByteArray(totalBytes)
        val blockMeta = buffer.readByteArray(halfBytes)
        val light = buffer.readByteArray(halfBytes)
        var skyLight: ByteArray? = null
        if (containsSkyLight) {
            skyLight = buffer.readByteArray(halfBytes)
        }
        val addData = buffer.readByteArray(addBitMask.cardinality() * (LIGHT_SIZE))
        if (complete) {
            chunkData.biomeSource = readLegacyBiomeArray(buffer)
        }

        // parse data
        var index = 0
        val sections: Array<Array<BlockState?>?> = arrayOfNulls(dimension.sections)
        for ((sectionIndex, sectionHeight) in (dimension.minSection..dimension.maxSection).withIndex()) {
            if (!sectionBitMask[sectionIndex]) {
                continue
            }

            val blocks: Array<BlockState?> = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION)

            for (yzx in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
                var blockId = (blockData[index].toInt() and 0xFF) shl 4
                var meta: Int
                // get block meta and shift and add (merge) id if needed
                if (index % 2 == 0) {
                    // high bits
                    meta = blockMeta[index / 2].toInt() and 0x0F
                    if (addBitMask.get(sectionHeight)) {
                        blockId = (blockId shl 4) or (addData[index / 2].toInt() ushr 4)
                    }
                } else {
                    // low 4 bits
                    meta = blockMeta[index / 2].toInt() ushr 4 and 0xF

                    if (addBitMask.get(sectionHeight)) {
                        blockId = blockId shl 4 or (addData[index / 2].toInt() and 0xF)
                    }
                }
                index++

                blockId = blockId or meta

                blocks[yzx] = buffer.session.registries.blockState.getOrNull(blockId) ?: continue
            }
            sections[sectionHeight] = blocks
        }
        chunkData.blocks = sections
        return chunkData
    }

    fun readLegacyChunk(buffer: PlayInByteBuffer, dimension: DimensionProperties, sectionBitMask: BitSet, addBitMask: BitSet? = null, isFullChunk: Boolean, containsSkyLight: Boolean = false): ChunkPrototype? {
        if (sectionBitMask.length() == 0 && isFullChunk) {
            // unload chunk
            return null
        }

        if (buffer.versionId < V_14W26A) {
            return readLegacyChunkWithAddBitSet(buffer, dimension, sectionBitMask, addBitMask!!, isFullChunk, containsSkyLight)
        }
        val chunkData = ChunkPrototype()

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

        var index = 0
        val sections: Array<Array<BlockState?>?> = arrayOfNulls(dimension.sections)
        for ((sectionIndex, sectionHeight) in (dimension.minSection..dimension.maxSection).withIndex()) { // max sections per chunks in chunk column
            if (!sectionBitMask[sectionIndex]) {
                continue
            }
            val blocks = arrayOfNulls<BlockState>(ProtocolDefinition.BLOCKS_PER_SECTION)
            for (yzx in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
                val blockId = blockData[index++]
                val block = buffer.session.registries.blockState.getOrNull(blockId) ?: continue
                blocks[yzx] = block
            }
            sections[sectionHeight] = blocks
        }
        chunkData.blocks = sections
        return chunkData
    }

    fun readPaletteChunk(buffer: PlayInByteBuffer, dimension: DimensionProperties, sectionBitMask: BitSet?, complete: Boolean, skylight: Boolean = false): ChunkPrototype {
        val chunkData = ChunkPrototype()
        val sectionBlocks: Array<Array<BlockState?>?> = arrayOfNulls(dimension.sections)
        val light: Array<LightArray?> = arrayOfNulls(dimension.sections)
        var lightReceived = 0
        val biomes: Array<Array<Biome?>?> = arrayOfNulls(dimension.sections)

        for (sectionIndex in (0 until (sectionBitMask?.length() ?: dimension.sections))) { // max sections per chunks in chunk column
            if (sectionBitMask?.get(sectionIndex) == false) {
                continue
            }
            val sectionHeight = sectionIndex + dimension.minSection
            if (buffer.versionId >= V_18W43A) {
                buffer.readShort() // non-air block count
            }


            sectionBlocks[sectionHeight - dimension.minSection] = PalettedContainerReader.unpack(buffer, buffer.session.registries.blockState, factory = BlockStatePaletteFactory)


            if (buffer.versionId >= V_21W37A) {
                biomes[sectionHeight - dimension.minSection] = PalettedContainerReader.unpack(buffer, buffer.session.registries.biome.unsafeCast(), factory = BiomePaletteFactory)
            }


            if (buffer.versionId < V_18W43A) {
                if (StaticConfiguration.IGNORE_SERVER_LIGHT) {
                    var size = LIGHT_SIZE
                    if (skylight) {
                        size += LIGHT_SIZE
                    }
                    buffer.pointer += size
                } else {
                    val blockLight = buffer.readByteArray(LIGHT_SIZE)
                    var skyLight: ByteArray? = null
                    if (skylight) {
                        skyLight = buffer.readByteArray(LIGHT_SIZE)
                    }
                    light[sectionHeight - dimension.minSection] = LightPacketUtil.mergeLight(blockLight, skyLight ?: LightPacketUtil.EMPTY_LIGHT_ARRAY)
                    lightReceived++
                }
            }
        }

        chunkData.blocks = sectionBlocks
        if (lightReceived > 0) {
            chunkData.light = light
        }
        if (buffer.versionId >= V_21W37A) {
            chunkData.biomeSource = PalettedBiomeArray(biomes, dimension.minSection, BiomePaletteFactory.edgeBits)
        } else if (buffer.versionId < V_19W36A && complete) {
            chunkData.biomeSource = readLegacyBiomeArray(buffer)
        }

        return chunkData
    }


    private fun readLegacyBiomeArray(buffer: PlayInByteBuffer): XZBiomeArray {
        val biomes: Array<Biome?> = arrayOfNulls(ProtocolDefinition.SECTION_WIDTH_X * ProtocolDefinition.SECTION_WIDTH_Z)
        for (i in biomes.indices) {
            biomes[i] = buffer.session.registries.biome.getOrNull(if (buffer.versionId < V_1_13_2) { // ToDo: Was V_15W35A, but this can't be correct
                buffer.readUnsignedByte()
            } else {
                buffer.readInt()
            })
        }
        return XZBiomeArray(biomes)
    }
}
