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

package de.bixilon.minosoft.protocol.network.connection.status

import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.modding.event.events.status.ServerStatusReceiveEvent
import de.bixilon.minosoft.modding.event.events.status.StatusConnectionErrorEvent
import de.bixilon.minosoft.modding.event.events.status.StatusConnectionUpdateEvent
import de.bixilon.minosoft.modding.event.events.status.StatusPongReceiveEvent
import de.bixilon.minosoft.modding.event.invoker.EventInstantFireable
import de.bixilon.minosoft.modding.event.invoker.EventInvoker
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.packets.c2s.handshaking.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusRequestC2SP
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.protocol.protocol.PacketTypes
import de.bixilon.minosoft.protocol.protocol.PingQuery
import de.bixilon.minosoft.protocol.protocol.Protocol
import de.bixilon.minosoft.protocol.status.ServerStatus
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.ServerAddress
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool

class StatusConnection(
    val address: String,
) : Connection() {
    var pingStatus = StatusConnectionStatuses.WAITING
        set(value) {
            field = value
            fireEvent(StatusConnectionUpdateEvent(this, EventInitiators.UNKNOWN, value))
        }
    var lastServerStatus: ServerStatus? = null
    var pingQuery: PingQuery? = null
    var lastPongEvent: StatusPongReceiveEvent? = null

    lateinit var realAddress: ServerAddress
        private set
    private var addresses: List<ServerAddress>? = null

    var serverVersion: Version? = null


    override var error: Throwable? = super.error
        set(value) {
            field = value
            value?.let {
                fireEvent(StatusConnectionErrorEvent(this, EventInitiators.UNKNOWN, it))
                pingStatus = StatusConnectionStatuses.ERROR
            }
        }


    fun resolve() {
        pingStatus = StatusConnectionStatuses.RESOLVING
        error = null

        var addresses = this.addresses
        if (addresses == null) {
            addresses = DNSUtil.resolveServerAddress(address)
            realAddress = addresses.first()
            this.addresses = addresses
        }
    }

    fun ping() {
        DefaultThreadPool += execute@{
            try {
                resolve()
            } catch (exception: Exception) {
                Log.log(LogMessageType.NETWORK_RESOLVING) { "Can not resolve $realAddress" }
                error = exception
                disconnect()
                return@execute
            }

            Log.log(LogMessageType.NETWORK_RESOLVING) { "Trying to ping $realAddress (from $address)" }

            pingStatus = StatusConnectionStatuses.ESTABLISHING
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
                    pingStatus = StatusConnectionStatuses.HANDSHAKING
                    network.sendPacket(HandshakeC2SP(realAddress, ConnectionStates.STATUS, Versions.AUTOMATIC_VERSION.protocolId))
                    connectionState = ConnectionStates.STATUS
                }
                ConnectionStates.STATUS -> {
                    pingStatus = StatusConnectionStatuses.QUERYING_STATUS
                    network.sendPacket(StatusRequestC2SP())
                }
                ConnectionStates.DISCONNECTED -> {
                    if (previousConnectionState.connected) {
                        wasConnected = true
                        return
                    }
                    if (addresses == null || error != null) {
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
                        error = Exception("Tried all hostnames")
                    }
                }
                else -> {
                }
            }
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

    override fun registerEvent(invoker: EventInvoker) {
        if (invoker is EventInstantFireable && !invoker.instantFire) {
            return super.registerEvent(invoker)
        }

        if (!invoker.eventType.isAssignableFrom(ServerStatusReceiveEvent::class.java) && !invoker.eventType.isAssignableFrom(StatusConnectionErrorEvent::class.java) && !invoker.eventType.isAssignableFrom(StatusConnectionUpdateEvent::class.java) && !invoker.eventType.isAssignableFrom(StatusPongReceiveEvent::class.java)) {
            return super.registerEvent(invoker)
        }


        when {
            invoker.eventType.isAssignableFrom(StatusConnectionErrorEvent::class.java) -> {
                error?.let { invoker.invoke(StatusConnectionErrorEvent(this, EventInitiators.UNKNOWN, it)) } ?: super.registerEvent(invoker)
            }
            invoker.eventType.isAssignableFrom(ServerStatusReceiveEvent::class.java) -> {
                lastServerStatus?.let { invoker.invoke(ServerStatusReceiveEvent(this, EventInitiators.UNKNOWN, it)) } ?: super.registerEvent(invoker)
            }
            invoker.eventType.isAssignableFrom(StatusPongReceiveEvent::class.java) -> {
                lastPongEvent?.let { invoker.invoke(it) } ?: super.registerEvent(invoker)
            }
            invoker.eventType.isAssignableFrom(StatusConnectionUpdateEvent::class.java) -> {
                super.registerEvent(invoker)
                invoker.invoke(StatusConnectionUpdateEvent(this, EventInitiators.UNKNOWN, pingStatus))
            }
            else -> TODO()
        }
    }
}
