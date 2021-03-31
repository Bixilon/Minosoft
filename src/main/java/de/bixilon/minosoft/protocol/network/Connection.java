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
import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.VelocityHandler;
import de.bixilon.minosoft.data.accounts.Account;
import de.bixilon.minosoft.data.commands.CommandRootNode;
import de.bixilon.minosoft.data.mappings.MappingsLoadingException;
import de.bixilon.minosoft.data.mappings.recipes.Recipes;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.VersionMapping;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.data.player.Player;
import de.bixilon.minosoft.data.player.tab.TabList;
import de.bixilon.minosoft.data.scoreboard.ScoreboardManager;
import de.bixilon.minosoft.data.world.World;
import de.bixilon.minosoft.gui.rendering.Rendering;
import de.bixilon.minosoft.modding.event.EventInvoker;
import de.bixilon.minosoft.modding.event.events.*;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.PacketHandshake;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketLoginStart;
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusRequest;
import de.bixilon.minosoft.protocol.ping.ServerListPing;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.terminal.CLI;
import de.bixilon.minosoft.terminal.commands.commands.Command;
import de.bixilon.minosoft.util.CountUpAndDownLatch;
import de.bixilon.minosoft.util.DNSUtil;
import de.bixilon.minosoft.util.ServerAddress;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.logging.LogLevels;
import org.xbill.DNS.TextParseException;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicBoolean;

public class Connection {
    public static int lastConnectionId;
    private final Network network = Network.getNetworkInstance(this);
    private final PacketSender sender = new PacketSender(this);
    private final VelocityHandler velocityHandler = new VelocityHandler(this);
    private final LinkedList<EventInvoker> eventListeners = new LinkedList<>();
    private final int connectionId;
    private final Player player;
    private final String hostname;
    private final Recipes recipes = new Recipes();
    private final World world = new World();
    private final TabList tabList = new TabList();
    private final ScoreboardManager scoreboardManager = new ScoreboardManager();
    private Rendering rendering;
    private LinkedList<ServerAddress> addresses;
    private int desiredVersionNumber = -1;
    private ServerAddress address;
    private Version version = Versions.LOWEST_VERSION_SUPPORTED; // default
    private final VersionMapping customMapping = new VersionMapping(this.version);
    private ConnectionStates state = ConnectionStates.DISCONNECTED;
    private ConnectionReasons reason;
    private ConnectionReasons nextReason;
    private ServerListPing lastPing;
    private Exception lastException;
    private CommandRootNode commandRootNode;
    private ConnectionPing connectionStatusPing;
    private ServerListPongEvent pong;

    public Connection(int connectionId, String hostname, Account account) {
        this.connectionId = connectionId;
        if (account == null) {
            this.player = null;
        } else {
            this.player = new Player(account, this);
        }
        this.hostname = hostname;
    }

    public void resolve(ConnectionReasons reason, int versionId) {
        this.lastException = null;
        this.desiredVersionNumber = versionId;

        Minosoft.THREAD_POOL.execute(() -> {
            if (this.desiredVersionNumber != -1) {
                setVersion(Versions.getVersionById(this.desiredVersionNumber));
            }
            if (this.addresses == null) {
                try {
                    this.addresses = DNSUtil.getServerAddresses(this.hostname);
                } catch (TextParseException e) {
                    setConnectionState(ConnectionStates.FAILED_NO_RETRY);
                    this.lastException = e;
                    e.printStackTrace();
                    return;
                }
            }
            this.address = this.addresses.getFirst();
            this.nextReason = reason;
            Log.info(String.format("Trying to connect to %s", this.address));
            if (versionId != -1) {
                setVersion(Versions.getVersionById(versionId));
            }
            resolve(this.address);
        });
    }

    public void resolve(ConnectionReasons reason) {
        resolve(reason, -1);
    }

    public void resolve(ServerAddress address) {
        this.reason = ConnectionReasons.DNS;
        this.network.connect(address);
    }

