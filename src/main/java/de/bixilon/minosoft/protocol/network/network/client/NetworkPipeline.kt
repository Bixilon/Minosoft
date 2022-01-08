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

import de.bixilon.minosoft.protocol.network.network.client.pipeline.PacketHandler
import de.bixilon.minosoft.protocol.network.network.client.pipeline.encoding.PacketDecoder
import de.bixilon.minosoft.protocol.network.network.client.pipeline.encoding.PacketEncoder
import de.bixilon.minosoft.protocol.network.network.client.pipeline.length.LengthDecoder
import de.bixilon.minosoft.protocol.network.network.client.pipeline.length.LengthEncoder
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler

class NetworkPipeline(private val client: NettyClient) : ChannelInitializer<SocketChannel>() {
    private val maxLength = client.connection.version?.maxPacketLength ?: ProtocolDefinition.STATUS_PROTOCOL_PACKET_MAX_SIZE

    override fun initChannel(channel: SocketChannel) {
        val pipeline = channel.pipeline()

        pipeline.addLast("timeout", ReadTimeoutHandler(ProtocolDefinition.SOCKET_TIMEOUT / 1000))


        pipeline.addLast(LengthDecoder.NAME, LengthDecoder(maxLength))
        pipeline.addLast(PacketDecoder.NAME, PacketDecoder(client))
        pipeline.addLast(PacketHandler.NAME, PacketHandler(client.connection))

        pipeline.addLast(LengthEncoder.NAME, LengthEncoder(maxLength))
        pipeline.addLast(PacketEncoder.NAME, PacketEncoder(client))
    }

    override fun channelActive(context: ChannelHandlerContext) {
        super.channelActive(context)
        println("Channel active: $context")
    }

    override fun exceptionCaught(channel: ChannelHandlerContext, cause: Throwable) {
        cause.printStackTrace()

        channel.close()
    }
}
