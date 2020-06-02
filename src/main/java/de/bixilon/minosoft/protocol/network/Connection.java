package de.bixilon.minosoft.protocol.network;

import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.PacketHandshake;
import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.PacketStatusPing;
import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.PacketStatusRequest;
import de.bixilon.minosoft.protocol.protocol.ConnectionState;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class Connection {
    private final String host;
    private final int port;
    private final Network network;
    private ConnectionState state = ConnectionState.DISCONNECTED;

    private boolean onlyPing;

    public Connection(String host, int port) {
        this.host = host;
        this.port = port;
        network = new Network(this);
    }

    /**
     * Sends an server ping to the server (player count, motd, ...)
     */
    public void ping() {
        onlyPing = true;
        network.connect();

    }

    /**
     * Tries to connect to the server and login
     */
    public void connect() {
        onlyPing = false;
        network.connect();

    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public ConnectionState getConnectionState() {
        return state;
    }

    public void setConnectionState(ConnectionState state) {
        if (this.state == state) {
            return;
        }
        System.out.println("ConnectionStatus changed: " + state.name());
        this.state = state;
        switch (state) {
            case HANDSHAKING:
                // connection established, logging in
                ConnectionState next = (onlyPing ? ConnectionState.STATUS : ConnectionState.LOGIN);
                network.sendPacket(new PacketHandshake(getHost(), getPort(), next, (onlyPing) ? -1 : getVersion().getVersion()));
                // after sending it, switch to next state
                setConnectionState(next);
                break;
            case STATUS:
                // send staus request and ping
                network.sendPacket(new PacketStatusRequest());
                network.sendPacket(new PacketStatusPing(0));
                break;
        }
    }

    public ProtocolVersion getVersion() {
        //ToDo: static right now
        return ProtocolVersion.VERSION_1_7_10;
    }
}
