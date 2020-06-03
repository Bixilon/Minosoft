package de.bixilon.minosoft.game.datatypes;

public enum GameMode {
    SURVIVAL(0),
    CREATIVE(1),
    ADVENTURE(3),
    SPECTATOR(4);

    int id;

    GameMode(int id) {
        this.id = id;
    }

    public static GameMode byId(int id) {
        for (GameMode g : values()) {
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
