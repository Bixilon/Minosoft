package de.bixilon.minosoft.logging;

public enum LogLevel {
    GAME(0),
    FATAL(1),
    INFO(2),
    WARNING(3),
    DEBUG(4),
    VERBOSE(5),
    PROTOCOL(6);

    private final int id;

    LogLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static LogLevel byId(int id) {
        for (LogLevel g : values()) {
            if (g.getId() == id) {
                return g;
            }
        }
        return null;
    }

    public static LogLevel byName(String name) {
        for (LogLevel g : values()) {
            if (g.name().equals(name)) {
                return g;
            }
        }
        return null;
    }
}