    private void connect() {
        Log.info(String.format("Connecting to server: %s", this.address));
        if (this.reason == null || this.reason == ConnectionReasons.DNS) {
            // first get version, then login
            this.reason = ConnectionReasons.GET_VERSION;
        }
        this.network.connect(this.address);
    }

    public void connect(ServerAddress address, Version version, CountUpAndDownLatch latch) {
        this.address = address;
        this.reason = ConnectionReasons.CONNECT;
        setVersion(version);
        try {
            version.load(latch);  // ToDo: show gui loader
            this.customMapping.setVersion(version);
            this.customMapping.setParentMapping(version.getMapping());

            if (!StaticConfiguration.HEADLESS_MODE) {
                this.rendering = new Rendering(this);
                this.rendering.start(latch);
            }
            latch.waitForChange();
            Log.info(String.format("Connecting to server: %s", address));
            this.network.connect(address);
            latch.countDown();
        } catch (Throwable exception) {
            Log.printException(exception, LogLevels.DEBUG);
            Log.fatal(String.format("Could not load version %s. This version seems to be unsupported!", version));
            if (this.customMapping.getVersion() != null) {
                this.customMapping.getVersion().getMapping().setParentMapping(null);
            }
            this.customMapping.setVersion(null);
            version.unload();
            this.lastException = new MappingsLoadingException("Mappings could not be loaded", exception);
            setConnectionState(ConnectionStates.FAILED_NO_RETRY);
        }
    }

    public ServerAddress getAddress() {
        return this.address;
    }

    public LinkedList<ServerAddress> getAvailableAddresses() {
        return this.addresses;
    }

    public Version getVersion() {
        return this.version;
    }

    public void setVersion(Version version) {
        if (this.version == version) {
            return;
        }

        this.version = version;
    }

    public void handle(PacketTypes.Clientbound packetType, ClientboundPacket packet) {
        if (!packetType.isThreadSafe()) {
            handlePacket(packet);
            return;
        }
        Minosoft.THREAD_POOL.execute(() -> {
            handlePacket(packet);
        });
    }

    private void handlePacket(ClientboundPacket packet) {
        try {
            if (Log.getLevel().ordinal() >= LogLevels.PROTOCOL.ordinal()) {
                packet.log();
            }
            PacketReceiveEvent event = new PacketReceiveEvent(this, packet);
            if (fireEvent(event)) {
                return;
            }
            packet.handle(this);
        } catch (Throwable exception) {
            Log.printException(exception, LogLevels.PROTOCOL);
        }
    }

    public void disconnect() {
        this.network.disconnect();
    }

    public Player getPlayer() {
        return this.player;
    }

    public void sendPacket(ServerboundPacket packet) {
        PacketSendEvent event = new PacketSendEvent(this, packet);
        if (fireEvent(event)) {
            return;
        }
        this.network.sendPacket(packet);
    }

    /**
     * @param connectionEvent The event to fire
     * @return if the event has been cancelled or not
     */
    public boolean fireEvent(ConnectionEvent connectionEvent) {
        synchronized (Minosoft.EVENT_MANAGERS) {
            for (var eventManager : Minosoft.EVENT_MANAGERS) {
                final var globalListeners = eventManager.getGlobalEventListeners();
                synchronized (globalListeners) {
                    for (var eventListener : globalListeners) {
                        eventListener.invoke(connectionEvent);
                    }
                }
            }
        }
        synchronized (this.eventListeners) {
            for (var eventListener : this.eventListeners) {
                eventListener.invoke(connectionEvent);
            }
        }

        if (connectionEvent instanceof CancelableEvent cancelableEvent) {
            return cancelableEvent.isCancelled();
        }
        return false;
    }

