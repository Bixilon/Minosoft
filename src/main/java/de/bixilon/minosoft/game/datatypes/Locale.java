package de.bixilon.minosoft.game.datatypes;

public enum Locale {
    EN_US("en_US"),
    EN_GB("en_gb"),
    DE_DE("de_DE");

    String name;

    Locale(String name) {
        this.name = name;
    }

    public static Locale byId(String name) {
        for (Locale g : values()) {
            if (g.getName().equals(name)) {
                return g;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
