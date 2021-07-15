/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.connection

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.modding.event.EventInvoker
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.modding.event.events.ServerListPongEvent
import de.bixilon.minosoft.modding.event.events.ServerListStatusArriveEvent
import de.bixilon.minosoft.protocol.packets.c2s.handshaking.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusRequestC2SP
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import de.bixilon.minosoft.protocol.ping.ServerListPing
import de.bixilon.minosoft.protocol.protocol.ConnectionPing
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.protocol.protocol.PacketTypes
import de.bixilon.minosoft.protocol.protocol.Protocol
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.ServerAddress
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class StatusConnection(
    val address: String,
) : Connection() {
    var lastPing: ServerListPing? = null
    var connectionStatusPing: ConnectionPing? = null
    var pong: ServerListPongEvent? = null

    lateinit var realAddress: ServerAddress
        private set
    private var addresses: List<ServerAddress>? = null

    var serverVersion: Version? = null


    fun resolve() {
        error = null

        if (this.addresses == null) {
            this.addresses = DNSUtil.getServerAddresses(address)
            this.realAddress = this.addresses!!.first()
        }
    }

    fun ping() {
        Minosoft.THREAD_POOL.execute {
            try {
                resolve()
            } catch (exception: Exception) {
                Log.log(LogMessageType.NETWORK_RESOLVING) { "Can not resolve $realAddress" }
                error = exception
                disconnect()
                return@execute
            }

            Log.log(LogMessageType.NETWORK_RESOLVING) { "Trying to ping $realAddress (from $address)" }

            network.connect(realAddress)
        }
    }


    override var connectionState: ConnectionStates = ConnectionStates.DISCONNECTED
        set(value) {
            val previousConnectionState = connectionState
            field = value
            // handle callbacks
            fireEvent(ConnectionStateChangeEvent(this, previousConnectionState, connectionState))
            when (value) {
                ConnectionStates.HANDSHAKING -> {
                    network.sendPacket(HandshakeC2SP(realAddress, ConnectionStates.STATUS, Versions.AUTOMATIC_VERSION.protocolId))
                    connectionState = ConnectionStates.STATUS
                }
                ConnectionStates.STATUS -> {
                    network.sendPacket(StatusRequestC2SP())
                }
                ConnectionStates.DISCONNECTED -> {
                    if (previousConnectionState.connected) {
                        wasConnected = true
                        handlePingCallbacks(this.lastPing)
                        return
                    }
                    if (addresses == null || error != null) {
                        handlePingCallbacks(null)
                        return
                    }

                    val nextIndex = addresses!!.indexOf(realAddress) + 1
                    if (addresses!!.size > nextIndex) {
                        val nextAddress = addresses!![nextIndex]
                        Log.log(LogMessageType.NETWORK_RESOLVING) { "Could not connect to $address, trying next hostname: $nextAddress" }
                        realAddress = nextAddress
                        ping()
                    } else {
                        // no connection and no servers available anymore... sorry, but you can not play today :(
                        handlePingCallbacks(null)
                    }
                }
                else -> {
                }
            }
        }

    fun handlePingCallbacks(ping: ServerListPing?) {
        lastPing = ping
        fireEvent(ServerListStatusArriveEvent(this, ping))
    }

    override fun getPacketId(packetType: PacketTypes.C2S): Int {
        return Protocol.getPacketId(packetType)!!
    }

    override fun getPacketById(packetId: Int): PacketTypes.S2C {
        return Protocol.getPacketById(connectionState, packetId) ?: error("Can not find packet $packetId in $connectionState")
    }

    override fun handlePacket(packet: S2CPacket) {
        try {
            packet.log()
            val event = PacketReceiveEvent(this, packet)
            if (fireEvent(event)) {
                return
            }
            if (packet is StatusS2CPacket) {
                packet.handle(this)
            }
        } catch (exception: Throwable) {
            Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.WARN) { exception }
        }
    }

    override fun registerEvent(method: EventInvoker) {
        if (method.eventType.isAssignableFrom(ServerListStatusArriveEvent::class.java) && wasConnected) {
            // ping done
            method(ServerListStatusArriveEvent(this, this.lastPing))
        } else if (method.eventType.isAssignableFrom(ServerListPongEvent::class.java) && wasConnected && this.pong != null) {
            method(this.pong!!)
        } else {
            super.registerEvent(method)
        }
    }
}
