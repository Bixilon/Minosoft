/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.Player;
import de.bixilon.minosoft.data.VelocityHandler;
import de.bixilon.minosoft.data.commands.CommandRootNode;
import de.bixilon.minosoft.data.mappings.MappingsLoadingException;
import de.bixilon.minosoft.data.mappings.recipes.Recipes;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.VersionMapping;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.modding.event.EventInvoker;
import de.bixilon.minosoft.modding.event.events.*;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.PacketHandshake;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketLoginStart;
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusRequest;
import de.bixilon.minosoft.protocol.ping.ServerListPing;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.util.DNSUtil;
import de.bixilon.minosoft.util.ServerAddress;
import org.xbill.DNS.TextParseException;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class Connection {
    public static int lastConnectionId;
    final Network network = Network.getNetworkInstance(this);
    final PacketHandler handler = new PacketHandler(this);
    final PacketSender sender = new PacketSender(this);
    final LinkedBlockingQueue<ClientboundPacket> handlingQueue = new LinkedBlockingQueue<>();
    final VelocityHandler velocityHandler = new VelocityHandler(this);
    final LinkedList<EventInvoker> eventListeners = new LinkedList<>();
    final int connectionId;
    final Player player;
    final String hostname;
    LinkedList<ServerAddress> addresses;
    int desiredVersionNumber = -1;
    ServerAddress address;
    Thread handleThread;
    Version version = Versions.LOWEST_VERSION_SUPPORTED; // default
    final VersionMapping customMapping = new VersionMapping(version);
    ConnectionStates state = ConnectionStates.DISCONNECTED;
    ConnectionReasons reason;
    ConnectionReasons nextReason;
    public ConnectionPing connectionStatusPing;
    ServerListPing lastPing;
    Exception lastException;
    private CommandRootNode commandRootNode;

    public Connection(int connectionId, String hostname, Player player) {
        this.connectionId = connectionId;
        this.player = player;
        this.hostname = hostname;
    }

    public void resolve(ConnectionReasons reason, int versionId) {
        lastException = null;
        this.desiredVersionNumber = versionId;

        Thread resolveThread = new Thread(() -> {
            if (desiredVersionNumber != -1) {
                setVersion(Versions.getVersionById(desiredVersionNumber));
            }
            if (addresses == null) {
                try {
                    addresses = DNSUtil.getServerAddresses(hostname);
                } catch (TextParseException e) {
                    setConnectionState(ConnectionStates.FAILED_NO_RETRY);
                    lastException = e;
                    e.printStackTrace();
                    return;
                }
            }
            address = addresses.getFirst();
            this.nextReason = reason;
            Log.info(String.format("Trying to connect to %s", address));
            if (versionId != -1) {
                setVersion(Versions.getVersionById(versionId));
            }
            resolve(address);
        }, String.format("%d/Resolving", connectionId));
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

    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        if (this.version == version) {
            return;
        }

        this.version = version;
        try {
            Versions.loadVersionMappings(version);
            customMapping.setVersion(version);
            this.customMapping.setParentMapping(version.getMapping());
        } catch (Exception e) {
            Log.printException(e, LogLevels.DEBUG);
            Log.fatal(String.format("Could not load mapping for %s. This version seems to be unsupported!", version));
            lastException = new MappingsLoadingException("Mappings could not be loaded", e);
            setConnectionState(ConnectionStates.FAILED_NO_RETRY);
        }
    }

    public PacketHandler getHandler() {
        return this.handler;
    }

    public void handle(ClientboundPacket p) {
        handlingQueue.add(p);
    }

    public void disconnect() {
        setConnectionState(ConnectionStates.DISCONNECTING);
        network.disconnect();
        handleThread.interrupt();
    }

    public Player getPlayer() {
        return player;
    }

    public void sendPacket(ServerboundPacket packet) {
        PacketSendEvent event = new PacketSendEvent(this, packet);
        if (fireEvent(event)) {
            return;
        }
        network.sendPacket(packet);
    }

    /**
     * @param connectionEvent The event to fire
     * @return if the event has been cancelled or not
     */
    public boolean fireEvent(ConnectionEvent connectionEvent) {
        Minosoft.eventManagers.forEach((eventManager -> eventManager.getGlobalEventListeners().forEach((method) -> method.invoke(connectionEvent))));
        eventListeners.forEach((method -> method.invoke(connectionEvent)));
        if (connectionEvent instanceof CancelableEvent cancelableEvent) {
            return cancelableEvent.isCancelled();
        }
        return false;
    }

    private void startHandlingThread() {
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
                    PacketReceiveEvent event = new PacketReceiveEvent(this, packet);
                    if (fireEvent(event)) {
                        continue;
                    }
                    packet.handle(getHandler());
                } catch (Throwable e) {
                    Log.printException(e, LogLevels.PROTOCOL);
                }
            }
        }, String.format("%d/Handling", connectionId));
        handleThread.start();
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

    public VersionMapping getMapping() {
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

    public ConnectionReasons getReason() {
        return reason;
    }

    public void setReason(ConnectionReasons reason) {
        this.reason = reason;
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

    public ConnectionStates getConnectionState() {
        return state;
    }

    public void setConnectionState(ConnectionStates state) {
        if (this.state == state) {
            return;
        }
        Log.verbose("ConnectionState changed: " + state);
        ConnectionStates previousState = this.state;
        this.state = state;
        switch (state) {
            case HANDSHAKING -> {
                // get and add all events, that are connection specific
                Minosoft.eventManagers.forEach((eventManagers -> eventManagers.getSpecificEventListeners().forEach((serverAddresses, listener) -> {
                    AtomicBoolean isValid = new AtomicBoolean(false);
                    serverAddresses.forEach((validator) -> {
                        if (validator.check(address)) {
                            isValid.set(true);
                        }
                    });
                    if (isValid.get()) {
                        eventListeners.addAll(listener);
                    }
                })));
                eventListeners.sort((a, b) -> {
                    if (a == null || b == null) {
                        return 0;
                    }
                    return -(b.getPriority().ordinal() - a.getPriority().ordinal());
                });
                // connection established, starting threads and logging in
                startHandlingThread();
                ConnectionStates next = ((reason == ConnectionReasons.CONNECT) ? ConnectionStates.LOGIN : ConnectionStates.STATUS);
                if (reason == ConnectionReasons.DNS) {
                    // valid hostname found
                    reason = nextReason;
                    Log.info(String.format("Connection to %s seems to be okay, connecting...", address));
                }
                network.sendPacket(new PacketHandshake(address, next, (next == ConnectionStates.STATUS) ? -1 : getVersion().getProtocolId()));
                // after sending it, switch to next state
                setConnectionState(next);
            }
            case STATUS -> {
                // send status request
                network.sendPacket(new PacketStatusRequest());
            }
            case LOGIN -> network.sendPacket(new PacketLoginStart(player));
            case DISCONNECTED -> {
                if (reason == ConnectionReasons.GET_VERSION) {
                    setReason(ConnectionReasons.CONNECT);
                    connect();
                } else {
                    // unregister all custom recipes
                    Recipes.removeCustomRecipes();
                }
            }
            case FAILED -> {
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
                    handlePingCallbacks(null);
                }
            }
            case FAILED_NO_RETRY -> handlePingCallbacks(null);
        }
        // handle callbacks
        fireEvent(new ConnectionStateChangeEvent(this, previousState, state));
    }

    public int getDesiredVersionNumber() {
        return desiredVersionNumber;
    }

    public void setDesiredVersionNumber(int desiredVersionNumber) {
        this.desiredVersionNumber = desiredVersionNumber;
    }

    public void handlePingCallbacks(@Nullable ServerListPing ping) {
        this.lastPing = ping;
        fireEvent(new ServerListPingArriveEvent(this, ping));
    }

    public void registerEvent(EventInvoker method) {
        eventListeners.add(method);
        if (method.getEventType() == ServerListPingArriveEvent.class) {
            if (getConnectionState() == ConnectionStates.FAILED || getConnectionState() == ConnectionStates.FAILED_NO_RETRY || lastPing != null) {
                // ping done
                method.invoke(new ServerListPingArriveEvent(this, lastPing));
            }
        }
    }

    public Throwable getLastConnectionException() {
        return (lastException != null) ? lastException : network.getLastException();
    }

    public ServerListPing getLastPing() {
        return lastPing;
    }

    public void setCommandRootNode(CommandRootNode commandRootNode) {
        this.commandRootNode = commandRootNode;
    }
}
