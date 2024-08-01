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
import de.bixilon.minosoft.protocol.AddressResolver
import de.bixilon.minosoft.protocol.connection.NetworkConnection
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.packets.c2s.handshake.HandshakeC2SP
import de.bixilon.minosoft.protocol.packets.c2s.status.StatusRequestC2SP
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.status.ServerStatus
import de.bixilon.minosoft.protocol.status.StatusPing
import de.bixilon.minosoft.protocol.status.StatusPong
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType

class StatusSession(
    var hostname: String,
    var forcedVersion: Version? = null,
) : Session() {
    var connection: NetworkConnection? = null
    private var resolver: AddressResolver? = null

    var status: ServerStatus? by observed(null)
    var ping: StatusPing? by observed(null)
    var pong: StatusPong? by observed(null)

    var serverVersion: Version? = null

    var state by observed(StatusSessionStates.WAITING)

    val timeout = TimeoutHandler(this)

    init {
        this::error.observe(this) {
            if (it == null) return@observe
            terminate()
            status = null
            ping = null
            pong = null
            serverVersion = null
            state = StatusSessionStates.ERROR
        }
        GlobalEventMaster.fire(StatusSessionCreateEvent(this))

        this::error.observe(this) {
            if (it == null) return@observe
            tryNext()
        }
    }

    private fun NetworkConnection.register() {
        this::state.observe(this) {
            if (this@StatusSession.connection != this) return@observe
            when (it) {
                ProtocolStates.HANDSHAKE -> {
                    // send handshake
                    send(HandshakeC2SP(hostname, this.address.port, HandshakeC2SP.Actions.STATUS, forcedVersion?.protocolId ?: Versions.AUTOMATIC.protocolId))
                    state = ProtocolStates.STATUS
                }

                ProtocolStates.STATUS -> {
                    this@StatusSession.state = StatusSessionStates.QUERYING_STATUS
                    send(StatusRequestC2SP())
                }

                null -> Unit // done

                else -> throw IllegalStateException("Illegal status state: $it")
            }
        }
    }

    override fun terminate() {
        timeout.cancel()
        this.connection?.disconnect()
        this.connection = null
        state = StatusSessionStates.WAITING
    }

    fun reset() {
        status = null
        ping = null
        pong = null
        serverVersion = null
        error = null
    }

    private fun tryNext(): Boolean {
        val network = resolver!!.tryNext() ?: return false
        this.connection = network

        if (state == StatusSessionStates.ESTABLISHING) {
            error("Already connecting!")
        }

        timeout.register()
        Log.log(LogMessageType.NETWORK) { "Pinging ${network.address} (from ${this.hostname})" }

        state = StatusSessionStates.ESTABLISHING
        network.register()
        network.connect(this)

        return true
    }

    fun ping() {
        if (state == StatusSessionStates.RESOLVING || state == StatusSessionStates.ESTABLISHING) {
            error("Already connecting!")
        }
        terminate()
        reset()

        state = StatusSessionStates.RESOLVING
        try {
            val resolver = AddressResolver(hostname)
            this.resolver = resolver
        } catch (error: Exception) {
            Log.log(LogMessageType.NETWORK) { "Can not resolve ${this.hostname}" }
            return
        }
        tryNext()
    }
}
