/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network

import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.observer.DataObserver
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.protocol.ServerConnection
import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.network.network.client.ClientNetwork
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.session.Session
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class NetworkConnection(
    val address: ServerAddress,
    val native: Boolean,
) : ServerConnection {
    var client: ClientNetwork? = null
        private set
    override val identifier = address.toString()
    override var active by observed(false)
        private set
    var state: ProtocolStates? by DataObserver(null, RWLock.rwlock())

    init {
        this::state.observe(this) { active = it != null }
    }

    override fun connect(session: Session) {
        Log.log(LogMessageType.NETWORK, level = LogLevels.INFO) { "Connecting to server: $address" }
        if (client != null) throw IllegalStateException("Already connected???")
        val netty = NettyClient(this, session)
        this.client = netty
        netty.connect()
    }

    override fun disconnect() {
        client?.disconnect()
    }

    override fun detach() {
        client?.detach()
    }

    override fun send(packet: C2SPacket) {
        client?.send(packet)
    }
}
