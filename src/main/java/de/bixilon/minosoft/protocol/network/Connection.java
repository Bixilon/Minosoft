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

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.PingCallback;
import de.bixilon.minosoft.config.GameConfiguration;
import de.bixilon.minosoft.game.datatypes.Player;
import de.bixilon.minosoft.game.datatypes.VelocityHandler;
import de.bixilon.minosoft.game.datatypes.objectLoader.CustomMapping;
import de.bixilon.minosoft.game.datatypes.objectLoader.recipes.Recipes;
import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Version;
import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Versions;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.ping.ServerListPing;
import de.bixilon.minosoft.protocol.modding.channels.DefaultPluginChannels;
import de.bixilon.minosoft.protocol.modding.channels.PluginChannelHandler;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketStopSound;
import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.PacketHandshake;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketLoginStart;
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusPing;
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusRequest;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.util.DNSUtil;
import de.bixilon.minosoft.util.ServerAddress;
import org.xbill.DNS.TextParseException;

import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

public class Connection {
    public static int lastConnectionId;
    final Network network = new Network(this);
    final PacketHandler handler = new PacketHandler(this);
    final PacketSender sender = new PacketSender(this);
    final LinkedBlockingQueue<ClientboundPacket> handlingQueue = new LinkedBlockingQueue<>();
    final VelocityHandler velocityHandler = new VelocityHandler(this);
    final HashSet<PingCallback> pingCallbacks = new HashSet<>();
    final int connectionId;
    final Player player;
    final String hostname;
    LinkedList<ServerAddress> addresses;
    int desiredVersionNumber = -1;
    ServerAddress address;
    PluginChannelHandler pluginChannelHandler;
    Thread handleThread;
    Version version = Versions.getLowestVersionSupported(); // default
    final CustomMapping customMapping = new CustomMapping(version);
    ConnectionStates state = ConnectionStates.DISCONNECTED;
    ConnectionReasons reason;
    ConnectionReasons nextReason;
    ConnectionPing connectionStatusPing;
    ServerListPing lastPing;

    public Connection(int connectionId, String hostname, Player player) {
        this.connectionId = connectionId;
        this.player = player;
        this.hostname = hostname;
    }

    public void resolve(ConnectionReasons reason, int protocolId) {
        network.lastException = null;
        this.desiredVersionNumber = protocolId;

        Thread resolveThread = new Thread(() -> {
            if (desiredVersionNumber != -1) {
                setVersion(Versions.getVersionById(desiredVersionNumber));
            }
            if (addresses == null) {
                try {
                    addresses = DNSUtil.getServerAddresses(hostname);
                } catch (TextParseException e) {
                    setConnectionState(ConnectionStates.FAILED_NO_RETRY);
                    network.lastException = e;
                    e.printStackTrace();
                    return;
                }
            }
            address = addresses.getFirst();
            this.nextReason = reason;
            Log.info(String.format("Trying to connect to %s", address));
            if (protocolId != -1) {
                setVersion(Versions.getVersionById(protocolId));
            }
            resolve(address);
        });
        resolveThread.setName(String.format("%d/Resolving", connectionId));
        resolveThread.start();
    }

    public void resolve(ConnectionReasons reason) {
        resolve(reason, -1);
    }

    public void resolve(ServerAddress address) {
        reason = ConnectionReasons.DNS;
        network.connect(address);
    }

    private void connect() {
        Log.info(String.format("Connecting to server: %s", address));
        if (reason == null || reason == ConnectionReasons.DNS) {
            // first get version, then login
            reason = ConnectionReasons.GET_VERSION;
        }
        network.connect(address);
    }

    public void connect(ServerAddress address, Version version) {
        this.address = address;
        this.reason = ConnectionReasons.CONNECT;
        setVersion(version);
        Log.info(String.format("Connecting to server: %s", address));
        network.connect(address);
    }

    public ServerAddress getAddress() {
        return address;
    }

    public LinkedList<ServerAddress> getAvailableAddresses() {
        return addresses;
    }

