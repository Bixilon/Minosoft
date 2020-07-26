/*
 * Codename Minosoft
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

import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Block;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.world.Chunk;
import de.bixilon.minosoft.game.datatypes.world.ChunkNibble;
import de.bixilon.minosoft.game.datatypes.world.ChunkNibbleLocation;
import de.bixilon.minosoft.game.datatypes.world.palette.Palette;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class ChunkUtil {
    public static Chunk readChunkPacket(InByteBuffer buffer, short sectionBitMask, short addBitMask, boolean groundUpContinuous, boolean containsSkyLight) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10: {
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
                HashMap<Byte, ChunkNibble> nibbleMap = new HashMap<>();
                for (byte c = 0; c < 16; c++) { // max sections per chunks in chunk column
                    if (BitByte.isBitSet(sectionBitMask, c)) {

                        HashMap<ChunkNibbleLocation, Block> blockMap = new HashMap<>();

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
                                    Block block = Blocks.getBlockByLegacy(singeBlockId, singleMeta);
                                    if (block.equals(Blocks.nullBlock)) {
                                        arrayPos++;
                                        continue;
                                    }
                                    blockMap.put(new ChunkNibbleLocation(nibbleX, nibbleY, nibbleZ), block);
                                    arrayPos++;
                                }
                            }
                        }
                        nibbleMap.put(c, new ChunkNibble(blockMap));
                    }
                }
                return new Chunk(nibbleMap);
            }
            case VERSION_1_8: {
                if (sectionBitMask == 0x00 && groundUpContinuous) {
                    // unload chunk
                    return null;
                }
                byte sections = BitByte.getBitCount(sectionBitMask);
                int totalBlocks = 4096 * sections; // 16 * 16 * 16 * sections; Section Width * Section Height * Section Width * sections
                int halfBytes = totalBlocks / 2; // half bytes


                short[] blockData = buffer.readShorts(totalBlocks); // blocks >>> 4, data & 0xF

                byte[] light = buffer.readBytes(halfBytes);
                byte[] skyLight = null;
                if (containsSkyLight) {
                    skyLight = buffer.readBytes(halfBytes);
                }

                if (groundUpContinuous) {
                    byte[] biomes = buffer.readBytes(256);
                }

                int arrayPos = 0;
                HashMap<Byte, ChunkNibble> nibbleMap = new HashMap<>();
                for (byte c = 0; c < 16; c++) { // max sections per chunks in chunk column
                    if (!BitByte.isBitSet(sectionBitMask, c)) {
                        continue;
                    }
                    HashMap<ChunkNibbleLocation, Block> blockMap = new HashMap<>();

                    for (int nibbleY = 0; nibbleY < 16; nibbleY++) {
                        for (int nibbleZ = 0; nibbleZ < 16; nibbleZ++) {
                            for (int nibbleX = 0; nibbleX < 16; nibbleX++) {
                                Block block = Blocks.getBlockByLegacy(blockData[arrayPos]);
                                if (block.equals(Blocks.nullBlock)) {
                                    arrayPos++;
                                    continue;
                                }
                                blockMap.put(new ChunkNibbleLocation(nibbleX, nibbleY, nibbleZ), block);
                                arrayPos++;
                            }
                        }
                    }
                    nibbleMap.put(c, new ChunkNibble(blockMap));
                }
                return new Chunk(nibbleMap);
            }
            default: {
                // really big thanks to: https://wiki.vg/index.php?title=Chunk_Format&oldid=13712
                HashMap<Byte, ChunkNibble> nibbleMap = new HashMap<>();
                for (byte c = 0; c < 16; c++) { // max sections per chunks in chunk column
                    if (!BitByte.isBitSet(sectionBitMask, c)) {
                        continue;
                    }
                    if (buffer.getVersion().getVersionNumber() >= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
                        buffer.readShort(); // block count
                    }
                    Palette palette = Palette.choosePalette(buffer.readByte());
                    palette.read(buffer);
                    int individualValueMask = ((1 << palette.getBitsPerBlock()) - 1);

                    long[] data = buffer.readLongs(buffer.readVarInt());

                    HashMap<ChunkNibbleLocation, Block> blockMap = new HashMap<>();
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
                                    if (buffer.getVersion().getVersionNumber() <= ProtocolVersion.VERSION_1_12_2.getVersionNumber()) {
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
                                blockMap.put(new ChunkNibbleLocation(nibbleX, nibbleY, nibbleZ), block);
                            }
                        }
                    }

                    if (buffer.getVersion().getVersionNumber() < ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
                        byte[] light = buffer.readBytes(2048);
                        if (containsSkyLight) {
                            byte[] skyLight = buffer.readBytes(2048);
                        }
                    }

                    nibbleMap.put(c, new ChunkNibble(blockMap));
                }
                if (buffer.getVersion().getVersionNumber() < ProtocolVersion.VERSION_1_15_2.getVersionNumber()) {
                    byte[] biomes = buffer.readBytes(256);
                }
                return new Chunk(nibbleMap);
            }
        }
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
