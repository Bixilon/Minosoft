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

package de.bixilon.minosoft.protocol.network.network.client.pipeline.encoding

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.protocol.network.network.client.NettyClient
import de.bixilon.minosoft.protocol.packets.c2s.AllC2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer
import de.bixilon.minosoft.protocol.protocol.PacketTypes
import de.bixilon.minosoft.protocol.protocol.PlayOutByteBuffer
import de.bixilon.minosoft.protocol.protocol.Protocol
import de.bixilon.minosoft.util.KUtil.index
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageEncoder

class PacketEncoder(
    private val client: NettyClient,
) : MessageToMessageEncoder<C2SPacket>() {
    private val version: Version? = client.connection.version


    override fun encode(context: ChannelHandlerContext, packet: C2SPacket, out: MutableList<Any>) {
        val packetData: OutByteBuffer
        when (packet) {
            is AllC2SPacket -> {
                packetData = OutByteBuffer()
                packet.write(packetData)
            }
            is PlayC2SPacket -> {
                packetData = PlayOutByteBuffer(client.connection.nullCast() ?: throw IllegalStateException("Trying to send a play packet in non play connection"))
                packet.write(packetData)
            }
            else -> throw IllegalArgumentException("Unknown packet type $packet")
        }
        val state = client.state
        val packetType = PacketTypes.C2S.getPacketType(packet::class.java)
        val packetId = version?.c2sPackets?.get(state)?.index(packetType) ?: Protocol.getPacketId(packetType) ?: throw IllegalArgumentException("Can not get packet id for $packetType")

        val data = OutByteBuffer()
        data.writeVarInt(packetId)
        data.writeUnprefixedByteArray(packetData.toArray())

        out += data.toArray()
    }

    companion object {
        const val NAME = "packet_encoder"
    }
}
