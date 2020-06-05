package de.bixilon.minosoft.game.datatypes;

public enum Dimension {
    NETHER(-1),
    OVERWORLD(0),
    END(1);

    final int id;

    Dimension(int id) {
        this.id = id;
    }

    public static Dimension byId(int id) {
        for (Dimension g : values()) {
            if (g.getId() == id) {
                return g;
            }
        }
        return null;
    }

    public int getId() {
        return id;
    }
}