    public ConnectionStates getConnectionState() {
        return state;
    }

    public void setConnectionState(ConnectionStates state) {
        if (this.state == state) {
            return;
        }
        Log.verbose("ConnectionStatus changed: " + state);
        ConnectionStates previousState = this.state;
        this.state = state;
        switch (state) {
            case HANDSHAKING:
                // connection established, starting threads and logging in
                startHandlingThread();
                ConnectionStates next = ((reason == ConnectionReasons.CONNECT) ? ConnectionStates.LOGIN : ConnectionStates.STATUS);
                if (reason == ConnectionReasons.DNS) {
                    // valid hostname found
                    reason = nextReason;
                    Log.info(String.format("Connection to %s seems to be okay, connecting...", address));
                }
                network.sendPacket(new PacketHandshake(address, next, (next == ConnectionStates.STATUS) ? -1 : getVersion().getProtocolVersion()));
                // after sending it, switch to next state
                setConnectionState(next);
                break;
            case STATUS:
                // send status request and ping
                network.sendPacket(new PacketStatusRequest());
                connectionStatusPing = new ConnectionPing();
                network.sendPacket(new PacketStatusPing(connectionStatusPing));
                break;
            case LOGIN:
                network.sendPacket(new PacketLoginStart(player));
                pluginChannelHandler = new PluginChannelHandler(this);
                registerDefaultChannels();
                break;
            case DISCONNECTED:
                if (reason == ConnectionReasons.GET_VERSION) {
                    setReason(ConnectionReasons.CONNECT);
                    connect();
                } else {
                    // unregister all custom recipes
                    Recipes.removeCustomRecipes();
                }
                break;
            case FAILED:
                // connect to next hostname, if available
                if (previousState == ConnectionStates.PLAY) {
                    // connection was good, do not reconnect
                    break;
                }
                int nextIndex = addresses.indexOf(address) + 1;
                if (addresses.size() > nextIndex) {
                    ServerAddress nextAddress = addresses.get(nextIndex);
                    Log.warn(String.format("Could not connect to %s, trying next hostname: %s", address, nextAddress));
                    this.address = nextAddress;
                    resolve(address);
                } else {
                    // no connection and no servers available anymore... sorry, but you can not play today :(
                    handleCallbacks(null);
                }
                break;
            case FAILED_NO_RETRY:
                handleCallbacks(null);
                break;
        }
    }

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        if (this.version == version) {
            return;
        }

