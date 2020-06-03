package de.bixilon.minosoft.game.datatypes;

import de.bixilon.minosoft.game.datatypes.blocks.Block;

import java.util.HashMap;

/**
 * Collection of 16x16x16 blocks
 */
public class ChunkNibble {
    private final HashMap<ChunkNibbleLocation, Block> blocks;

    public ChunkNibble(HashMap<ChunkNibbleLocation, Block> blocks) {
        this.blocks = blocks;
    }

    public Block getBlock(ChunkNibbleLocation loc) {
        return blocks.get(loc);
    }

    public Block getBlock(int x, int y, int z) {
        return getBlock(new ChunkNibbleLocation(x, y, z));
    }
}
