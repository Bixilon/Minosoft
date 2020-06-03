package de.bixilon.minosoft.game.datatypes;

public class Identifier {
    final String legacy;
    String mod = "minecraft"; // by default minecraft
    String water;

    public Identifier(String mod, String legacy, String water) { // water for water update name (post 1.13.x)
        this.mod = mod;
        this.legacy = legacy;
        this.water = water;
    }

    public Identifier(String legacy, String water) {
        this.legacy = legacy;
        this.water = water;
    }

    public Identifier(String name) {
        // legacy and water are the same
        this.legacy = name;
    }

    public String getMod() {
        return mod;
    }

    public String getLegacy() {
        return legacy;
    }

    public String getWaterUpdateName() {
        return ((water == null) ? legacy : water);
    }
}
