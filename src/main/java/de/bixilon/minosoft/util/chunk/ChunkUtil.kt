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

package de.bixilon.minosoft.util.chunk

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.biome.source.PalettedBiomeArray
import de.bixilon.minosoft.data.world.biome.source.XZBiomeArray
import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.chunk.chunk.ChunkPrototype
import de.bixilon.minosoft.data.world.chunk.neighbours.ChunkNeighbours
import de.bixilon.minosoft.data.world.container.block.BlockSectionDataProvider
import de.bixilon.minosoft.data.world.container.palette.PalettedContainer
import de.bixilon.minosoft.data.world.container.palette.PalettedContainerReader
import de.bixilon.minosoft.data.world.container.palette.palettes.BiomePaletteFactory
import de.bixilon.minosoft.data.world.container.palette.palettes.BlockStatePaletteFactory
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionHeight
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W26A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_15W35A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W43A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_19W36A
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_13_2
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_21W37A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import java.lang.StrictMath.abs
import java.util.*


object ChunkUtil {

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
        val addData = buffer.readByteArray(addBitMask.cardinality() * (ProtocolDefinition.BLOCKS_PER_SECTION / 2))
        if (complete) {
            chunkData.biomeSource = readLegacyBiomeArray(buffer)
        }

        // parse data
        var index = 0
        val sections: Array<BlockSectionDataProvider?> = arrayOfNulls(dimension.sections)
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

