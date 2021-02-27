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

package de.bixilon.minosoft.util.chunk;

import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.data.mappings.biomes.Biome;
import de.bixilon.minosoft.data.mappings.blocks.BlockState;
import de.bixilon.minosoft.data.world.BlockInfo;
import de.bixilon.minosoft.data.world.ChunkData;
import de.bixilon.minosoft.data.world.ChunkSection;
import de.bixilon.minosoft.data.world.InChunkSectionPosition;
import de.bixilon.minosoft.data.world.biome.DummyBiomeAccessor;
import de.bixilon.minosoft.data.world.biome.XZBiomeAccessor;
import de.bixilon.minosoft.data.world.light.DummyLightAccessor;
import de.bixilon.minosoft.data.world.palette.Palette;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.BitByte;

import java.util.BitSet;
import java.util.HashMap;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public final class ChunkUtil {
    public static ChunkData readChunkPacket(InByteBuffer buffer, Dimension dimension, long[] sectionBitMasks, int addBitMask, boolean fullChunk, boolean containsSkyLight) {
        if (buffer.getVersionId() < V_14W26A) {
            if (sectionBitMasks[0] == 0x00 && fullChunk) {
                // unload chunk
                return null;
            }
            // chunk
            byte sections = BitByte.getBitCount(sectionBitMasks[0]);
            int totalBytes = ProtocolDefinition.BLOCKS_PER_SECTION * sections;
            int halfBytes = totalBytes >> 1;

            byte[] blockTypes = buffer.readBytes(totalBytes);
            byte[] meta = buffer.readBytes(halfBytes);
            byte[] light = buffer.readBytes(halfBytes);
            byte[] skyLight = null;
            if (containsSkyLight) {
                skyLight = buffer.readBytes(halfBytes);
            }
            byte[] addBlockTypes = buffer.readBytes(Integer.bitCount(addBitMask) * (ProtocolDefinition.BLOCKS_PER_SECTION >> 1));
            if (fullChunk) {
                byte[] biomes = buffer.readBytes(256);
            }

            // parse data
            int arrayPos = 0;
            HashMap<Integer, ChunkSection> sectionMap = new HashMap<>();
            for (int sectionHeight = dimension.getLowestSection(); sectionHeight < dimension.getHighestSection(); sectionHeight++) { // max sections per chunks in chunk column
                if (BitByte.isBitSet(sectionBitMasks[0], sectionHeight)) {
                    HashMap<InChunkSectionPosition, BlockInfo> blockMap = new HashMap<>();

                    for (int nibbleY = 0; nibbleY < ProtocolDefinition.SECTION_HEIGHT_Y; nibbleY++) {
                        for (int nibbleZ = 0; nibbleZ < ProtocolDefinition.SECTION_WIDTH_Z; nibbleZ++) {
                            for (int nibbleX = 0; nibbleX < ProtocolDefinition.SECTION_WIDTH_X; nibbleX++) {
                                short singeBlockId = (short) (blockTypes[arrayPos] & 0xFF);
                                byte singleMeta;
                                // get block meta and shift and add (merge) id if needed
                                if (arrayPos % 2 == 0) {
                                    // high bits
                                    singleMeta = (byte) (meta[arrayPos / 2] & 0xF);
                                    if (BitByte.isBitSet(addBitMask, sectionHeight)) {
                                        singeBlockId = (short) ((singeBlockId << 4) | (addBlockTypes[arrayPos / 2] >>> 4));
                                    }
                                } else {
                                    // low 4 bits
                                    singleMeta = (byte) ((meta[arrayPos / 2] >>> 4) & 0xF);
                                    if (BitByte.isBitSet(addBitMask, sectionHeight)) {
                                        singeBlockId = (short) ((singeBlockId << 4) | (addBlockTypes[arrayPos / 2] & 0xF));
                                    }
                                }
                                // ToDo light, biome
                                int fullBlockId = (singeBlockId << 4) | singleMeta;
                                if (fullBlockId == ProtocolDefinition.NULL_BLOCK_ID) {
                                    arrayPos++;
                                    continue;
                                }
                                BlockState block = buffer.getConnection().getMapping().getBlockState(fullBlockId);
                                blockMap.put(new InChunkSectionPosition(nibbleX, nibbleY, nibbleZ), new BlockInfo(block));
                                arrayPos++;
                            }
                        }
                    }
                    sectionMap.put(dimension.getLowestSection() + sectionHeight, new ChunkSection(blockMap)); // ToDo
                }
            }
            return new ChunkData(sectionMap, new DummyBiomeAccessor(buffer.getConnection().getMapping().getBiomeRegistry().get(0)), DummyLightAccessor.INSTANCE);
        }
        if (buffer.getVersionId() < V_15W35A) { // ToDo: was this really changed in 62?
            byte sections = BitByte.getBitCount(sectionBitMasks[0]);
            int totalBlocks = ProtocolDefinition.BLOCKS_PER_SECTION * sections;
            int halfBytes = totalBlocks >> 1;

            int[] blockData = buffer.readUnsignedLEShorts(totalBlocks); // blocks >>> 4, data & 0xF

            byte[] light = buffer.readBytes(halfBytes);
            byte[] skyLight = null;
            if (containsSkyLight) {
                skyLight = buffer.readBytes(halfBytes);
            }
            if (fullChunk) {
                byte[] biomes = buffer.readBytes(256);
            }
            if (sectionBitMasks[0] == 0x00 && fullChunk) {
                // unload chunk
                return null;
            }

            int arrayPos = 0;
            HashMap<Integer, ChunkSection> sectionMap = new HashMap<>();
            for (int sectionHeight = dimension.getLowestSection(); sectionHeight < dimension.getHighestSection(); sectionHeight++) { // max sections per chunks in chunk column
                if (!BitByte.isBitSet(sectionBitMasks[0], sectionHeight)) {
                    continue;
                }
                HashMap<InChunkSectionPosition, BlockInfo> blockMap = new HashMap<>();

                for (int nibbleY = 0; nibbleY < ProtocolDefinition.SECTION_HEIGHT_Y; nibbleY++) {
                    for (int nibbleZ = 0; nibbleZ < ProtocolDefinition.SECTION_WIDTH_Z; nibbleZ++) {
                        for (int nibbleX = 0; nibbleX < ProtocolDefinition.SECTION_WIDTH_X; nibbleX++) {
                            int blockId = blockData[arrayPos];
                            BlockState block = buffer.getConnection().getMapping().getBlockState(blockId);
                            if (block == null) {
                                arrayPos++;
                                continue;
                            }
                            blockMap.put(new InChunkSectionPosition(nibbleX, nibbleY, nibbleZ), new BlockInfo(block));
                            arrayPos++;
                        }
                    }
                }
                sectionMap.put(dimension.getLowestSection() + sectionHeight, new ChunkSection(blockMap));
            }
            return new ChunkData(sectionMap, new DummyBiomeAccessor(buffer.getConnection().getMapping().getBiomeRegistry().get(0)), DummyLightAccessor.INSTANCE); // ToDo
        }
        // really big thanks to: https://wiki.vg/index.php?title=Chunk_Format&oldid=13712
        HashMap<Integer, ChunkSection> sectionMap = new HashMap<>();
        BitSet sectionBitSet = BitSet.valueOf(sectionBitMasks);
        for (int sectionHeight = dimension.getLowestSection(); sectionHeight < sectionBitSet.length(); sectionHeight++) { // max sections per chunks in chunk column
            if (!sectionBitSet.get(sectionHeight)) {
                continue;
            }
            if (buffer.getVersionId() >= V_18W43A) {
                buffer.readShort(); // block count
            }
            Palette palette = Palette.Companion.choosePalette(buffer.readUnsignedByte());
            palette.read(buffer);
            int individualValueMask = ((1 << palette.getBitsPerBlock()) - 1);

            long[] data = buffer.readLongArray();

            HashMap<InChunkSectionPosition, BlockInfo> blockMap = new HashMap<>();
            for (int nibbleY = 0; nibbleY < ProtocolDefinition.SECTION_HEIGHT_Y; nibbleY++) {
                for (int nibbleZ = 0; nibbleZ < ProtocolDefinition.SECTION_WIDTH_Z; nibbleZ++) {
                    for (int nibbleX = 0; nibbleX < ProtocolDefinition.SECTION_WIDTH_X; nibbleX++) {
                        int blockNumber = (((nibbleY * ProtocolDefinition.SECTION_HEIGHT_Y) + nibbleZ) * ProtocolDefinition.SECTION_WIDTH_X) + nibbleX;
                        int startLong;
                        int startOffset;
                        int endLong;

                        if (buffer.getVersionId() < V_1_16) { // ToDo: When did this changed? is just a guess
                            startLong = (blockNumber * palette.getBitsPerBlock()) / 64;
                            startOffset = (blockNumber * palette.getBitsPerBlock()) % 64;
                            endLong = ((blockNumber + 1) * palette.getBitsPerBlock() - 1) / 64;
                        } else {
                            startLong = endLong = blockNumber / (64 / palette.getBitsPerBlock());
                            startOffset = (blockNumber % (64 / palette.getBitsPerBlock())) * palette.getBitsPerBlock();
                        }

                        int blockId;
                        if (startLong == endLong) {
                            blockId = (int) (data[startLong] >>> startOffset);
                        } else {
                            int endOffset = 64 - startOffset;
                            blockId = (int) (data[startLong] >>> startOffset | data[endLong] << endOffset);
                        }
                        blockId &= individualValueMask;

                        BlockState block = palette.blockById(blockId);
                        if (block == null) {
                            continue;
                        }
                        blockMap.put(new InChunkSectionPosition(nibbleX, nibbleY, nibbleZ), new BlockInfo(block));
                    }
                }
            }

            if (buffer.getVersionId() < V_18W43A) {
                byte[] light = buffer.readBytes(ProtocolDefinition.BLOCKS_PER_SECTION >> 1);
                if (containsSkyLight) {
                    byte[] skyLight = buffer.readBytes(ProtocolDefinition.BLOCKS_PER_SECTION >> 1);
                }
                // ToDo
            }

            sectionMap.put(dimension.getLowestSection() + sectionHeight, new ChunkSection(blockMap));
        }
        ChunkData chunkData = new ChunkData();
        chunkData.setBlocks(sectionMap);
        if (buffer.getVersionId() < V_19W36A && fullChunk) {
            Biome[] biomes = new Biome[256];
            for (int i = 0; i < biomes.length; i++) {
                biomes[i] = buffer.getConnection().getMapping().getBiomeRegistry().get(buffer.readInt());
            }
            chunkData.setBiomeAccessor(new XZBiomeAccessor(biomes));
        }
        return chunkData;
    }

}
