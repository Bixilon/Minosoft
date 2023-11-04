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

package de.bixilon.minosoft.protocol.network.network.client.netty.pipeline

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.SimplePoolRunnable
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.network.network.client.netty.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.NetworkException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketHandleException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.WrongConnectionException
import de.bixilon.minosoft.protocol.packets.registry.PacketType
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ClientPacketHandler(
    private val client: NettyClient,
) : SimpleChannelInboundHandler<QueuedS2CP<*>>() {
    private val connection: Connection = client.connection

    override fun channelRead0(context: ChannelHandlerContext, queued: QueuedS2CP<*>) {
        if (queued.type.threadSafe && (DefaultThreadPool.queueSize < DefaultThreadPool.threadCount - 1 || queued.type.lowPriority)) { // only handle async when thread pool not busy
            val runnable = SimplePoolRunnable(priority = if (queued.type.lowPriority) ThreadPool.Priorities.NORMAL else ThreadPool.Priorities.HIGH)
            runnable.runnable = Runnable { tryHandle(context, queued.type, queued.packet) }
            DefaultThreadPool += runnable
        } else {
            tryHandle(context, queued.type, queued.packet)
        }
    }

    private fun handleError(context: ChannelHandlerContext, type: PacketType, error: Throwable) {
        if (type.extra != null) {
            type.extra.onError(error, connection)
        }
        client.handleError(error)
    }

    private fun tryHandle(context: ChannelHandlerContext, type: PacketType, packet: S2CPacket) {
        if (!client.connected) {
            return
        }
        try {
            handle(packet)
        } catch (exception: NetworkException) {
            handleError(context, type, exception)
        } catch (error: Throwable) {
            handleError(context, type, PacketHandleException(error))
        }
    }

    private fun handle(packet: S2CPacket) {
        val event = PacketReceiveEvent(connection, packet)
        if (connection.events.fire(event)) {
            return
        }
        when (packet) {
            is PlayS2CPacket -> handle(packet)
            is StatusS2CPacket -> handle(packet)
            else -> throw IllegalStateException("Unknown packet type!")
        }
    }

    private fun handle(packet: PlayS2CPacket) {
        val connection = connection.nullCast<PlayConnection>() ?: throw WrongConnectionException(PlayConnection::class.java, this.connection::class.java)
        packet.log(connection.profiles.other.log.reducedProtocolLog)
        packet.check(connection)
        packet.handle(connection)
    }

    private fun handle(packet: StatusS2CPacket) {
        val connection = connection.nullCast<StatusConnection>() ?: throw WrongConnectionException(StatusConnection::class.java, this.connection::class.java)
        packet.log(OtherProfileManager.selected.log.reducedProtocolLog)
        packet.check(connection)
        packet.handle(connection)
    }

    companion object {
        const val NAME = "client_packet_handler"
    }
}
