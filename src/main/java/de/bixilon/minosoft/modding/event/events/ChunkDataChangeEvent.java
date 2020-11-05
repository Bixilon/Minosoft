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

package de.bixilon.minosoft.modding.event.events;

import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.data.world.ChunkLocation;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketChunkData;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

/**
 * Fired when a new chunk is received or a full chunk changes
 */
public class ChunkDataChangeEvent extends ConnectionEvent {
    private final ChunkLocation location;
    private final Chunk chunk;
    private final CompoundTag heightMap;

    public ChunkDataChangeEvent(Connection connection, ChunkLocation location, Chunk chunk, CompoundTag heightMap) {
        super(connection);
        this.location = location;
        this.chunk = chunk;
        this.heightMap = heightMap;
    }

    public ChunkDataChangeEvent(Connection connection, ChunkLocation location, Chunk chunk) {
        super(connection);
        this.location = location;
        this.chunk = chunk;
        this.heightMap = new CompoundTag();
    }

    public ChunkDataChangeEvent(Connection connection, PacketChunkData pkg) {
        super(connection);
        this.location = pkg.getLocation();
        this.chunk = pkg.getChunk();
        this.heightMap = pkg.getHeightMap();
    }

    public ChunkLocation getLocation() {
        return location;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public CompoundTag getHeightMap() {
        return heightMap;
    }
}
