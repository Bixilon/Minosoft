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

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.entities.block.BlockEntityMetaData;
import de.bixilon.minosoft.data.entities.entities.Entity;
import de.bixilon.minosoft.data.mappings.Dimension;
import de.bixilon.minosoft.data.mappings.blocks.BlockState;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.UUID;

/**
 * Collection of chunks
 */
public class World {
    private final HashMap<ChunkLocation, Chunk> chunks = new HashMap<>();
    private final HashBiMap<Integer, Entity> entityIdMap = HashBiMap.create();
    private final HashBiMap<UUID, Entity> entityUUIDMap = HashBiMap.create();
    private boolean hardcore;
    private boolean raining;
    private Dimension dimension; // used for sky color, etc

    public HashMap<ChunkLocation, Chunk> getAllChunks() {
        return this.chunks;
    }

    @Nullable
    public BlockInfo getBlockInfo(BlockPosition pos) {
        ChunkLocation loc = pos.getChunkLocation();
        if (getChunk(loc) != null) {
            return getChunk(loc).getBlockInfo(pos.getInChunkLocation());
        }
        return null;
    }

    public Chunk getChunk(ChunkLocation loc) {
        return this.chunks.get(loc);
    }

    public void setBlock(BlockPosition pos, BlockState block) {
        if (getChunk(pos.getChunkLocation()) != null) {
            getChunk(pos.getChunkLocation()).setRawBlock(pos.getInChunkLocation(), block);
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
        this.entityIdMap.put(entity.getEntityId(), entity);
        this.entityUUIDMap.put(entity.getUUID(), entity);
    }

    public Entity getEntity(int id) {
        return this.entityIdMap.get(id);
    }

    public Entity getEntity(UUID uuid) {
        return this.entityUUIDMap.get(uuid);
    }

    public void removeEntity(Entity entity) {
        this.entityIdMap.inverse().remove(entity);
        this.entityUUIDMap.inverse().remove(entity);
    }

    public void removeEntity(int entityId) {
        removeEntity(this.entityIdMap.get(entityId));
    }

    public void removeEntity(UUID entityUUID) {
        removeEntity(this.entityUUIDMap.get(entityUUID));
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
        var section = chunk.getSections().get(position.getSectionHeight());
        if (section == null) {
            return;
        }
        var blockInfo = section.getBlockInfo(position.getInChunkSectionLocation());
        if (blockInfo == null) {
            return;
        }
        blockInfo.setMetaData(data);
    }


    public void setBlockEntityData(HashMap<BlockPosition, BlockEntityMetaData> blockEntities) {
        blockEntities.forEach(this::setBlockEntityData);
    }

    public HashBiMap<Integer, Entity> getEntityIdMap() {
        return this.entityIdMap;
    }

    public HashBiMap<UUID, Entity> getEntityUUIDMap() {
        return this.entityUUIDMap;
    }
}
