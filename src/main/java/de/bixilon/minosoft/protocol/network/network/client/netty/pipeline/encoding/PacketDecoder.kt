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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.netty.NetworkAllocator
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.NetworkException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketReadException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.type.PacketNotImplementedException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.type.UnknownPacketIdException
import de.bixilon.minosoft.protocol.network.network.client.netty.packet.receiver.QueuedS2CP
import de.bixilon.kutil.buffer.bytes.ArbitraryByteBuffer
import de.bixilon.minosoft.protocol.packets.registry.PacketType
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.protocol.DefaultPacketMapping
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import de.bixilon.minosoft.protocol.versions.Version
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import java.lang.reflect.InvocationTargetException

class PacketDecoder(
    private val client: NettyClient,
) : MessageToMessageDecoder<ArbitraryByteBuffer>() {
    private val version: Version? = client.session.version

    override fun decode(context: ChannelHandlerContext, array: ArbitraryByteBuffer, out: MutableList<Any>) {
        out += decode(array) ?: return
    }

    fun decode(data: ArbitraryByteBuffer): QueuedS2CP<S2CPacket>? {
        val buffer = InByteBuffer(data)
        val packetId = buffer.readVarInt()

        val state = client.connection.state ?: return null
        val type = version?.s2c?.get(state, packetId) ?: DefaultPacketMapping.S2C_PACKET_MAPPING[state, packetId] ?: throw UnknownPacketIdException(packetId, state, version)

        val length = data.size - (buffer.offset - data.offset)
        try {
            return decode(type, ArbitraryByteBuffer(buffer.offset, length, data.buffer))
        } finally {
            NetworkAllocator.free(data.buffer)
        }
    }

    private fun decode(type: PacketType, buffer: ArbitraryByteBuffer): QueuedS2CP<S2CPacket>? {
        if (type.extra != null && type.extra.skip(client.session)) {
            return null
        }

        val packet = try {
            type.create(buffer, client.session).unsafeCast<S2CPacket>()
        } catch (error: PacketNotImplementedException) {
            error.printStackTrace()
            return null
        } catch (exception: NetworkException) {
            type.extra?.onError(exception, client.session)
            throw exception
        } catch (error: Throwable) {
            var real = error
            if (error is InvocationTargetException) {
                error.cause?.let { real = it }
            }
            type.extra?.onError(real, client.session)
            throw PacketReadException(real)
        }

        return QueuedS2CP(type, packet)
    }


    companion object {
        const val NAME = "packet_decoder"
    }
}