    public boolean isConnected() {
        return this.state != ConnectionStates.FAILED && this.state != ConnectionStates.FAILED_NO_RETRY && this.state != ConnectionStates.DISCONNECTING && this.state != ConnectionStates.DISCONNECTED && this.state != ConnectionStates.CONNECTING;
    }

    public PacketSender getSender() {
        return this.sender;
    }

    public ConnectionPing getConnectionStatusPing() {
        return this.connectionStatusPing;
    }

    public void setConnectionStatusPing(ConnectionPing connectionStatusPing) {
        this.connectionStatusPing = connectionStatusPing;
    }

    public VersionMapping getMapping() {
        return this.customMapping;
    }

    public int getPacketCommand(PacketTypes.Serverbound packet) {
        Integer command = null;
        if (getReason() == ConnectionReasons.CONNECT) {
            command = this.version.getCommandByPacket(packet);
        }
        if (command == null) {
            return Protocol.getPacketCommand(packet);
        }
        return command;
    }

    public ConnectionReasons getReason() {
        return this.reason;
    }

    public void setReason(ConnectionReasons reason) {
        this.reason = reason;
    }

    public PacketTypes.Clientbound getPacketByCommand(ConnectionStates state, int command) {
        PacketTypes.Clientbound packet = null;
        if (getReason() == ConnectionReasons.CONNECT) {
            packet = this.version.getPacketByCommand(state, command);
        }
        if (packet == null) {
            return Protocol.getPacketByCommand(state, command);
        }
        return packet;
    }

    public VelocityHandler getVelocityHandler() {
        return this.velocityHandler;
    }

    public int getConnectionId() {
        return this.connectionId;
    }

    public ConnectionStates getConnectionState() {
        return this.state;
    }

    public void setConnectionState(ConnectionStates state) {
        ConnectionStates previousState = this.state;
        synchronized (state) {
            if (this.state == state) {
                return;
            }
            Log.verbose("ConnectionState changed: " + state);
            this.state = state;
        }
        switch (state) {
            case HANDSHAKING -> {
                // get and add all events, that are connection specific
                Minosoft.EVENT_MANAGERS.forEach((eventManagers -> eventManagers.getSpecificEventListeners().forEach((serverAddresses, listener) -> {
                    AtomicBoolean isValid = new AtomicBoolean(false);
                    serverAddresses.forEach((validator) -> {
                        if (validator.check(this.address)) {
                            isValid.set(true);
                        }
                    });
                    if (isValid.get()) {
                        this.eventListeners.addAll(listener);
                    }
                })));
                this.eventListeners.sort((a, b) -> {
                    if (a == null || b == null) {
                        return 0;
                    }
                    return -(b.getPriority().ordinal() - a.getPriority().ordinal());
                });

                ConnectionStates next = ((this.reason == ConnectionReasons.CONNECT) ? ConnectionStates.LOGIN : ConnectionStates.STATUS);
                if (this.reason == ConnectionReasons.DNS) {
                    // valid hostname found
                    this.reason = this.nextReason;
                    Log.info(String.format("Connection to %s seems to be okay, connecting...", this.address));
                }
                this.network.sendPacket(new PacketHandshake(this.address, next, (next == ConnectionStates.STATUS) ? -1 : getVersion().getProtocolId()));
                // after sending it, switch to next state
                setConnectionState(next);
            }
            case STATUS -> this.network.sendPacket(new PacketStatusRequest());
            case LOGIN -> this.network.sendPacket(new PacketLoginStart(this.player));
            case DISCONNECTED -> {
                if (this.reason == ConnectionReasons.GET_VERSION) {
                    setReason(ConnectionReasons.CONNECT);
                    connect();
                } else {
                    // unregister all custom recipes
                    this.recipes.removeCustomRecipes();
                    Minosoft.CONNECTIONS.remove(getConnectionId());
                    if (CLI.getCurrentConnection() == this) {
                        CLI.setCurrentConnection(null);
                        Command.print("Disconnected from current connection!");
                    }
                }

            }
            case FAILED -> {
                // connect to next hostname, if available
                if (previousState == ConnectionStates.PLAY) {
                    // connection was good, do not reconnect
                    break;
                }
                if (this.addresses == null) {
                    handlePingCallbacks(null);
                    break;
                }
                int nextIndex = this.addresses.indexOf(this.address) + 1;
                if (this.addresses.size() > nextIndex) {
                    ServerAddress nextAddress = this.addresses.get(nextIndex);
                    Log.warn(String.format("Could not connect to %s, trying next hostname: %s", this.address, nextAddress));
                    this.address = nextAddress;
                    resolve(this.address);
                } else {
                    // no connection and no servers available anymore... sorry, but you can not play today :(
                    handlePingCallbacks(null);
                }
            }
            case FAILED_NO_RETRY -> handlePingCallbacks(null);
            case PLAY -> {
                Minosoft.CONNECTIONS.put(getConnectionId(), this);

                if (CLI.getCurrentConnection() == null) {
                    CLI.setCurrentConnection(this);
                }
            }
        }
        // handle callbacks
        fireEvent(new ConnectionStateChangeEvent(this, previousState, state));
    }

