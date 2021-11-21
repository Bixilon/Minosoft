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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.biomes.Biome
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.dimension.DimensionProperties
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkData
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.biome.source.PalettedBiomeArray
import de.bixilon.minosoft.data.world.biome.source.XZBiomeArray
import de.bixilon.minosoft.data.world.container.SectionDataProvider
import de.bixilon.minosoft.data.world.container.palette.PalettedContainer
import de.bixilon.minosoft.data.world.container.palette.PalettedContainerReader
import de.bixilon.minosoft.data.world.container.palette.palettes.BiomePaletteFactory
import de.bixilon.minosoft.data.world.container.palette.palettes.BlockStatePaletteFactory
import de.bixilon.minosoft.data.world.container.palette.palettes.SingularPalette
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.abs
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*
import glm_.vec2.Vec2i
import java.util.*


object ChunkUtil {

    fun readChunkPacket(buffer: PlayInByteBuffer, dimension: DimensionProperties, sectionBitMask: BitSet, addBitMask: BitSet? = null, isFullChunk: Boolean, containsSkyLight: Boolean): ChunkData? {
        if (buffer.versionId < V_15W35A) { // ToDo: was this really changed in 62?
            return readLegacyChunk(buffer, dimension, sectionBitMask, addBitMask, isFullChunk, containsSkyLight)
        }
        return readPaletteChunk(buffer, dimension, sectionBitMask, isFullChunk, containsSkyLight)
    }

