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

import de.bixilon.minosoft.game.datatypes.Dimension;
import de.bixilon.minosoft.game.datatypes.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.entities.Entity;
import de.bixilon.minosoft.nbt.tag.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of ChunkColumns
 */
public class World {
    final HashMap<ChunkLocation, Chunk> chunks;
    final HashMap<Integer, Entity> entities;
    final String name;
    private final HashMap<BlockPosition, String[]> signs;
    private final HashMap<BlockPosition, CompoundTag> blockEntityMeta;
    boolean hardcore;
    boolean raining;
    Dimension dimension; // used for sky color, etc

    public World(String name) {
        this.name = name;
        chunks = new HashMap<>();
        entities = new HashMap<>();
        signs = new HashMap<>();
        blockEntityMeta = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Chunk getChunk(ChunkLocation loc) {
        return chunks.get(loc);
    }

    public HashMap<ChunkLocation, Chunk> getAllChunks() {
        return chunks;
    }

    public Blocks getBlock(BlockPosition pos) {
        ChunkLocation loc = pos.getChunkLocation();
        if (getChunk(loc) != null) {
            return getChunk(loc).getBlock(pos.getInChunkLocation());
        }
        return Blocks.AIR;
    }

    public void setBlock(BlockPosition pos, Blocks block) {
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
        for (Map.Entry<ChunkLocation, Chunk> set : chunkMap.entrySet()) {
            chunks.put(set.getKey(), set.getValue());
        }
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
        this.entities.put(entity.getId(), entity);
    }

    public Entity getEntity(int id) {
        return entities.get(id);
    }

    public void removeEntity(Entity entity) {
        removeEntity(entity.getId());
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

    public void updateSign(BlockPosition position, String[] lines) {
        //ToDo check if block is really a sign
        signs.put(position, lines);
    }

    public String[] getSignText(BlockPosition position) {
        return signs.get(position);
    }

    public void setBlockEntityData(BlockPosition position, CompoundTag nbt) {
        //ToDo check if block is really a block entity (command block, spawner, skull, flower pot)
        blockEntityMeta.put(position, nbt);
    }

    public CompoundTag getBlockEntityData(BlockPosition position) {
        return blockEntityMeta.get(position);
    }
}
