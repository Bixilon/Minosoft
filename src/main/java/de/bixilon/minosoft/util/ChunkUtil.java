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

import de.bixilon.minosoft.game.datatypes.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.world.Chunk;
import de.bixilon.minosoft.game.datatypes.world.ChunkNibble;
import de.bixilon.minosoft.game.datatypes.world.ChunkNibbleLocation;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import java.util.HashMap;

public class ChunkUtil {
    public static Chunk readChunkPacket(InByteBuffer buffer, short sectionBitMask, short addBitMask, boolean groundUpContinuous, boolean containsSkyLight) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10: {
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

                        HashMap<ChunkNibbleLocation, Blocks> blockMap = new HashMap<>();

                        for (int nibbleY = 0; nibbleY < 16; nibbleY++) {
                            for (int nibbleZ = 0; nibbleZ < 16; nibbleZ++) {
                                for (int nibbleX = 0; nibbleX < 16; nibbleX++) {

                                    short singeBlockId = blockTypes[arrayPos];
                                    byte singleMeta;
                                    // get block meta and shift and add (merge) id if needed
                                    if (arrayPos % 2 == 0) {
                                        // high bits
                                        singleMeta = BitByte.getLow4Bits(meta[arrayPos / 2]);
                                        if (BitByte.isBitSet(addBitMask, c)) {
                                            singeBlockId = (short) ((singeBlockId << 4) | BitByte.getHigh4Bits(addBlockTypes[arrayPos / 2]));
                                        }
                                    } else {
                                        // low 4 bits
                                        singleMeta = BitByte.getHigh4Bits(meta[arrayPos / 2]);

                                        if (BitByte.isBitSet(addBitMask, c)) {
                                            singeBlockId = (short) ((singeBlockId << 4) | BitByte.getLow4Bits(addBlockTypes[arrayPos / 2]));
                                        }
                                    }


                                    // ToDo light, biome
                                    Blocks block = Blocks.byId(singeBlockId, singleMeta);
                                    if (block == Blocks.AIR) {
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
                    HashMap<ChunkNibbleLocation, Blocks> blockMap = new HashMap<>();

                    for (int nibbleY = 0; nibbleY < 16; nibbleY++) {
                        for (int nibbleZ = 0; nibbleZ < 16; nibbleZ++) {
                            for (int nibbleX = 0; nibbleX < 16; nibbleX++) {
                                Blocks block = Blocks.byId(blockData[arrayPos] >>> 4, blockData[arrayPos] & 0xF);
                                if (block == Blocks.AIR) {
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
            case VERSION_1_9_4: {
                byte sections = BitByte.getBitCount(sectionBitMask);

                HashMap<Byte, ChunkNibble> nibbleMap = new HashMap<>();
                for (byte c = 0; c < 16; c++) { // max sections per chunks in chunk column
                    if (!BitByte.isBitSet(sectionBitMask, c)) {
                        continue;
                    }

                    byte bitsPerBlock = buffer.readByte();
                    int[] palette = new int[buffer.readVarInt()];
                    for (int i = 0; i < palette.length; i++) {
                        palette[i] = buffer.readVarInt();
                    }
                    long[] data = buffer.readLongs(buffer.readVarInt());

                    HashMap<ChunkNibbleLocation, Blocks> blockMap = new HashMap<>();
                    int blocks = 0;
/*
                    for (int nibbleY = 0; nibbleY < 16; nibbleY++) {
                        for (int nibbleZ = 0; nibbleZ < 16; nibbleZ++) {
                            for (int nibbleX = 0; nibbleX < 16; nibbleX++) {
                                Blocks block = Blocks.byId(blockData[arrayPos] >>> 4, blockData[arrayPos] & 0xF);
                                if (block == Blocks.AIR) {
                                    arrayPos++;
                                    continue;
                                }
                                blockMap.put(new ChunkNibbleLocation(nibbleX, nibbleY, nibbleZ), block);
                                arrayPos++;
                            }
                        }
                    }

 */

                    byte[] light = buffer.readBytes(blocks / 2);
                    if (containsSkyLight) {
                        byte[] skyLight = buffer.readBytes(blocks / 2);
                    }

                    nibbleMap.put(c, new ChunkNibble(blockMap));
                }
                byte[] biomes = buffer.readBytes(256);
                return new Chunk(nibbleMap);
            }
        }
        throw new RuntimeException("Could not parse chunk!");
    }

}
