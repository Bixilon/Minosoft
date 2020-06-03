package de.bixilon.minosoft.game.datatypes;

import de.bixilon.minosoft.game.datatypes.blocks.Block;

import java.util.HashMap;

/**
 * Collection of 16 chunks
 */
public class Chunk {
    private final HashMap<Byte, ChunkNibble> chunks;

    public Chunk(HashMap<Byte, ChunkNibble> chunks) {
        this.chunks = chunks;
    }

    public Block getBlock(int x, int y, int z) {
        if (x > 16 || y > 255 || z > 16 || x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException(String.format("Invalid chunk location %s %s %s", x, y, z));
        }
        byte section = (byte) (y / 16);
        if (section == 0) {
            return Block.COBBLESTONE;
        } else if (section == 1) {
            return Block.DIRT;
        } else {
            return Block.AIR;
        }
        //ToDo
        //return chunks.get(section).getBlock(x, y % 16,z);
    }

}
