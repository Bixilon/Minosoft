/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util;

import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.blocks.Blocks;
import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.data.world.ChunkSection;
import de.bixilon.minosoft.data.world.InChunkSectionLocation;
import de.bixilon.minosoft.data.world.palette.Palette;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.HashMap;

public final class ChunkUtil {
    public static Chunk readChunkPacket(InByteBuffer buffer, short sectionBitMask, short addBitMask, boolean groundUpContinuous, boolean containsSkyLight) {
        if (buffer.getVersionId() < 23) {
            if (sectionBitMask == 0x00 && groundUpContinuous) {
                // unload chunk
                return null;
            }
            //chunk
            byte sections = BitByte.getBitCount(sectionBitMask);
            int totalBytes = 4096 * sections; // 16 * 16 * 16 * sections; Section Width * Section Height * Section Width * sections
            int halfBytes = totalBytes / 2; // half bytes

            byte[] blockTypes = buffer.readBytes(totalBytes);
            byte[] meta = buffer.readBytes(halfBytes);
            byte[] light = buffer.readBytes(halfBytes);
            byte[] skyLight = null;
            if (containsSkyLight) {
                skyLight = buffer.readBytes(halfBytes);
            }
            byte[] addBlockTypes = buffer.readBytes(Integer.bitCount(addBitMask) * 2048); // 16 * 16 * 16 * addBlocks / 2
            if (groundUpContinuous) {
                byte[] biomes = buffer.readBytes(256);
            }

            //parse data
            int arrayPos = 0;
            HashMap<Byte, ChunkSection> sectionMap = new HashMap<>();
            for (byte c = 0; c < 16; c++) { // max sections per chunks in chunk column
                if (BitByte.isBitSet(sectionBitMask, c)) {
                    HashMap<InChunkSectionLocation, Block> blockMap = new HashMap<>();

                    for (int nibbleY = 0; nibbleY < 16; nibbleY++) {
                        for (int nibbleZ = 0; nibbleZ < 16; nibbleZ++) {
                            for (int nibbleX = 0; nibbleX < 16; nibbleX++) {
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
                                Block block = buffer.getConnection().getMapping().getBlockByIdAndMetaData(singeBlockId, singleMeta);
                                if (block.equals(Blocks.nullBlock)) {
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
            }
            return new Chunk(sectionMap);
        }
        if (buffer.getVersionId() < 62) { // ToDo: was this really changed in 62?
            if (sectionBitMask == 0x00 && groundUpContinuous) {
                // unload chunk
                return null;
            }
            byte sections = BitByte.getBitCount(sectionBitMask);
            int totalBlocks = 4096 * sections; // 16 * 16 * 16 * sections; Section Width * Section Height * Section Width * sections
            int halfBytes = totalBlocks / 2; // half bytes

            short[] blockData = buffer.readLEShorts(totalBlocks); // blocks >>> 4, data & 0xF

            byte[] light = buffer.readBytes(halfBytes);
            byte[] skyLight = null;
            if (containsSkyLight) {
                skyLight = buffer.readBytes(halfBytes);
            }

            if (groundUpContinuous) {
                byte[] biomes = buffer.readBytes(256);
            }

            int arrayPos = 0;
            HashMap<Byte, ChunkSection> sectionMap = new HashMap<>();
            for (byte c = 0; c < 16; c++) { // max sections per chunks in chunk column
                if (!BitByte.isBitSet(sectionBitMask, c)) {
                    continue;
                }
                HashMap<InChunkSectionLocation, Block> blockMap = new HashMap<>();

                for (int nibbleY = 0; nibbleY < 16; nibbleY++) {
                    for (int nibbleZ = 0; nibbleZ < 16; nibbleZ++) {
                        for (int nibbleX = 0; nibbleX < 16; nibbleX++) {
                            int blockId = blockData[arrayPos] & 0xFFFF;
                            Block block = buffer.getConnection().getMapping().getBlockById(blockId);
                            if (block.equals(Blocks.nullBlock)) {
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
        for (byte c = 0; c < 16; c++) { // max sections per chunks in chunk column
            if (!BitByte.isBitSet(sectionBitMask, c)) {
                continue;
            }
            if (buffer.getVersionId() >= 440) {
                buffer.readShort(); // block count
            }
            Palette palette = Palette.choosePalette(buffer.readByte());
            palette.read(buffer);
            int individualValueMask = ((1 << palette.getBitsPerBlock()) - 1);

            long[] data = buffer.readLongArray(buffer.readVarInt());

            HashMap<InChunkSectionLocation, Block> blockMap = new HashMap<>();
            for (int nibbleY = 0; nibbleY < 16; nibbleY++) {
                for (int nibbleZ = 0; nibbleZ < 16; nibbleZ++) {
                    for (int nibbleX = 0; nibbleX < 16; nibbleX++) {
                        int blockNumber = (((nibbleY * 16) + nibbleZ) * 16) + nibbleX;
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
                            String blockName;
                            if (buffer.getVersionId() <= ProtocolDefinition.FLATTING_VERSION_ID) {
                                blockName = String.format("%d:%d", blockId >> 4, blockId & 0xF);
                            } else {
                                blockName = String.valueOf(blockId);
                            }
                            Log.warn(String.format("Server sent unknown block: %s", blockName));
                            continue;
                        }
                        if (block.equals(Blocks.nullBlock)) {
                            continue;
                        }
                        blockMap.put(new InChunkSectionLocation(nibbleX, nibbleY, nibbleZ), block);
                    }
                }
            }

            if (buffer.getVersionId() < 440) {
                byte[] light = buffer.readBytes(2048);
                if (containsSkyLight) {
                    byte[] skyLight = buffer.readBytes(2048);
                }
            }

            sectionMap.put(c, new ChunkSection(blockMap));
        }
        if (buffer.getVersionId() < 552) {
            byte[] biomes = buffer.readBytes(256);
        }
        return new Chunk(sectionMap);
    }

    public static void readSkyLightPacket(InByteBuffer buffer, int skyLightMask, int blockLightMask, int emptyBlockLightMask, int emptySkyLightMask) {
        for (byte c = 0; c < 18; c++) { // light sections
            if (!BitByte.isBitSet(skyLightMask, c)) {
                continue;
            }
            byte[] skyLight = buffer.readBytes(buffer.readVarInt());
        }
        for (byte c = 0; c < 18; c++) { // light sections
            if (!BitByte.isBitSet(blockLightMask, c)) {
                continue;
            }
            byte[] blockLight = buffer.readBytes(buffer.readVarInt());
        }
        // ToDo
    }
}
