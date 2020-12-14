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

package de.bixilon.minosoft.data.world;

import de.bixilon.minosoft.data.entities.block.BlockEntityMetaData;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.data.mappings.blocks.Block;

import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * Collection of chunks
 */
public class World {
    final HashMap<ChunkLocation, Chunk> chunks = new HashMap<>();
    final HashMap<Integer, Entity> entities = new HashMap<>();
    boolean hardcore;
    boolean raining;
    Dimension dimension; // used for sky color, etc

    public HashMap<ChunkLocation, Chunk> getAllChunks() {
        return this.chunks;
    }

    @Nullable
    public Block getBlock(BlockPosition pos) {
        ChunkLocation loc = pos.getChunkLocation();
        if (getChunk(loc) != null) {
            return getChunk(loc).getBlock(pos.getInChunkLocation());
        }
        return null;
    }

    public Chunk getChunk(ChunkLocation loc) {
        return this.chunks.get(loc);
    }

    public void setBlock(BlockPosition pos, Block block) {
        if (getChunk(pos.getChunkLocation()) != null) {
            getChunk(pos.getChunkLocation()).setBlock(pos.getInChunkLocation(), block);
        }
        // do nothing if chunk is unloaded
    }

    public void unloadChunk(ChunkLocation location) {
        this.chunks.remove(location);
    }

    public void setChunk(ChunkLocation location, Chunk chunk) {
        this.chunks.put(location, chunk);
    }

    public void setChunks(HashMap<ChunkLocation, Chunk> chunkMap) {
        chunkMap.forEach(this.chunks::put);
    }

    public boolean isHardcore() {
        return this.hardcore;
    }

    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    public boolean isRaining() {
        return this.raining;
    }

    public void setRaining(boolean raining) {
        this.raining = raining;
    }

    public void addEntity(Entity entity) {
        this.entities.put(entity.getEntityId(), entity);
    }

    public Entity getEntity(int id) {
        return this.entities.get(id);
    }

    public void removeEntity(Entity entity) {
        removeEntity(entity.getEntityId());
    }

    public void removeEntity(int entityId) {
        this.entities.remove(entityId);
    }

    public Dimension getDimension() {
        return this.dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public void setBlockEntityData(BlockPosition position, BlockEntityMetaData data) {
        Chunk chunk = this.chunks.get(position.getChunkLocation());
        if (chunk == null) {
            return;
        }
        chunk.setBlockEntityData(position.getInChunkLocation(), data);
    }

    public BlockEntityMetaData getBlockEntityData(BlockPosition position) {
        Chunk chunk = this.chunks.get(position.getChunkLocation());
        if (chunk == null) {
            return null;
        }
        return chunk.getBlockEntityData(position.getInChunkLocation());
    }

    public void setBlockEntityData(HashMap<BlockPosition, BlockEntityMetaData> blockEntities) {
        blockEntities.forEach(this::setBlockEntityData);
    }
}
