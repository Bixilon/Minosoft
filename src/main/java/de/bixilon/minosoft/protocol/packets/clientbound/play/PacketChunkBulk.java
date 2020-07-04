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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.world.Chunk;
import de.bixilon.minosoft.game.datatypes.world.ChunkLocation;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.ChunkUtil;
import de.bixilon.minosoft.util.Util;

import java.util.HashMap;

public class PacketChunkBulk implements ClientboundPacket {
    final HashMap<ChunkLocation, Chunk> chunkMap = new HashMap<>();


    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10: {
                short chunkCount = buffer.readShort();
                int dataLen = buffer.readInt();
                boolean containsSkyLight = buffer.readBoolean();

                // decompress chunk data
                InByteBuffer decompressed = Util.decompress(buffer.readBytes(dataLen), buffer.getVersion());

                // chunk meta data
                for (int i = 0; i < chunkCount; i++) {
                    int x = buffer.readInt();
                    int z = buffer.readInt();
                    short sectionBitMask = buffer.readShort();
                    short addBitMask = buffer.readShort();

                    chunkMap.put(new ChunkLocation(x, z), ChunkUtil.readChunkPacket(decompressed, sectionBitMask, addBitMask, true, containsSkyLight));
                }
                return true;
            }
            case VERSION_1_8: {
                boolean containsSkyLight = buffer.readBoolean();
                int chunks = buffer.readVarInt();
                int[] x = new int[chunks];
                int[] z = new int[chunks];
                short[] sectionBitMask = new short[chunks];
                for (int i = 0; i < chunks; i++) {
                    x[i] = buffer.readInt();
                    z[i] = buffer.readInt();
                    sectionBitMask[i] = buffer.readShort();
                }
                for (int i = 0; i < chunks; i++) {
                    chunkMap.put(new ChunkLocation(x[i], z[i]), ChunkUtil.readChunkPacket(buffer, sectionBitMask[i], (short) 0, true, containsSkyLight));
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Chunk bulk packet received (chunks=%s)", chunkMap.size()));
    }

    public HashMap<ChunkLocation, Chunk> getChunkMap() {
        return chunkMap;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