    private fun readLegacyChunkWithAddBitSet(buffer: PlayInByteBuffer, dimension: DimensionProperties, sectionBitMask: BitSet, addBitMask: BitSet, isFullChunk: Boolean, containsSkyLight: Boolean): ChunkData {
        val chunkData = ChunkData()
        // ToDo chunkData.lightAccessor = DummyLightAccessor

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
        val sectionBlocks: Array<SectionDataProvider<BlockState?>?> = arrayOfNulls(dimension.sections)
        for ((sectionIndex, sectionHeight) in (dimension.lowestSection until dimension.highestSection).withIndex()) {
            if (!sectionBitMask[sectionIndex]) {
                continue
            }

            val blocks: Array<BlockState?> = arrayOfNulls(ProtocolDefinition.BLOCKS_PER_SECTION)

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

                blocks[blockNumber] = buffer.connection.registries.blockStateRegistry[blockId] ?: continue
            }
            sectionBlocks[sectionHeight] = SectionDataProvider(blocks, true)
        }
        chunkData.blocks = sectionBlocks
        return chunkData
    }

    fun readLegacyChunk(buffer: PlayInByteBuffer, dimension: DimensionProperties, sectionBitMask: BitSet, addBitMask: BitSet? = null, isFullChunk: Boolean, containsSkyLight: Boolean = false): ChunkData? {
        if (sectionBitMask.length() == 0 && isFullChunk) {
            // unload chunk
            return null
        }

        if (buffer.versionId < V_14W26A) {
            return readLegacyChunkWithAddBitSet(buffer, dimension, sectionBitMask, addBitMask!!, isFullChunk, containsSkyLight)
        }
        val chunkData = ChunkData()
        // ToDo: chunkData.lightAccessor = DummyLightAccessor

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
        val sectionBlocks: Array<SectionDataProvider<BlockState?>?> = arrayOfNulls(dimension.sections)
        for ((sectionIndex, sectionHeight) in (dimension.lowestSection until dimension.highestSection).withIndex()) { // max sections per chunks in chunk column
            if (!sectionBitMask[sectionIndex]) {
                continue
            }
            val blocks = arrayOfNulls<BlockState>(ProtocolDefinition.BLOCKS_PER_SECTION)
            for (blockNumber in 0 until ProtocolDefinition.BLOCKS_PER_SECTION) {
                val blockId = blockData[arrayPos++]
                val block = buffer.connection.registries.blockStateRegistry[blockId] ?: continue
                blocks[blockNumber] = block
            }
            sectionBlocks[sectionHeight] = SectionDataProvider(blocks, true)
        }
        chunkData.blocks = sectionBlocks
        return chunkData
    }

    fun readPaletteChunk(buffer: PlayInByteBuffer, dimension: DimensionProperties, sectionBitMask: BitSet?, isFullChunk: Boolean, containsSkyLight: Boolean = false): ChunkData {
        val chunkData = ChunkData()
        val sectionBlocks: Array<SectionDataProvider<BlockState?>?> = arrayOfNulls(dimension.sections)
        val light: Array<ByteArray?> = arrayOfNulls(dimension.sections)
        var lightReceived = 0
        val biomes: Array<Array<Biome>?> = arrayOfNulls(dimension.sections)

        for ((sectionIndex, sectionHeight) in (dimension.lowestSection until (sectionBitMask?.length() ?: dimension.highestSection)).withIndex()) { // max sections per chunks in chunk column
            if (sectionBitMask?.get(sectionIndex) == false) {
                continue
            }
            if (buffer.versionId >= V_18W43A) {
                buffer.readShort() // non-air block count
            }


            val blockContainer: PalettedContainer<BlockState?> = PalettedContainerReader.read(buffer, buffer.connection.registries.blockStateRegistry, paletteFactory = BlockStatePaletteFactory)

            if (blockContainer.palette !is SingularPalette<*> || blockContainer.palette.item != null) {
                sectionBlocks[sectionHeight - dimension.lowestSection] = SectionDataProvider(blockContainer.unpack(), checkSize = true)
            }
            if (buffer.versionId >= V_21W37A) {
                val biomeContainer: PalettedContainer<Biome> = PalettedContainerReader.read(buffer, buffer.connection.registries.biomeRegistry, paletteFactory = BiomePaletteFactory)
                biomes[sectionHeight - dimension.lowestSection] = biomeContainer.unpack()
            }


            if (buffer.versionId < V_18W43A) {
                val blockLight = buffer.readByteArray(ProtocolDefinition.BLOCKS_PER_SECTION / 2)
                var skyLight: ByteArray? = null
                if (containsSkyLight) {
                    skyLight = buffer.readByteArray(ProtocolDefinition.BLOCKS_PER_SECTION / 2)
                }
                light[sectionHeight - dimension.lowestSection] = LightUtil.mergeLight(blockLight, skyLight ?: LightUtil.EMPTY_LIGHT_ARRAY)
                lightReceived++
            }
        }

        chunkData.blocks = sectionBlocks
        if (lightReceived > 0) {
            chunkData.light = light
        }
        if (buffer.versionId >= V_21W37A) {
            chunkData.biomeSource = PalettedBiomeArray(biomes, dimension.lowestSection, BiomePaletteFactory.edgeBits)
        } else if (buffer.versionId < V_19W36A && isFullChunk) {
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

    val Array<Chunk?>.fullyLoaded: Boolean
        get() {
            for (neighbour in this) {
                if (neighbour?.isFullyLoaded != true) {
                    return false
                }
            }
            return true
        }

    val Array<Chunk?>.loaded: Boolean
        get() {
            for (neighbour in this) {
                if (neighbour?.isLoaded != true) {
                    return false
                }
            }
            return true
        }

    val Array<Chunk?>.received: Boolean
        get() {
            for (neighbour in this) {
                if (neighbour?.blocksInitialized != true && neighbour?.lightInitialized != true) {
                    return false
                }
            }
            return true
        }

    val Array<Chunk?>.canBuildBiomeCache: Boolean
        get() {
            for (neighbour in this) {
                if (neighbour?.biomeSource == null || !neighbour.cacheBiomes) {
                    return false
                }
            }
            return true
        }


    fun getChunkNeighbourPositions(chunkPosition: Vec2i): Array<Vec2i> {
        return arrayOf(
            chunkPosition + Vec2i(-1, -1),
            chunkPosition + Vec2i(-1, 0),
            chunkPosition + Vec2i(-1, 1),
            chunkPosition + Vec2i(0, -1),
            chunkPosition + Vec2i(0, 1),
            chunkPosition + Vec2i(1, -1),
            chunkPosition + Vec2i(1, 0),
            chunkPosition + Vec2i(1, 1),
        )
    }

    /**
     * @param neighbourChunks: **Fully loaded** neighbour chunks
     */
    fun getSectionNeighbours(neighbourChunks: Array<Chunk>, chunk: Chunk, sectionHeight: Int): Array<ChunkSection?> {
        return arrayOf(
            chunk[sectionHeight - 1],
            chunk[sectionHeight + 1],
            neighbourChunks[3][sectionHeight],
            neighbourChunks[4][sectionHeight],
            neighbourChunks[1][sectionHeight],
            neighbourChunks[6][sectionHeight],
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

    fun Vec2i.isInRenderDistance(cameraPosition: Vec2i): Boolean {
        val viewDistance = Minosoft.config.config.game.camera.viewDistance
        val delta = (this - cameraPosition).abs

        return delta.x < viewDistance || delta.y < viewDistance
    }
}
