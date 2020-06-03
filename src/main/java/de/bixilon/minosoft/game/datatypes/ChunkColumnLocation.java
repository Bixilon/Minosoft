package de.bixilon.minosoft.game.datatypes;

/**
 * Chunk X and Z location (block position / 16, rounded down)
 */
public class ChunkColumnLocation {
    int x;
    int z;

    public ChunkColumnLocation(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        ChunkColumnLocation that = (ChunkColumnLocation) obj;
        return getX() == that.getX() && getZ() == that.getZ();
    }
}
