package de.bixilon.minosoft.game.datatypes;

public enum Difficulty {
    PEACEFUL(0),
    EASY(1),
    NORMAL(2),
    HARD(3);

    final int id;

    Difficulty(int id) {
        this.id = id;
    }

    public static Difficulty byId(int id) {
        for (Difficulty g : values()) {
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
