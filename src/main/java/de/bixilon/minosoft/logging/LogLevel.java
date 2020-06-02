package de.bixilon.minosoft.logging;

public enum LogLevel {
    GAME(0),
    FATAL(1),
    INFO(2),
    WARNING(3),
    DEBUG(4),
    VERBOSE(5),
    PROTOCOL(6);

    private int id;

    LogLevel(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
