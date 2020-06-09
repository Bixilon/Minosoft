/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.objects.Player;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.PacketHandshake;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketLoginStart;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketChatMessage;
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusPing;
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusRequest;
import de.bixilon.minosoft.protocol.protocol.ConnectionState;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.ArrayList;

public class Connection {
    private final String host;
    private final int port;
    private final Network network;
    private final PacketHandler handler;
    private final ArrayList<ClientboundPacket> handlingQueue;
    private Player player;
    private ConnectionState state = ConnectionState.DISCONNECTED;
    Thread handleThread;

    private boolean onlyPing;

    public Connection(String host, int port) {
        this.host = host;
        this.port = port;
        network = new Network(this);
        handlingQueue = new ArrayList<>();
        handler = new PacketHandler(this);
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
                // connection established, starting threads and logging in
                network.startPacketThread();
                startHandlingThread();
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
        handleThread.interrupt();
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

    private void startHandlingThread() {
        handleThread = new Thread(() -> {
            while (getConnectionState() != ConnectionState.DISCONNECTED) {
                while (handlingQueue.size() > 0) {
                    try {
                        handlingQueue.get(0).log();
                        handlingQueue.get(0).handle(getHandler());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handlingQueue.remove(0);
                }
                try {
                    // sleep, wait for an interrupt from other thread
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }
            }
        });
        handleThread.start();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void sendChatMessage(String message) {
        sendPacket(new PacketChatMessage(message));
    }
}
