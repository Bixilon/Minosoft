package de.bixilon.minosoft.game.datatypes;

import java.util.HashMap;

/**
 * Collection of 16x16x16 blocks
 */
public class Chunk {
    private final HashMap<ChunkLocation, WorldBlock> blocks;

    public Chunk() {
        blocks = new HashMap<>();
    }

    public WorldBlock getWorldBlock(ChunkLocation loc) {
        //ToDo will return air if null
        return blocks.get(loc);
    }

    public WorldBlock getWorldBlock(byte x, byte y, byte z) {
        return getWorldBlock(new ChunkLocation(x, y, z));
    }
}
