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

import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.data.world.ChunkLocation;
import de.bixilon.minosoft.data.world.light.LightAccessor;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.ChunkUtil;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_1_16_PRE3;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_20W49A;

public class PacketUpdateLight extends ClientboundPacket {
    private ChunkLocation location;
    private LightAccessor lightAccessor;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.location = new ChunkLocation(buffer.readVarInt(), buffer.readVarInt());
        if (buffer.getVersionId() >= V_1_16_PRE3) {
            boolean trustEdges = buffer.readBoolean();
        }

        long[] skyLightMask;
        long[] blockLightMask;
        long[] emptySkyLightMask;
        long[] emptyBlockLightMask;
        if (buffer.getVersionId() < V_20W49A) {
            // was a varInt before 20w45a, should we change this?
            skyLightMask = new long[]{buffer.readVarLong()};
            blockLightMask = new long[]{buffer.readVarLong()};
            emptyBlockLightMask = new long[]{buffer.readVarLong()};
            emptySkyLightMask = new long[]{buffer.readVarLong()};
        } else {
            skyLightMask = buffer.readLongArray();
            blockLightMask = buffer.readLongArray();
            emptySkyLightMask = buffer.readLongArray();
            emptyBlockLightMask = buffer.readLongArray();
        }
        this.lightAccessor = ChunkUtil.readSkyLightPacket(buffer, skyLightMask, blockLightMask, emptyBlockLightMask, emptySkyLightMask);
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received light update (location=%s)", this.location));
    }

    @Override
    public void handle(Connection connection) {
        Chunk chunk = connection.getPlayer().getWorld().getOrCreateChunk(this.location);
        chunk.setLightAccessor(this.lightAccessor);
        connection.getRenderer().getRenderWindow().getWorldRenderer().prepareChunk(this.location, chunk);
    }
}
