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

package de.bixilon.minosoft.protocol.network.network.client.pipeline

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.collections.CollectionUtil.synchronizedSetOf
import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedList
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPoolRunnable
import de.bixilon.kutil.watcher.DataWatcher.Companion.observe
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.modding.event.events.PacketReceiveEvent
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.network.network.client.NettyClient
import de.bixilon.minosoft.protocol.network.network.client.exceptions.NetworkException
import de.bixilon.minosoft.protocol.network.network.client.exceptions.PacketHandleException
import de.bixilon.minosoft.protocol.network.network.client.exceptions.WrongConnectionException
import de.bixilon.minosoft.protocol.packets.factory.S2CPacketType
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler

class ClientPacketHandler(
    private val client: NettyClient,
) : SimpleChannelInboundHandler<QueuedS2CP<*>>() {
    private val connection: Connection = client.connection
    private val handlers: MutableSet<ThreadPoolRunnable> = synchronizedSetOf()

    init {
        client::connected.observe(this) {
            if (!it) {
                for (handler in handlers.toSynchronizedList()) {
                    handler.interrupt()
                }
            }
        }
    }

    override fun channelRead0(context: ChannelHandlerContext, queued: QueuedS2CP<*>) {
        if (queued.type.threadSafe && DefaultThreadPool.queueSize <= DefaultThreadPool.threadCount) { // only handle async when thread pool not busy
            val runnable = ThreadPoolRunnable(priority = ThreadPool.Priorities.HIGH)
            runnable.runnable = Runnable { tryHandle(context, queued.type, queued.packet);handlers -= runnable }
            handlers += runnable
            DefaultThreadPool += runnable
        } else {
            tryHandle(context, queued.type, queued.packet)
        }
    }

    private fun tryHandle(context: ChannelHandlerContext, type: S2CPacketType, packet: S2CPacket) {
        if (!client.connected) {
            return
        }
        try {
            handle(packet)
        } catch (exception: NetworkException) {
            type.onError(exception, connection)
            context.fireExceptionCaught(exception)
        } catch (error: Throwable) {
            type.onError(error, connection)
            context.fireExceptionCaught(PacketHandleException(error))
        }
    }

    private fun handle(packet: S2CPacket) {
        val event = PacketReceiveEvent(connection, packet)
        if (connection.fireEvent(event)) {
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
