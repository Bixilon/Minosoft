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

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketNotAvailableException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.WrongConnectionException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.WrongProtocolStateException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.unknown.UnknownPacketException
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.packets.factory.PacketTypeRegistry
import de.bixilon.minosoft.protocol.protocol.Protocol
import de.bixilon.minosoft.protocol.protocol.buffers.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.versions.Version
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

class PacketEncoder(
    private val client: NettyClient,
) : MessageToMessageEncoder<C2SPacket>() {
    private val version: Version? = client.connection.version

    override fun encode(context: ChannelHandlerContext, packet: C2SPacket, out: MutableList<Any>) {
        val state = client.state
        val packetData: OutByteBuffer
        when (packet) {
            is PlayC2SPacket -> {
                packetData = PlayOutByteBuffer(client.connection.nullCast() ?: throw WrongConnectionException(PlayConnection::class.java, client.connection::class.java))
                packet.write(packetData)
            }
            else -> {
                packetData = OutByteBuffer()
                packet.write(packetData)
            }
        }
        val packetType = PacketTypeRegistry.getC2S(packet::class.java) ?: throw UnknownPacketException(packet::class.java)
        if (packetType.state != state) {
            throw WrongProtocolStateException(packetType.state, state)
        }
        val packetId = version?.c2sPackets?.get(state)?.get(packetType) ?: Protocol.C2S_PACKET_MAPPING[state]?.get(packetType) ?: throw PacketNotAvailableException(packetType, state, version)

        val data = OutByteBuffer()
        data.writeVarInt(packetId)
        data.writeBareByteArray(packetData.toArray())

        out += data.toArray()
    }

    companion object {
        const val NAME = "packet_encoder"
    }
}
