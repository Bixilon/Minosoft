/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.connection

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.mappings.versions.Version
import de.bixilon.minosoft.data.mappings.versions.Versions
import de.bixilon.minosoft.modding.event.EventInvoker
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.modding.event.events.ServerListPongEvent
import de.bixilon.minosoft.modding.event.events.ServerListStatusArriveEvent
import de.bixilon.minosoft.protocol.packets.clientbound.ClientboundPacket
import de.bixilon.minosoft.protocol.packets.clientbound.StatusClientboundPacket
import de.bixilon.minosoft.protocol.packets.serverbound.handshaking.PacketHandshake
import de.bixilon.minosoft.protocol.packets.serverbound.status.PacketStatusRequest
import de.bixilon.minosoft.protocol.ping.ServerListPing
import de.bixilon.minosoft.protocol.protocol.ConnectionPing
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.protocol.protocol.PacketTypes
import de.bixilon.minosoft.protocol.protocol.Protocol
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.ServerAddress
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import java.util.*

class StatusConnection(
    val address: String,
) : Connection() {
    var lastPing: ServerListPing? = null
    var connectionStatusPing: ConnectionPing? = null
    var pong: ServerListPongEvent? = null

    lateinit var realAddress: ServerAddress
        private set
    private var addresses: LinkedList<ServerAddress>? = null

    var serverVersion: Version? = null


    fun resolve() {
        lastException = null

        if (this.addresses == null) {
            this.addresses = DNSUtil.getServerAddresses(address)
        }
        this.realAddress = this.addresses!!.first
    }

    fun ping() {
        Minosoft.THREAD_POOL.execute {
            try {
                resolve()
            } catch (exception: Exception) {
                Log.info("Can not resolve $realAddress")
                lastException = exception
                connectionState = ConnectionStates.FAILED_NO_RETRY
                return@execute
            }

            Log.info("Trying to ping $realAddress (from $address)")

            network.connect(realAddress)
        }
    }

    private var _connectionState = ConnectionStates.DISCONNECTED

    override var connectionState: ConnectionStates
        get() = _connectionState
        set(value) {
            val previousConnectionState = connectionState
            _connectionState = value
            // handle callbacks
            fireEvent(ConnectionStateChangeEvent(this, previousConnectionState, _connectionState))
            when (value) {
                ConnectionStates.HANDSHAKING -> {
                    network.sendPacket(PacketHandshake(realAddress, ConnectionStates.STATUS, Versions.AUTOMATIC_VERSION.protocolId))
                    connectionState = ConnectionStates.STATUS
                }
                ConnectionStates.STATUS -> {
                    network.sendPacket(PacketStatusRequest())
                }
                ConnectionStates.FAILED -> {
                    if (addresses == null) {
                        handlePingCallbacks(null)
                        return
                    }

                    val nextIndex = addresses!!.indexOf(realAddress) + 1
                    if (addresses!!.size > nextIndex) {
                        val nextAddress = addresses!![nextIndex]
                        Log.warn(String.format("Could not connect to %s, trying next hostname: %s", address, nextAddress))
                        realAddress = nextAddress
                        ping()
                    } else {
                        // no connection and no servers available anymore... sorry, but you can not play today :(
                        handlePingCallbacks(null)
                    }
                }
                ConnectionStates.FAILED_NO_RETRY -> {
                    handlePingCallbacks(null)
                }
                else -> {
                }
            }
        }

    fun handlePingCallbacks(ping: ServerListPing?) {
        lastPing = ping
        fireEvent(ServerListStatusArriveEvent(this, ping))
    }

    override fun getPacketId(packetType: PacketTypes.Serverbound): Int {
        return Protocol.getPacketId(packetType)!!
    }

    override fun getPacketById(packetId: Int): PacketTypes.Clientbound {
        return Protocol.getPacketById(connectionState, packetId)!!
    }

    override fun handlePacket(packet: ClientboundPacket) {
        try {
            if (Log.getLevel().ordinal >= LogLevels.PROTOCOL.ordinal) {
                packet.log()
            }
            val event = PacketReceiveEvent(this, packet)
            if (fireEvent(event)) {
                return
            }
            if (packet is StatusClientboundPacket) {
                packet.handle(this)
            }
        } catch (exception: Throwable) {
            Log.printException(exception, LogLevels.PROTOCOL)
        }
    }

    override fun registerEvent(method: EventInvoker) {
        if (method.eventType.isAssignableFrom(ServerListStatusArriveEvent::class.java) && wasPingDone()) {
            // ping done
            method.invoke(ServerListStatusArriveEvent(this, this.lastPing))
        } else if (method.eventType.isAssignableFrom(ServerListPongEvent::class.java) && wasPingDone() && this.pong != null) {
            method.invoke(this.pong)
        } else {
            super.registerEvent(method)
        }
    }


    private fun wasPingDone(): Boolean {
        return connectionState == ConnectionStates.FAILED || connectionState == ConnectionStates.FAILED_NO_RETRY || lastPing != null
    }
}
