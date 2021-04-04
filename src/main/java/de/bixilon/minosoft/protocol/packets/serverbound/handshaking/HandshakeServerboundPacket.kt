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
package de.bixilon.minosoft.protocol.packets.serverbound.handshaking

import de.bixilon.minosoft.protocol.packets.serverbound.AllServerboundPacket
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer
import de.bixilon.minosoft.util.ServerAddress
import de.bixilon.minosoft.util.logging.Log

class HandshakeServerboundPacket : AllServerboundPacket {
    private val address: ServerAddress
    private val nextState: ConnectionStates
    private val protocolId: Int

    constructor(address: ServerAddress, nextState: ConnectionStates, protocolId: Int) {
        this.address = address
        this.nextState = nextState
        this.protocolId = protocolId
    }

    constructor(address: ServerAddress, protocolId: Int) {
        this.address = address
        this.protocolId = protocolId
        nextState = ConnectionStates.STATUS
    }

    override fun write(buffer: OutByteBuffer) {
        buffer.writeVarInt(protocolId) // get best protocol version
        buffer.writeString(address.hostname)
        buffer.writeShort(address.port.toShort())
        buffer.writeVarInt(nextState.ordinal)
    }

    override fun log() {
        Log.protocol("[OUT] Sending handshake packet (address=$address)")
    }
}
