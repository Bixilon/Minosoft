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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.data.mappings.tweaker.VersionTweaker;
import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.data.world.ChunkLocation;
import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.ChunkUtil;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;

import java.util.HashMap;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W26A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W28A;

public class PacketChunkBulk extends ClientboundPacket {
    private final HashMap<ChunkLocation, Chunk> chunks = new HashMap<>();

    @Override
    public boolean read(InByteBuffer buffer) {
        Dimension dimension = buffer.getConnection().getPlayer().getWorld().getDimension();
        if (buffer.getVersionId() < V_14W26A) {
            int chunkCount = buffer.readUnsignedShort();
            int dataLen = buffer.readInt();
            boolean containsSkyLight = buffer.readBoolean();

            // decompress chunk data
            InByteBuffer decompressed;
            if (buffer.getVersionId() < V_14W28A) {
                decompressed = Util.decompress(buffer.readBytes(dataLen), buffer.getConnection());
            } else {
                decompressed = buffer;
            }

            // chunk meta data
            for (int i = 0; i < chunkCount; i++) {
                int x = buffer.readInt();
                int z = buffer.readInt();
                long[] sectionBitMask = {buffer.readUnsignedShort()};
                int addBitMask = buffer.readUnsignedShort();

                this.chunks.put(new ChunkLocation(x, z), ChunkUtil.readChunkPacket(decompressed, dimension, sectionBitMask, addBitMask, true, containsSkyLight));
            }
            return true;
        }
        boolean containsSkyLight = buffer.readBoolean();
        int chunkCount = buffer.readVarInt();
        int[] x = new int[chunkCount];
        int[] z = new int[chunkCount];
        long[][] sectionBitMask = new long[chunkCount][];

        // ToDo: this was still compressed in 14w28a

        for (int i = 0; i < chunkCount; i++) {
            x[i] = buffer.readInt();
            z[i] = buffer.readInt();
            sectionBitMask[i] = new long[]{buffer.readUnsignedShort()};
        }
        for (int i = 0; i < chunkCount; i++) {
            this.chunks.put(new ChunkLocation(x[i], z[i]), ChunkUtil.readChunkPacket(buffer, dimension, sectionBitMask[i], (short) 0, true, containsSkyLight));
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        this.chunks.values().forEach((chunk) -> VersionTweaker.transformChunk(chunk, connection.getVersion().getVersionId()));

        getChunks().forEach(((location, chunk) -> connection.fireEvent(new ChunkDataChangeEvent(connection, location, chunk))));

        connection.getPlayer().getWorld().setChunks(getChunks());

        getChunks().forEach(((location, chunk) -> connection.getRenderer().getRenderWindow().getChunkRenderer().prepareChunk(location, chunk)));
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Chunk bulk packet received (chunks=%s)", this.chunks.size()));
    }

    public HashMap<ChunkLocation, Chunk> getChunks() {
        return this.chunks;
    }
}
