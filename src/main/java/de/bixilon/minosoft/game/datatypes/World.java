package de.bixilon.minosoft.game.datatypes;

import de.bixilon.minosoft.game.datatypes.blocks.Block;

import java.util.HashMap;

/**
 * Collection of ChunkColumns
 */
public class World {
    public final HashMap<ChunkLocation, Chunk> chunks;
    final String name;

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
}
