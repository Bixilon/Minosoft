package de.bixilon.minosoft.protocol.protocol;

public enum ConnectionState {
    CONNECTING(4),
    HANDSHAKING(0),
    STATUS(1),
    LOGIN(2),
    PLAY(3),
    DISCONNECTING(5),
    DISCONNECTED(6),
    UNKNOWN(7);

    private final int id;

    ConnectionState(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public static ConnectionState getById(int id) {
        for (ConnectionState state : values()) {
            if (state.getId() == id) {
                return state;
            }
        }
        return ConnectionState.UNKNOWN;
    }
}
