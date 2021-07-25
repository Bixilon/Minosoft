/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.protocol.protocol

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.server.Server
import de.bixilon.minosoft.modding.event.events.LANServerDiscoverEvent
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.net.*
import java.nio.charset.StandardCharsets
import java.util.concurrent.CountDownLatch

object LANServerListener {
    val SERVERS: HashBiMap<InetAddress, Server> = HashBiMap.create()
    private const val MOTD_BEGIN_STRING = "[MOTD]"
    private const val MOTD_END_STRING = "[/MOTD]"
    private const val PORT_START_STRING = "[AD]"
    private const val PORT_END_STRING = "[/AD]"
    private val BROADCAST_MUST_CONTAIN = arrayOf(MOTD_BEGIN_STRING, MOTD_END_STRING, PORT_START_STRING, PORT_END_STRING)

    fun listen() {
        val latch = CountDownLatch(1)
        Thread({
            try {
                val socket = MulticastSocket(ProtocolDefinition.LAN_SERVER_BROADCAST_PORT)
                socket.joinGroup(InetSocketAddress(ProtocolDefinition.LAN_SERVER_BROADCAST_INET_ADDRESS, ProtocolDefinition.LAN_SERVER_BROADCAST_PORT), NetworkInterface.getByInetAddress(ProtocolDefinition.LAN_SERVER_BROADCAST_INET_ADDRESS))
                val buffer = ByteArray(256) // this should be enough, if the packet is longer, it is probably invalid
                Log.log(LogMessageType.NETWORK_STATUS, LogLevels.INFO) { "Listening for LAN servers..." }
                latch.countDown()
                while (true) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)
                        val broadcast = String(buffer, 0, packet.length, StandardCharsets.UTF_8)
                        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.INFO) { "Received LAN servers broadcast (${packet.address.hostAddress}:${packet.port}): $broadcast" }
                        val sender = packet.address
                        if (SERVERS.containsKey(sender)) {
                            // This guy sent us already a server, maybe just the regular 1.5 second interval, a duplicate or a DOS attack...We don't care
                            continue
                        }
                        val server = getServerByBroadcast(sender, broadcast)
                        if (SERVERS.containsValue(server)) {
                            continue
                        }
                        if (SERVERS.size > ProtocolDefinition.LAN_SERVER_MAXIMUM_SERVERS) {
                            continue
                        }
                        if (Minosoft.GLOBAL_EVENT_MASTER.fireEvent(LANServerDiscoverEvent(packet.address, server))) {
                            continue
                        }
                        SERVERS[sender] = server
                        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.INFO) { "Discovered LAN servers: $server" }
                    } catch (ignored: Throwable) {
                    }
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                latch.countDown()
            }
            SERVERS.clear()
            Log.log(LogMessageType.NETWORK_STATUS, LogLevels.INFO) { "Stop listening for LAN servers..." }
        }, "LAN Server Listener").start()
        latch.await()
    }

    private fun getServerByBroadcast(address: InetAddress, broadcast: String): Server {
        // example: [MOTD]Bixilon - New World[/MOTD][AD]41127[/AD]
        for (mustContain in BROADCAST_MUST_CONTAIN) {
            require(broadcast.contains(mustContain)) { "Broadcast is invalid!" }
        }
        var rawAddress = Util.getStringBetween(broadcast, PORT_START_STRING, PORT_END_STRING)
        if (rawAddress.contains(":")) {
            // weird, just extract the port
            rawAddress = rawAddress.split(":").toTypedArray()[1]
        }
        val port = rawAddress.toInt()
        require(!(port < 0 || port > 65535)) { String.format("Invalid port: %d", port) }
        return Server(address = address.hostAddress + ":" + rawAddress) // ToDo: Name
    }
}
