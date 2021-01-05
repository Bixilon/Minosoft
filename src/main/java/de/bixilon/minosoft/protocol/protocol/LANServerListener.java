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

package de.bixilon.minosoft.protocol.protocol;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.gui.main.ServerListCell;
import de.bixilon.minosoft.util.ServerAddress;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;
import javafx.application.Platform;

import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

public class LANServerListener {
    public static final HashBiMap<InetAddress, Server> SERVER_MAP = HashBiMap.create();
    private static final String MOTD_BEGIN_STRING = "[MOTD]";
    private static final String MOTD_END_STRING = "[/MOTD]";
    private static final String PORT_START_STRING = "[AD]";
    private static final String PORT_END_STRING = "[/AD]";
    private static final String[] BROADCAST_MUST_CONTAIN = {MOTD_BEGIN_STRING, MOTD_END_STRING, PORT_START_STRING, PORT_END_STRING};

    public static void listen() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                MulticastSocket socket = new MulticastSocket(ProtocolDefinition.LAN_SERVER_BROADCAST_PORT);
                socket.joinGroup(new InetSocketAddress(ProtocolDefinition.LAN_SERVER_BROADCAST_INET_ADDRESS, ProtocolDefinition.LAN_SERVER_BROADCAST_PORT), NetworkInterface.getByInetAddress(ProtocolDefinition.LAN_SERVER_BROADCAST_INET_ADDRESS));
                byte[] buf = new byte[256]; // this should be enough, if the packet is longer, it is probably invalid
                latch.countDown();
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        Log.protocol(String.format("LAN UDP Broadcast from %s:%s -> %s", packet.getAddress().getHostAddress(), packet.getPort(), new String(buf)));
                        InetAddress sender = packet.getAddress();
                        if (SERVER_MAP.containsKey(sender)) {
                            // This guy sent us already a server, maybe just the regular 1.5 second interval, a duplicate or a DOS attack...We don't care
                            continue;
                        }
                        Server server = getServerByBroadcast(sender, packet.getData());
                        if (SERVER_MAP.containsValue(server)) {
                            continue;
                        }
                        if (SERVER_MAP.size() > ProtocolDefinition.LAN_SERVER_MAXIMUM_SERVERS) {
                            continue;
                        }
                        SERVER_MAP.put(sender, server);
                        Platform.runLater(() -> ServerListCell.SERVER_LIST_VIEW.getItems().add(server));
                        Log.debug(String.format("Discovered new LAN Server: %s", server));
                    } catch (Exception ignored) {
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                latch.countDown();
            }
            SERVER_MAP.clear();
            Log.warn("Stopping LAN Server Listener Thread");
        }, "LAN Server Listener").start();
        latch.await();
    }

    public static HashBiMap<InetAddress, Server> getServerMap() {
        return SERVER_MAP;
    }

    public static void removeAll() {
        HashSet<Server> temp = new HashSet<>(SERVER_MAP.values());
        for (Server server : temp) {
            if (server.isConnected()) {
                continue;
            }
            SERVER_MAP.inverse().remove(server);
        }
    }

    private static Server getServerByBroadcast(InetAddress address, byte[] broadcast) {
        String parsed = new String(broadcast, StandardCharsets.UTF_8); // example: [MOTD]Bixilon - New World[/MOTD][AD]41127[/AD]
        for (String mustContain : BROADCAST_MUST_CONTAIN) {
            if (!parsed.contains(mustContain)) {
                throw new IllegalArgumentException("Broadcast is invalid!");
            }
        }
        String rawAddress = Util.getStringBetween(parsed, PORT_START_STRING, PORT_END_STRING);
        if (rawAddress.contains(":")) {
            // weird, just extract the port
            rawAddress = rawAddress.split(":")[1];
        }
        int port = Integer.parseInt(rawAddress);
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(String.format("Invalid port: %d", port));
        }
        return new Server(new ServerAddress(address.getHostAddress(), port));
    }
}
