/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.session.status

import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.modding.event.events.session.status.StatusSessionCreateEvent
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.packets.c2s.handshake.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusRequestC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.status.ServerStatus
import de.bixilon.minosoft.protocol.status.StatusPing
import de.bixilon.minosoft.protocol.status.StatusPong
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType

class StatusSession(
    var address: String,
    var forcedVersion: Version? = null,
) : Session() {
    var status: ServerStatus? by observed(null)
    var ping: StatusPing? by observed(null)
    var pong: StatusPong? by observed(null)

    var realAddress: ServerAddress? = null
        private set
    private var addresses: List<ServerAddress>? = null
    private var addressIndex = 0

    var serverVersion: Version? = null

    var state by observed(StatusSessionStates.WAITING)

    val timeout = TimeoutHandler(this)


    init {
        this::error.observe(this) {
            if (it == null) return@observe
            ping = null
            status = null
            state = StatusSessionStates.ERROR
            timeout.cancel()
            network.disconnect()
        }
        network::connected.observe(this) {
            if (it) {
                state = StatusSessionStates.HANDSHAKING
                network.send(HandshakeC2SP(realAddress!!, HandshakeC2SP.Actions.STATUS, forcedVersion?.protocolId ?: Versions.AUTOMATIC.protocolId))
                network.state = ProtocolStates.STATUS
                return@observe
            }
            if (status != null) {
                return@observe
            }
            tryNextAddress()
        }

        network::state.observe(this) {
            when (it) {
                ProtocolStates.HANDSHAKE -> {}
                ProtocolStates.PLAY, ProtocolStates.LOGIN, ProtocolStates.CONFIGURATION -> throw IllegalStateException("Invalid state!")
                ProtocolStates.STATUS -> {
                    state = StatusSessionStates.QUERYING_STATUS
                    network.send(StatusRequestC2SP())
                }
            }
        }
        GlobalEventMaster.fire(StatusSessionCreateEvent(this))
    }

    private fun tryNextAddress() {
        val addresses = this.addresses ?: return
        val nextIndex = ++addressIndex
        if (addresses.size > nextIndex) {
            val nextAddress = addresses[nextIndex]
            Log.log(LogMessageType.NETWORK) { "Could not connect to $address, trying next hostname: $nextAddress" }
            realAddress = nextAddress
            ping(nextAddress)
        }
    }

    private fun resolve(): List<ServerAddress> {
        state = StatusSessionStates.RESOLVING

        var addresses = this.addresses
        if (addresses == null) {
            addresses = DNSUtil.resolveServerAddress(address)
            realAddress = addresses.first()
            this.addresses = addresses
            this.addressIndex = 0
        }
        return addresses
    }

    fun reset() {
        timeout.cancel()
        realAddress = null
        this.addresses = null
        this.addressIndex = 0
        status = null
        ping = null
        pong = null
        serverVersion = null
        error = null
        state = StatusSessionStates.WAITING
    }

    fun ping(address: ServerAddress) {
        if (state == StatusSessionStates.ESTABLISHING || network.connected) {
            error("Already connecting!")
        }
        timeout.register()
        Log.log(LogMessageType.NETWORK) { "Pinging $address (from ${this.address})" }

        state = StatusSessionStates.ESTABLISHING
        network.connect(address, false)
    }

    fun ping() {
        if (state == StatusSessionStates.RESOLVING || state == StatusSessionStates.ESTABLISHING || network.connected) {
            error("Already connecting!")
        }
        reset()
        state = StatusSessionStates.RESOLVING
        val addresses: List<ServerAddress>
        try {
            // TODO: Don't resolve if address is ip
            addresses = resolve()
        } catch (exception: Exception) {
            Log.log(LogMessageType.NETWORK) { "Can not resolve ${this.address}" }
            return
        }
        ping(addresses.first())
    }

    override fun terminate() {
        super.terminate()
        state = StatusSessionStates.WAITING
    }
}
