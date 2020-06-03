package de.bixilon.minosoft.objects;

public enum Dimension {
    NETHER(-1),
    OVERWORLD(0),
    END(1);

    int id;

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
