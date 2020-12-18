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

package de.bixilon.minosoft.util;

import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.data.world.ChunkSection;
import de.bixilon.minosoft.data.world.InChunkSectionLocation;
import de.bixilon.minosoft.data.world.palette.Palette;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.BitSet;
import java.util.HashMap;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public final class ChunkUtil {
    public static Chunk readChunkPacket(InByteBuffer buffer, int sectionBitMask, int addBitMask, boolean groundUpContinuous, boolean containsSkyLight) {
        if (buffer.getVersionId() < V_14W26A) {
            if (sectionBitMask == 0x00 && groundUpContinuous) {
                // unload chunk
                return null;
            }
            // chunk
            byte sections = BitByte.getBitCount(sectionBitMask);
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
            if (groundUpContinuous) {
                byte[] biomes = buffer.readBytes(256);
            }

            // parse data
            int arrayPos = 0;
            HashMap<Byte, ChunkSection> sectionMap = new HashMap<>();
            for (byte c = 0; c < ProtocolDefinition.SECTIONS_PER_CHUNK; c++) { // max sections per chunks in chunk column
                if (BitByte.isBitSet(sectionBitMask, c)) {
                    HashMap<InChunkSectionLocation, Block> blockMap = new HashMap<>();

                    for (int nibbleY = 0; nibbleY < ProtocolDefinition.SECTION_HEIGHT_Y; nibbleY++) {
                        for (int nibbleZ = 0; nibbleZ < ProtocolDefinition.SECTION_WIDTH_Z; nibbleZ++) {
                            for (int nibbleX = 0; nibbleX < ProtocolDefinition.SECTION_WIDTH_X; nibbleX++) {
                                short singeBlockId = (short) (blockTypes[arrayPos] & 0xFF);
                                byte singleMeta;
                                // get block meta and shift and add (merge) id if needed
                                if (arrayPos % 2 == 0) {
                                    // high bits
                                    singleMeta = (byte) (meta[arrayPos / 2] & 0xF);
                                    if (BitByte.isBitSet(addBitMask, c)) {
                                        singeBlockId = (short) ((singeBlockId << 4) | (addBlockTypes[arrayPos / 2] >>> 4));
                                    }
                                } else {
                                    // low 4 bits
                                    singleMeta = (byte) ((meta[arrayPos / 2] >>> 4) & 0xF);
                                    if (BitByte.isBitSet(addBitMask, c)) {
                                        singeBlockId = (short) ((singeBlockId << 4) | (addBlockTypes[arrayPos / 2] & 0xF));
                                    }
                                }
                                // ToDo light, biome
                                int fullBlockId = (singeBlockId << 4) | singleMeta;
                                if (fullBlockId == ProtocolDefinition.NULL_BLOCK_ID) {
                                    arrayPos++;
                                    continue;
                                }
                                Block block = buffer.getConnection().getMapping().getBlockById(fullBlockId);
                                blockMap.put(new InChunkSectionLocation(nibbleX, nibbleY, nibbleZ), block);
                                arrayPos++;
                            }
                        }
                    }
                    sectionMap.put(c, new ChunkSection(blockMap));
                }
            }
            return new Chunk(sectionMap);
        }
        if (buffer.getVersionId() < V_15W35A) { // ToDo: was this really changed in 62?
            byte sections = BitByte.getBitCount(sectionBitMask);
            int totalBlocks = ProtocolDefinition.BLOCKS_PER_SECTION * sections;
            int halfBytes = totalBlocks >> 1;

            int[] blockData = buffer.readUnsignedLEShorts(totalBlocks); // blocks >>> 4, data & 0xF

            byte[] light = buffer.readBytes(halfBytes);
            byte[] skyLight = null;
            if (containsSkyLight) {
                skyLight = buffer.readBytes(halfBytes);
            }
            if (groundUpContinuous) {
                byte[] biomes = buffer.readBytes(256);
            }
            if (sectionBitMask == 0x00 && groundUpContinuous) {
                // unload chunk
                return null;
            }

            int arrayPos = 0;
            HashMap<Byte, ChunkSection> sectionMap = new HashMap<>();
            for (byte c = 0; c < ProtocolDefinition.SECTIONS_PER_CHUNK; c++) { // max sections per chunks in chunk column
                if (!BitByte.isBitSet(sectionBitMask, c)) {
                    continue;
                }
                HashMap<InChunkSectionLocation, Block> blockMap = new HashMap<>();

                for (int nibbleY = 0; nibbleY < ProtocolDefinition.SECTION_HEIGHT_Y; nibbleY++) {
                    for (int nibbleZ = 0; nibbleZ < ProtocolDefinition.SECTION_WIDTH_Z; nibbleZ++) {
                        for (int nibbleX = 0; nibbleX < ProtocolDefinition.SECTION_WIDTH_X; nibbleX++) {
                            int blockId = blockData[arrayPos];
                            Block block = buffer.getConnection().getMapping().getBlockById(blockId);
                            if (block == null) {
                                arrayPos++;
                                continue;
                            }
                            blockMap.put(new InChunkSectionLocation(nibbleX, nibbleY, nibbleZ), block);
                            arrayPos++;
                        }
                    }
                }
                sectionMap.put(c, new ChunkSection(blockMap));
            }
            return new Chunk(sectionMap);
        }
        // really big thanks to: https://wiki.vg/index.php?title=Chunk_Format&oldid=13712
        HashMap<Byte, ChunkSection> sectionMap = new HashMap<>();
        for (byte c = 0; c < ProtocolDefinition.SECTIONS_PER_CHUNK; c++) { // max sections per chunks in chunk column
            if (!BitByte.isBitSet(sectionBitMask, c)) {
                continue;
            }
            if (buffer.getVersionId() >= V_18W43A) {
                buffer.readShort(); // block count
            }
            Palette palette = Palette.choosePalette(buffer.readByte());
            palette.read(buffer);
            int individualValueMask = ((1 << palette.getBitsPerBlock()) - 1);

            long[] data = buffer.readLongArray(buffer.readVarInt());

            HashMap<InChunkSectionLocation, Block> blockMap = new HashMap<>();
            for (int nibbleY = 0; nibbleY < ProtocolDefinition.SECTION_HEIGHT_Y; nibbleY++) {
                for (int nibbleZ = 0; nibbleZ < ProtocolDefinition.SECTION_WIDTH_Z; nibbleZ++) {
                    for (int nibbleX = 0; nibbleX < ProtocolDefinition.SECTION_WIDTH_X; nibbleX++) {
                        int blockNumber = (((nibbleY * ProtocolDefinition.SECTION_HEIGHT_Y) + nibbleZ) * ProtocolDefinition.SECTIONS_PER_CHUNK) + nibbleX;
                        int startLong = (blockNumber * palette.getBitsPerBlock()) / 64;
                        int startOffset = (blockNumber * palette.getBitsPerBlock()) % 64;
                        int endLong = ((blockNumber + 1) * palette.getBitsPerBlock() - 1) / 64;

                        int blockId;
                        if (startLong == endLong) {
                            blockId = (int) (data[startLong] >>> startOffset);
                        } else {
                            int endOffset = 64 - startOffset;
                            blockId = (int) (data[startLong] >>> startOffset | data[endLong] << endOffset);
                        }
                        blockId &= individualValueMask;

                        Block block = palette.byId(blockId);
                        if (block == null) {
                            continue;
                        }
                        blockMap.put(new InChunkSectionLocation(nibbleX, nibbleY, nibbleZ), block);
                    }
                }
            }

            if (buffer.getVersionId() < V_18W43A) {
                byte[] light = buffer.readBytes(ProtocolDefinition.BLOCKS_PER_SECTION >> 1);
                if (containsSkyLight) {
                    byte[] skyLight = buffer.readBytes(ProtocolDefinition.BLOCKS_PER_SECTION >> 1);
                }
            }

            sectionMap.put(c, new ChunkSection(blockMap));
        }
        if (buffer.getVersionId() < V_19W36A) {
            byte[] biomes = buffer.readBytes(256);
        }
        return new Chunk(sectionMap);
    }

    public static void readSkyLightPacket(InByteBuffer buffer, long[] skyLightMask, long[] blockLightMask, long[] emptyBlockLightMask, long[] emptySkyLightMask) {
        readLightArray(buffer, BitSet.valueOf(skyLightMask));
        readLightArray(buffer, BitSet.valueOf(blockLightMask));
        // ToDo
    }

    private static void readLightArray(InByteBuffer buffer, BitSet lightMask) {
        int highestSectionIndex = ProtocolDefinition.SECTIONS_PER_CHUNK + 2;
        if (buffer.getVersionId() >= V_20W49A) {
            buffer.readVarInt(); // section count
            highestSectionIndex = lightMask.length();
        }
        for (int c = 0; c < highestSectionIndex; c++) { // light sections
            if (!lightMask.get(c)) {
                continue;
            }
            byte[] light = buffer.readBytes(buffer.readVarInt());
        }
    }
}
