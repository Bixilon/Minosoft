package de.bixilon.minosoft.game.datatypes.player;

public class Location {
    private final double x;
    private final double y;
    private final double z;

    public Location(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        Location that = (Location) obj;
        return that.getX() == getX() && that.getY() == getY() && that.getZ() == getZ();
    }
}
