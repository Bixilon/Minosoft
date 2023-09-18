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

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.NetworkException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketReadException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.ciritical.UnknownPacketIdException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.implementation.PacketNotImplementedException
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.QueuedS2CP
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.protocol.DefaultPacketMapping
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import de.bixilon.minosoft.protocol.versions.Version
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToMessageDecoder
import java.lang.reflect.InvocationTargetException

class PacketDecoder(
    private val client: NettyClient,
) : MessageToMessageDecoder<ByteArray>() {
    private val version: Version? = client.connection.version

    override fun decode(context: ChannelHandlerContext, array: ByteArray, out: MutableList<Any>) {
        val buffer = InByteBuffer(array)
        val packetId = buffer.readVarInt()
        val data = buffer.readRest()

        val state = client.state


        val type = version?.s2c?.get(state, packetId) ?: DefaultPacketMapping.S2C_PACKET_MAPPING[state, packetId] ?: throw UnknownPacketIdException(packetId, state, version)

        if (type.extra != null && type.extra.skip(client.connection)) {
            return
        }

        val packet = try {
            type.create(data, client.connection).unsafeCast<S2CPacket>()
        } catch (error: PacketNotImplementedException) {
            error.printStackTrace()
            return
        } catch (exception: NetworkException) {
            type.extra?.onError(exception, client.connection)
            throw exception
        } catch (error: Throwable) {
            var realError = error
            if (error is InvocationTargetException) {
                error.cause?.let { realError = it }
            }
            type.extra?.onError(realError, client.connection)
            throw PacketReadException(realError)
        }

        out += QueuedS2CP(type, packet)
    }


    companion object {
        const val NAME = "packet_decoder"
    }
}
