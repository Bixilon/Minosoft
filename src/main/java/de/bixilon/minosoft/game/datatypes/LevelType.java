package de.bixilon.minosoft.game.datatypes;

public enum LevelType {
    DEFAULT("default"),
    FLAT("flat"),
    LARGE_BIOMES("largeBiomes"),
    AMPLIFIED("amplified"),
    DEFAULT_1_1("default_1_1"),
    CUSTOMIZED("customized"),
    BUFFET("buffet");

    final String type;

    LevelType(String type) {
        this.type = type;
    }

    public static LevelType byType(String type) {
        for (LevelType g : values()) {
            if (g.getId().equals(type)) {
                return g;
            }
        }
        return null;
    }

    public String getId() {
        return type;
    }
}
