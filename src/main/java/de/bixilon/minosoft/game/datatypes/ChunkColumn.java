package de.bixilon.minosoft.game.datatypes;

import de.bixilon.minosoft.game.datatypes.blocks.Blocks;

import java.util.HashMap;

/**
 * Collection of 16 chunks
 */
public class ChunkColumn {
    private final HashMap<Byte, Chunk> chunks;
    final int x;
    final int z;

    public ChunkColumn(int x, int z) {
        chunks = new HashMap<>();
        this.x = x;
        this.z = z;
    }

    public WorldBlock getWorldBlock(byte x, short y, byte z) {
        if (x > 16 || y > 255 || z > 16 || x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException(String.format("Invalid chunk location %s %s %s", x, y, z));
        }
        byte heightNumber = (byte) (y / 16);
        BlockPosition pos = new BlockPosition(this.x * 16 + x, y, this.z * 16 + z);
        if (heightNumber == 0) {
            return Blocks.getBlockInstance(Blocks.DIRT, pos);
        } else {
            return Blocks.getBlockInstance(Blocks.AIR, pos);
        }
        //ToDo
        //return chunks.get(heightNumber).getWorldBlock(x, (byte) (y - (heightNumber * 16)), z);
    }

}
