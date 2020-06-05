package de.bixilon.minosoft.game.datatypes;

import de.bixilon.minosoft.game.datatypes.blocks.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of ChunkColumns
 */
public class World {
    public final HashMap<ChunkLocation, Chunk> chunks;
    final String name;
    boolean hardcore;

    public World(String name) {
        this.name = name;
        chunks = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public Chunk getChunk(ChunkLocation loc) {
        return chunks.get(loc);
    }

    public Block getBlock(BlockPosition pos) {
        //ToDo
        ChunkLocation loc = pos.getChunkLocation();
        if (getChunk(loc) != null) {
            return getChunk(loc).getBlock(pos.getX() % 16, pos.getX(), pos.getZ() % 16);
        }
        return Block.AIR;
    }

    public void setBlock(BlockPosition pos, Block block) {
        if (getChunk(pos.getChunkLocation()) != null) {
            getChunk(pos.getChunkLocation()).setBlock(pos.getX() % 16, pos.getX(), pos.getZ() % 16, block);
        } else {
            //throw new IllegalAccessException("Chunk is not loaded!");
            // ToDo
        }
    }

    public void unloadChunk(ChunkLocation location) {
        chunks.remove(location);
    }

    public void setChunk(ChunkLocation location, Chunk chunk) {
        chunks.replace(location, chunk);
    }

    public void setChunks(HashMap<ChunkLocation, Chunk> chunkMap) {
        for (Map.Entry<ChunkLocation, Chunk> set : chunkMap.entrySet()) {
            chunks.replace(set.getKey(), set.getValue());
        }
    }

    public boolean isHardcore() {
        return hardcore;
    }

    public void setHardcore(boolean hardcore) {
        this.hardcore = hardcore;
    }
}
