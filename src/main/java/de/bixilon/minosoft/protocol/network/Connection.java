package de.bixilon.minosoft.protocol.network;

import de.bixilon.minosoft.Config;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.objects.Account;
import de.bixilon.minosoft.objects.Player;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.PacketHandshake;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketLoginStart;
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusPing;
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusRequest;
import de.bixilon.minosoft.protocol.protocol.ConnectionState;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.Util;

import java.util.ArrayList;

public class Connection {
    private final String host;
    private final int port;
    private final Network network;
    private final PacketHandler handler;
    private final ArrayList<ClientboundPacket> handlingQueue;
    private Player player = new Player(new Account(Config.username, Config.password));
    private ConnectionState state = ConnectionState.DISCONNECTED;

    private boolean onlyPing;

    public Connection(String host, int port) {
        this.host = host;
        this.port = port;
        network = new Network(this);
        handlingQueue = new ArrayList<>();
        handler = new PacketHandler(this);
        Thread handleThread = new Thread(() -> {
            while (getConnectionState() != ConnectionState.DISCONNECTING) {
                while (handlingQueue.size() > 0) {
                    handlingQueue.get(0).handle(getHandler());
                    handlingQueue.remove(0);
                }
                Util.sleep(1);
            }
        });
        handleThread.start();
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
        Log.verbose("ConnectionStatus changed: " + state.name());
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
                // send status request and ping
                network.sendPacket(new PacketStatusRequest());
                network.sendPacket(new PacketStatusPing(0));
                break;
            case LOGIN:
                network.sendPacket(new PacketLoginStart(player));
                break;
        }
    }

    public ProtocolVersion getVersion() {
        //ToDo: static right now
        return ProtocolVersion.VERSION_1_7_10;
    }

    public PacketHandler getHandler() {
        return this.handler;
    }

    public void handle(ClientboundPacket p) {
        handlingQueue.add(p);
    }

    public boolean isOnlyPing() {
        return onlyPing;
    }

    public void disconnect() {
        setConnectionState(ConnectionState.DISCONNECTING);
    }

    public Player getPlayer() {
        return player;
    }

    public void sendPacket(ServerboundPacket p) {
        network.sendPacket(p);
    }
}
