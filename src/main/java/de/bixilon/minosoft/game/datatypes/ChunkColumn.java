package de.bixilon.minosoft.game.datatypes;

import java.util.HashMap;

/**
 * Collection of 16 chunks
 */
public class ChunkColumn {
    private final HashMap<Byte, Chunk> chunks;

    public ChunkColumn(int x, int z) {
        chunks = new HashMap<>();
    }

    public WorldBlock getWorldBlock(byte x, short y, byte z) {
        if (x > 16 || y > 255 || z > 16 || x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException(String.format("Invalid chunk location %s %s %s", x, y, z));
        }
        byte heightNumber = (byte) (y / 16);
        return chunks.get(heightNumber).getWorldBlock(x, (byte) (y - (heightNumber * 16)), z);
    }

}