                blocks[yzx] = buffer.connection.registries.blockState.getOrNull(blockId) ?: continue
            }
            sections[sectionHeight] = BlockSectionDataProvider(blocks)
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
        val sections: Array<BlockSectionDataProvider?> = arrayOfNulls(dimension.sections)
        for ((sectionIndex, sectionHeight) in (dimension.minSection..dimension.maxSection).withIndex()) { // max sections per chunks in chunk column
            if (!sectionBitMask[sectionIndex]) {
                continue
            }
            val blocks = arrayOfNulls<BlockState>(ProtocolDefinition.BLOCKS_PER_SECTION)
            for (yzx in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
                val blockId = blockData[index++]
                val block = buffer.connection.registries.blockState.getOrNull(blockId) ?: continue
                blocks[yzx] = block
            }
            sections[sectionHeight] = BlockSectionDataProvider(blocks)
        }
        chunkData.blocks = sections
        return chunkData
    }

    fun readPaletteChunk(buffer: PlayInByteBuffer, dimension: DimensionProperties, sectionBitMask: BitSet?, complete: Boolean, skylight: Boolean = false): ChunkPrototype {
        val chunkData = ChunkPrototype()
        val sectionBlocks: Array<BlockSectionDataProvider?> = arrayOfNulls(dimension.sections)
        val light: Array<ByteArray?> = arrayOfNulls(dimension.sections)
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


            val blockContainer: PalettedContainer<BlockState?> = PalettedContainerReader.read(buffer, buffer.connection.registries.blockState, paletteFactory = BlockStatePaletteFactory)

            if (!blockContainer.isEmpty) {
                val unpacked = BlockSectionDataProvider(blockContainer.unpack())
                if (!unpacked.isEmpty) {
                    sectionBlocks[sectionHeight - dimension.minSection] = unpacked
                }
            }


            if (buffer.versionId >= V_21W37A) {
                val biomeContainer: PalettedContainer<Biome?> = PalettedContainerReader.read(buffer, buffer.connection.registries.biome.unsafeCast(), paletteFactory = BiomePaletteFactory)

                if (!biomeContainer.isEmpty) {
                    biomes[sectionHeight - dimension.minSection] = biomeContainer.unpack()
                }
            }


            if (buffer.versionId < V_18W43A) {
                val blockLight = buffer.readByteArray(ProtocolDefinition.BLOCKS_PER_SECTION / 2)
                var skyLight: ByteArray? = null
                if (skylight) {
                    skyLight = buffer.readByteArray(ProtocolDefinition.BLOCKS_PER_SECTION / 2)
                }
                if (!StaticConfiguration.IGNORE_SERVER_LIGHT) {
                    light[sectionHeight - dimension.minSection] = LightUtil.mergeLight(blockLight, skyLight ?: LightUtil.EMPTY_LIGHT_ARRAY)
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
            biomes[i] = buffer.connection.registries.biome[if (buffer.versionId < V_1_13_2) { // ToDo: Was V_15W35A, but this can't be correct
                buffer.readUnsignedByte()
            } else {
                buffer.readInt()
            }]
        }
        return XZBiomeArray(biomes)
    }

    val Array<Chunk?>.fullyLoaded: Boolean
        get() {
            for (neighbour in this) {
                if (neighbour == null) return false
                if (neighbour.neighbours.complete) {
                    continue
                }
                return false
            }
            return true
        }


    fun getChunkNeighbourPositions(chunkPosition: ChunkPosition): Array<ChunkPosition> {
        return arrayOf(
            chunkPosition + ChunkNeighbours.OFFSETS[0],
            chunkPosition + ChunkNeighbours.OFFSETS[1],
            chunkPosition + ChunkNeighbours.OFFSETS[2],
            chunkPosition + ChunkNeighbours.OFFSETS[3],
            chunkPosition + ChunkNeighbours.OFFSETS[4],
            chunkPosition + ChunkNeighbours.OFFSETS[5],
            chunkPosition + ChunkNeighbours.OFFSETS[6],
            chunkPosition + ChunkNeighbours.OFFSETS[7],
        )
    }

    /**
     * @param neighbourChunks: **Fully loaded** direct neighbour chunks
     */
    fun getDirectNeighbours(neighbourChunks: Array<Chunk>, chunk: Chunk, sectionHeight: SectionHeight): Array<ChunkSection?> {
        return arrayOf(
            chunk[sectionHeight - 1],
            chunk[sectionHeight + 1],
            neighbourChunks[3][sectionHeight],
            neighbourChunks[4][sectionHeight],
            neighbourChunks[1][sectionHeight],
            neighbourChunks[6][sectionHeight],
        )
    }

    /**
     * @param neighbourChunks: **Fully loaded** direct neighbour chunks
     */
    fun getAllNeighbours(neighbourChunks: Array<Chunk>, chunk: Chunk, sectionHeight: SectionHeight): Array<ChunkSection?> {
        return arrayOf(
            neighbourChunks[0][sectionHeight - 1], // 0, (-1 | -1)
            neighbourChunks[1][sectionHeight - 1], // 1, (-1 | +0)
            neighbourChunks[2][sectionHeight - 1], // 2, (-1 | +1)
            neighbourChunks[3][sectionHeight - 1], // 3, (+0 | -1)
            chunk[sectionHeight - 1],              // 4, (+0 | +0)
            neighbourChunks[4][sectionHeight - 1], // 5, (+0 | +1)
            neighbourChunks[5][sectionHeight - 1], // 6, (-1 | -1)
            neighbourChunks[6][sectionHeight - 1], // 7, (-1 | -1)
            neighbourChunks[7][sectionHeight - 1], // 8, (-1 | -1)

            neighbourChunks[0][sectionHeight + 0], // 9, (-1 | -1)
            neighbourChunks[1][sectionHeight + 0], // 10, (-1 | +0)
            neighbourChunks[2][sectionHeight + 0], // 11, (-1 | +1)
            neighbourChunks[3][sectionHeight + 0], // 12, (+0 | -1)
            chunk[sectionHeight + 0],              // 13, (+0 | +0)
            neighbourChunks[4][sectionHeight + 0], // 14, (+0 | +1)
            neighbourChunks[5][sectionHeight + 0], // 15, (+1 | -1)
            neighbourChunks[6][sectionHeight + 0], // 16, (+1 | +0)
            neighbourChunks[7][sectionHeight + 0], // 17, (+1 | +1)

            neighbourChunks[0][sectionHeight + 1], // 18, (-1 | -1)
            neighbourChunks[1][sectionHeight + 1], // 19, (-1 | +0)
            neighbourChunks[2][sectionHeight + 1], // 20, (-1 | +1)
            neighbourChunks[3][sectionHeight + 1], // 21, (+0 | -1)
            chunk[sectionHeight + 1],              // 22, (+0 | +0)
            neighbourChunks[4][sectionHeight + 1], // 23, (+0 | +1)
            neighbourChunks[5][sectionHeight + 1], // 24, (+1 | -1)
            neighbourChunks[6][sectionHeight + 1], // 25, (+1 | +0)
            neighbourChunks[7][sectionHeight + 1], // 26, (+1 | +1)
        )
    }

    fun Array<ChunkSection?>.acquire() {
        for (section in this) {
            section?.acquire()
        }
    }

    fun Array<ChunkSection?>.release() {
        for (section in this) {
            section?.release()
        }
    }

    fun ChunkPosition.isInViewDistance(viewDistance: Int, cameraPosition: ChunkPosition): Boolean {
        return abs(this.x - cameraPosition.x) <= viewDistance && abs(this.y - cameraPosition.y) <= viewDistance
    }
}
