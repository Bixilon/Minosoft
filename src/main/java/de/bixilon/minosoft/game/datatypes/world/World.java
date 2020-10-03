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

package de.bixilon.minosoft.game.datatypes.world;

import de.bixilon.minosoft.game.datatypes.entities.Entity;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Block;
import de.bixilon.minosoft.game.datatypes.objectLoader.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.objectLoader.dimensions.Dimension;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import java.util.HashMap;

/**
 * Collection of ChunkColumns
 */
public class World {
    final HashMap<ChunkLocation, Chunk> chunks = new HashMap<>();
    final HashMap<Integer, Entity> entities = new HashMap<>();
    final String name;
    final HashMap<BlockPosition, CompoundTag> blockEntityMeta = new HashMap<>();
    boolean hardcore;
    boolean raining;
    Dimension dimension; // used for sky color, etc

    public World(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public HashMap<ChunkLocation, Chunk> getAllChunks() {
        return chunks;
    }

    public Block getBlock(BlockPosition pos) {
        ChunkLocation loc = pos.getChunkLocation();
        if (getChunk(loc) != null) {
            return getChunk(loc).getBlock(pos.getInChunkLocation());
        }
        return Blocks.nullBlock;
    }

    public Chunk getChunk(ChunkLocation loc) {
        return chunks.get(loc);
    }

    public void setBlock(BlockPosition pos, Block block) {
        if (getChunk(pos.getChunkLocation()) != null) {
            getChunk(pos.getChunkLocation()).setBlock(pos.getInChunkLocation(), block);
        }
        // do nothing if chunk is unloaded
    }

    public void unloadChunk(ChunkLocation location) {
        chunks.remove(location);
    }

    public void setChunk(ChunkLocation location, Chunk chunk) {
        chunks.put(location, chunk);
    }

    public void setChunks(HashMap<ChunkLocation, Chunk> chunkMap) {
        chunkMap.forEach(chunks::put);
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }

    public boolean isRaining() {
        return raining;
    }

    public void setRaining(boolean raining) {
        this.raining = raining;
    }

    public void addEntity(Entity entity) {
        this.entities.put(entity.getEntityId(), entity);
    }

    public Entity getEntity(int id) {
        return entities.get(id);
    }

    public void removeEntity(Entity entity) {
        removeEntity(entity.getEntityId());
    }

    public void removeEntity(int entityId) {
        this.entities.remove(entityId);
    }

    public Dimension getDimension() {
        return dimension;
    }

    public void setDimension(Dimension dimension) {
        this.dimension = dimension;
    }

    public void setBlockEntityData(BlockPosition position, CompoundTag nbt) {
        // ToDo check if block is really a block entity (command block, spawner, skull, flower pot)
        blockEntityMeta.put(position, nbt);
    }

    public CompoundTag getBlockEntityData(BlockPosition position) {
        return blockEntityMeta.get(position);
    }

    public void setBlockEntityData(HashMap<BlockPosition, CompoundTag> blockEntities) {
        blockEntities.forEach(blockEntityMeta::put);
    }
}