        this.version = version;
        this.customMapping.setVersion(version);
        try {
            Versions.loadVersionMappings(version.getProtocolVersion());
        } catch (IOException e) {
            if (Log.getLevel().ordinal() >= LogLevels.DEBUG.ordinal()) {
                e.printStackTrace();
            }
            Log.fatal(String.format("Could not load mapping for %s. This version seems to be unsupported!", version));
            network.lastException = new RuntimeException(String.format("Mappings could not be loaded: %s", e.getLocalizedMessage()));
            setConnectionState(ConnectionStates.FAILED_NO_RETRY);
        }
    }

    public PacketHandler getHandler() {
        return this.handler;
    }

    public void handle(ClientboundPacket p) {
        handlingQueue.add(p);
    }

    public ConnectionReasons getReason() {
        return reason;
    }

    public void setReason(ConnectionReasons reason) {
        this.reason = reason;
    }

    public void disconnect() {
        setConnectionState(ConnectionStates.DISCONNECTING);
        network.disconnect();
        handleThread.interrupt();
    }

    public Player getPlayer() {
        return player;
    }

    public void sendPacket(ServerboundPacket p) {
        network.sendPacket(p);
    }

    void startHandlingThread() {
        handleThread = new Thread(() -> {
            while (isConnected()) {
                ClientboundPacket packet;
                try {
                    packet = handlingQueue.take();
                } catch (InterruptedException e) {
                    continue;
                }
                try {
                    packet.log();
                    packet.handle(getHandler());
                } catch (Exception e) {
                    if (Log.getLevel().ordinal() >= LogLevels.DEBUG.ordinal()) {
                        e.printStackTrace();
                    }
                }
            }
        });
        handleThread.setName(String.format("%d/Handling", connectionId));
        handleThread.start();
    }

    public PluginChannelHandler getPluginChannelHandler() {
        return pluginChannelHandler;
    }

    public void registerDefaultChannels() {
        // MC|Brand
        getPluginChannelHandler().registerClientHandler(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(version.getProtocolVersion()), (handler, buffer) -> {
            String serverVersion;
            String clientVersion = (Minosoft.getConfig().getBoolean(GameConfiguration.NETWORK_FAKE_CLIENT_BRAND) ? "vanilla" : "Minosoft");
            OutByteBuffer toSend = new OutByteBuffer(this);
            if (getVersion().getProtocolVersion() < 29) {
                // no length prefix
                serverVersion = new String(buffer.readBytes(buffer.getBytesLeft()));
                toSend.writeBytes(clientVersion.getBytes());
            } else {
                // length prefix
                serverVersion = buffer.readString();
                toSend.writeString(clientVersion);
            }
            Log.info(String.format("Server is running \"%s\", connected with %s", serverVersion, getVersion().getVersionName()));

            getPluginChannelHandler().sendRawData(DefaultPluginChannels.MC_BRAND.getChangeableIdentifier().get(version.getProtocolVersion()), toSend);
        });

        // MC|StopSound
        getPluginChannelHandler().registerClientHandler(DefaultPluginChannels.STOP_SOUND.getChangeableIdentifier().get(version.getProtocolVersion()), (handler, buffer) -> {
            // it is basically a packet, handle it like a packet:
            PacketStopSound packet = new PacketStopSound();
            packet.read(buffer);
            handle(packet);
        });
    }

    public boolean isConnected() {
        return state != ConnectionStates.FAILED && state != ConnectionStates.FAILED_NO_RETRY && state != ConnectionStates.DISCONNECTING && state != ConnectionStates.DISCONNECTED && state != ConnectionStates.CONNECTING;
    }

    public PacketSender getSender() {
        return sender;
    }

    public ConnectionPing getConnectionStatusPing() {
        return connectionStatusPing;
    }

    public CustomMapping getMapping() {
        return customMapping;
    }

    public int getPacketCommand(Packets.Serverbound packet) {
        Integer command = null;
        if (getReason() == ConnectionReasons.CONNECT) {
            command = version.getCommandByPacket(packet);
        }
        if (command == null) {
            return Protocol.getPacketCommand(packet);
        }
        return command;
    }

    public Packets.Clientbound getPacketByCommand(ConnectionStates state, int command) {
        Packets.Clientbound packet = null;
        if (getReason() == ConnectionReasons.CONNECT) {
            packet = version.getPacketByCommand(state, command);
        }
        if (packet == null) {
            return Protocol.getPacketByCommand(state, command);
        }
        return packet;
    }

    public VelocityHandler getVelocityHandler() {
        return velocityHandler;
    }

    public int getConnectionId() {
        return connectionId;
    }

    public void addPingCallback(PingCallback pingCallback) {
        if (getConnectionState() == ConnectionStates.FAILED || getConnectionState() == ConnectionStates.FAILED_NO_RETRY || lastPing != null) {
            // ping done
            pingCallback.handle(lastPing);
            return;
        }
        pingCallbacks.add(pingCallback);
    }

    public HashSet<PingCallback> getPingCallbacks() {
        return pingCallbacks;
    }

    public int getDesiredVersionNumber() {
        return desiredVersionNumber;
    }

    public void setDesiredVersionNumber(int desiredVersionNumber) {
        this.desiredVersionNumber = desiredVersionNumber;
    }

    public void handleCallbacks(ServerListPing ping) {
        this.lastPing = ping;
        for (PingCallback callback : getPingCallbacks()) {
            callback.handle(ping);
        }
    }

    public Exception getLastConnectionException() {
        return network.lastException;
    }

    public ServerListPing getLastPing() {
        return lastPing;
    }
}
