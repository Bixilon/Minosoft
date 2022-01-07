/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.network.client

import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.util.ServerAddress

class NettyClient(
    private val connection: Connection,
) {
    val connected by watched(false)
    var state by watched(ProtocolStates.HANDSHAKING)
    var compressionThreshold = -1

    fun connect(address: ServerAddress) {
    }

    fun disconnect() {

    }

    fun pauseSending(pause: Boolean) {}
    fun pauseReceiving(pause: Boolean) {}

    fun send(packet: C2SPacket) {}
}
