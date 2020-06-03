package de.bixilon.minosoft.game.datatypes;

public class BlockPosition {
    int x;
    int y;
    int z;

    public BlockPosition(int x, short y, int z) {
        // y min -2048, max 2047
        //ToDo check values
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
        BlockPosition pos = (BlockPosition) obj;
        return pos.getX() == getX() && pos.getY() == getY() && pos.getZ() == getZ();
    }
}
