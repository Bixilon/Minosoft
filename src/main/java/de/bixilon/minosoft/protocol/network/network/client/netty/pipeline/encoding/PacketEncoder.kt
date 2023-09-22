/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.encoding

import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketNotAvailableException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.WrongConnectionException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.unknown.UnknownPacketException
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.registry.DefaultPackets
import de.bixilon.minosoft.protocol.packets.registry.PacketMapping
import de.bixilon.minosoft.protocol.packets.registry.PacketType
import de.bixilon.minosoft.protocol.protocol.DefaultPacketMapping
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.versions.Version
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

class PacketEncoder(
    private val client: NettyClient,
) : MessageToMessageEncoder<C2SPacket>() {
    private val version: Version? = client.connection.version

    private fun PlayC2SPacket.write(): OutByteBuffer {
        if (client.connection !is PlayConnection) throw WrongConnectionException(PlayConnection::class.java, client.connection::class.java)
        val buffer = PlayOutByteBuffer(client.connection)
        write(buffer)

        return buffer
    }

    private fun C2SPacket.write(): OutByteBuffer {
        if (this is PlayC2SPacket) {
            return this.write()
        }
        val buffer = OutByteBuffer()
        write(buffer)

        return buffer
    }

    @JvmName("getVersionPacketId")
    private fun Version?.getPacketId(state: ProtocolStates, type: PacketType): Int {
        if (this == null) return PacketMapping.INVALID_ID
        return c2s[state, type]
    }

    private fun getPacketId(version: Version?, state: ProtocolStates, type: PacketType): Int {
        var id = version.getPacketId(state, type)
        if (id != PacketMapping.INVALID_ID) {
            return id
        }

        id = DefaultPacketMapping.C2S_PACKET_MAPPING[state, type]
        if (id != PacketMapping.INVALID_ID) {
            return id
        }

        throw PacketNotAvailableException(type, state, version)
    }

    override fun encode(context: ChannelHandlerContext, packet: C2SPacket, out: MutableList<Any>) {
        val state = client.state

        val type = DefaultPackets.C2S[state]?.get(packet::class) ?: throw UnknownPacketException(packet::class.java)
        val id = getPacketId(version, state, type)

        val packetData = packet.write()

        val data = OutByteBuffer()
        data.writeVarInt(id)
        data.writeBareByteArray(packetData.toArray())

        out += data.toArray()
    }

    companion object {
        const val NAME = "packet_encoder"
    }
}
