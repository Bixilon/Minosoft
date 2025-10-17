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

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.encoding

import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.netty.NetworkAllocator
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.WrongSessionTypeException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.type.PacketNotAvailableException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.type.PacketNotFoundException
import de.bixilon.kutil.buffer.bytes.ArbitraryByteBuffer
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
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
    private val version: Version? = client.session.version

    // TODO: tests

    private fun PlayC2SPacket.write(): OutByteBuffer {
        if (client.session !is PlaySession) throw WrongSessionTypeException(PlaySession::class.java, client.session::class.java)
        val buffer = PlayOutByteBuffer(client.session)
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

    fun encode(packet: C2SPacket): ArbitraryByteBuffer? {
        val state = client.connection.state ?: return null

        val type = DefaultPackets.C2S[state]?.get(packet::class) ?: throw PacketNotFoundException(packet::class)
        val id = getPacketId(version, state, type)

        val packetData = packet.write().toArray() // TODO: remove toArray allocation

        val idData = OutByteBuffer().apply { writeVarInt(id) }.toArray()
        val length = idData.size + packetData.size
        val temporary = NetworkAllocator.allocate(length)

        System.arraycopy(idData, 0, temporary, 0, idData.size)
        System.arraycopy(packetData, 0, temporary, idData.size, packetData.size)

        return ArbitraryByteBuffer(0, length, temporary)
    }

    override fun encode(context: ChannelHandlerContext?, packet: C2SPacket, out: MutableList<Any>) {
        out += encode(packet) ?: return
    }

    companion object {
        const val NAME = "packet_encoder"
    }
}
