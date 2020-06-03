package de.bixilon.minosoft.game.datatypes;

/**
 * Chunk X, Y and Z location (max 16x16x16)
 */
public class ChunkLocation {
    byte x;
    byte y;
    byte z;

    public ChunkLocation(byte x, byte y, byte z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public byte getX() {
        return x;
    }

    public byte getY() {
        return y;
    }

    public byte getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        ChunkLocation that = (ChunkLocation) obj;
        return getX() == that.getX() && getY() == that.getY() && getZ() == that.getZ();
    }
}