    public void handlePingCallbacks(@Nullable ServerListPing ping) {
        this.lastPing = ping;
        fireEvent(new ServerListStatusArriveEvent(this, ping));
    }

    public int getDesiredVersionNumber() {
        return this.desiredVersionNumber;
    }

    public void setDesiredVersionNumber(int desiredVersionNumber) {
        this.desiredVersionNumber = desiredVersionNumber;
    }

    public void unregisterEvent(EventInvoker method) {
        this.eventListeners.remove(method);
    }

    public void registerEvent(EventInvoker method) {
        if (method.getEventType().isAssignableFrom(ServerListStatusArriveEvent.class) && wasPingDone()) {
            // ping done
            method.invoke(new ServerListStatusArriveEvent(this, this.lastPing));
        } else if (method.getEventType().isAssignableFrom(ServerListPongEvent.class) && wasPingDone() && this.pong != null) {
            method.invoke(this.pong);
        } else {
            this.eventListeners.add(method);
        }
    }

    private boolean wasPingDone() {
        return getConnectionState() == ConnectionStates.FAILED || getConnectionState() == ConnectionStates.FAILED_NO_RETRY || this.lastPing != null;
    }

    public Throwable getLastConnectionException() {
        return (this.lastException != null) ? this.lastException : this.network.getLastException();
    }

    public ServerListPing getLastPing() {
        return this.lastPing;
    }

    public Recipes getRecipes() {
        return this.recipes;
    }

    @Nullable
    public CommandRootNode getCommandRootNode() {
        return this.commandRootNode;
    }

    public void setCommandRootNode(CommandRootNode commandRootNode) {
        this.commandRootNode = commandRootNode;
    }

    public ServerListPongEvent getPong() {
        return this.pong;
    }

    public void setPong(ServerListPongEvent pong) {
        this.pong = pong;
    }

    public boolean isDisconnected() {
        return getConnectionState() == ConnectionStates.DISCONNECTING || getConnectionState() == ConnectionStates.DISCONNECTED || getConnectionState() == ConnectionStates.FAILED || getConnectionState() == ConnectionStates.FAILED_NO_RETRY;
    }

    @Override
    public String toString() {
        return String.format("(id=%d, address=%s, account=\"%s\")", getConnectionId(), getAddress(), ((this.player == null) ? null : getPlayer().getAccount()));
    }

    public Rendering getRenderer() {
        return this.rendering;
    }

    public int getEventListenerSize() {
        return this.eventListeners.size();
    }

    public Network getNetwork() {
        return this.network;
    }

    public World getWorld() {
        return this.world;
    }

    public TabList getTabList() {
        return this.tabList;
    }

    public ScoreboardManager getScoreboardManager() {
        return this.scoreboardManager;
    }
}
