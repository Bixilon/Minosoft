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
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.ServerAddress;
import de.bixilon.minosoft.util.Util;
import javafx.application.Platform;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;

public class LANServerListener {
    private final static String MOTD_BEGIN_STRING = "[MOTD]";
    private final static String MOTD_END_STRING = "[/MOTD]";
    private final static String PORT_START_STRING = "[AD]";
    private final static String PORT_END_STRING = "[/AD]";
    private final static String[] BROADCAST_MUST_CONTAIN = {MOTD_BEGIN_STRING, MOTD_END_STRING, PORT_START_STRING, PORT_END_STRING};
    public static HashBiMap<InetAddress, Server> servers;

    public static void listen() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        new Thread(() -> {
            try {
                servers = HashBiMap.create();
                MulticastSocket socket = new MulticastSocket(ProtocolDefinition.LAN_SERVER_BROADCAST_PORT);
                socket.joinGroup(ProtocolDefinition.LAN_SERVER_BROADCAST_ADDRESS); // ToDo: do not use deprecated methods
                byte[] buf = new byte[65535];
                latch.countDown();
                while (true) {
                    try {
                        DatagramPacket packet = new DatagramPacket(buf, buf.length);
                        socket.receive(packet);
                        Log.protocol(String.format("LAN UDP Broadcast from %s:%s -> %s", packet.getAddress().getHostAddress(), packet.getPort(), new String(buf)));
                        InetAddress sender = packet.getAddress();
                        if (servers.containsKey(sender)) {
                            // This guy sent us already a server, maybe a duplicate or a DOS attack...Skip
                            continue;
                        }
                        Server server = getServerByBroadcast(sender, packet.getData());
                        if (servers.containsValue(server)) {
                            continue;
                        }
                        if (servers.size() > ProtocolDefinition.LAN_SERVER_MAXIMUM_SERVERS) {
                            continue;
                        }
                        servers.put(sender, server);
                        Platform.runLater(() -> ServerListCell.listView.getItems().add(server));
                        Log.debug(String.format("Discovered new LAN Server: %s", server));

                    } catch (Exception ignored) {
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            servers = null;
            Log.warn("Stopping LAN Server Listener Thread");
        }, "LAN Server Listener").start();
        latch.await();
    }

    public static HashBiMap<InetAddress, Server> getServers() {
        return servers;
    }

    public static void removeAll() {
        HashSet<Server> temp = new HashSet<>(servers.values());
        for (Server server : temp) {
            if (server.isConnected()) {
                continue;
            }
            servers.inverse().remove(server);
        }

    }

    private static Server getServerByBroadcast(InetAddress address, byte[] broadcast) {
        String parsed = new String(broadcast, StandardCharsets.UTF_8);
        for (String mustContain : BROADCAST_MUST_CONTAIN) {
            if (!parsed.contains(mustContain)) {
                throw new IllegalArgumentException("Broadcast is invalid!");
            }
        }
        String rawAddress = Util.getStringBetween(parsed, PORT_START_STRING, PORT_END_STRING);
        if (rawAddress.contains(":")) {
            // weired, just extract the port
            rawAddress = rawAddress.split(":")[1];
        }
        int port = Integer.parseInt(rawAddress);
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException(String.format("Invalid port: %d", port));
        }
        return new Server(new ServerAddress(address.getHostAddress(), port));
    }
}
