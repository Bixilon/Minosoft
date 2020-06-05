package de.bixilon.minosoft.game.datatypes;

/**
 * Chunk X, Y and Z location (max 16x16x16)
 */
public class ChunkNibbleLocation {
    final int x;
    final int y;
    final int z;

    public ChunkNibbleLocation(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        ChunkNibbleLocation that = (ChunkNibbleLocation) obj;
        return getX() == that.getX() && getY() == that.getY() && getZ() == that.getZ();
    }
}
