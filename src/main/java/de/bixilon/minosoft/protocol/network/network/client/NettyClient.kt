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

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.network.client.pipeline.compression.PacketDeflater
import de.bixilon.minosoft.protocol.network.network.client.pipeline.compression.PacketInflater
import de.bixilon.minosoft.protocol.network.network.client.pipeline.encryption.PacketDecryptor
import de.bixilon.minosoft.protocol.network.network.client.pipeline.encryption.PacketEncryptor
import de.bixilon.minosoft.protocol.network.network.client.pipeline.length.LengthDecoder
import de.bixilon.minosoft.protocol.network.network.client.pipeline.length.LengthEncoder
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.util.ServerAddress
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import javax.crypto.Cipher


class NettyClient(
    val connection: Connection,
) : SimpleChannelInboundHandler<Any>() {
    var connected by watched(false)
        private set
    var state by watched(ProtocolStates.HANDSHAKING)
    var compressionThreshold = -1
        set(value) {
            field = value
            val channel = channel ?: return
            if (value < 0) {
                // disable
                channel.pipeline().remove(PacketInflater.NAME)
                channel.pipeline().remove(PacketDeflater.NAME)
            } else {
                // enable or update
                val deflater = channel.pipeline()[PacketDeflater.NAME]?.nullCast<PacketDeflater>()
                if (deflater == null) {
                    channel.pipeline().addAfter(LengthDecoder.NAME, PacketDeflater.NAME, PacketDeflater())
                }
                val inflater = channel.pipeline()[PacketInflater.NAME]?.nullCast<PacketInflater>()
                if (inflater != null) {
                    inflater.threshold = value
                } else {
                    channel.pipeline().addAfter(LengthEncoder.NAME, PacketInflater.NAME, PacketInflater(value))
                }
            }
        }
    var encrypted: Boolean = false
        private set
    private var channel: Channel? = null

    fun connect(address: ServerAddress, epoll: Boolean) {
        val threadPool: EventLoopGroup
        val channelClass: Class<out Channel>
        if (Epoll.isAvailable() && epoll) {
            threadPool = EPOLL_THREAD_POOL
            channelClass = EpollSocketChannel::class.java
        } else {
            threadPool = NIO_THREAD_POOL
            channelClass = NioSocketChannel::class.java
        }
        val bootstrap = Bootstrap()
            .group(threadPool)
            .channel(channelClass)
            .handler(NetworkPipeline(this))

        val future: ChannelFuture = bootstrap.connect(address.hostname, address.port).sync()
    }

    fun setupEncryption(encrypt: Cipher, decrypt: Cipher) {
        if (encrypted) {
            throw IllegalStateException("Already encrypted!")
        }
        val channel = channel ?: throw IllegalStateException("No channel!")
        channel.pipeline().addBefore(LengthEncoder.NAME, PacketEncryptor.NAME, PacketEncryptor(encrypt))
        channel.pipeline().addBefore(LengthDecoder.NAME, PacketDecryptor.NAME, PacketDecryptor(decrypt))
        encrypted = true
    }

    fun disconnect() {
        channel?.close()
        connected = false
    }

    fun pauseSending(pause: Boolean) {}
    fun pauseReceiving(pause: Boolean) {}

    fun send(packet: C2SPacket) {
        if (!connected) {
            throw IllegalStateException("Can not send packet when not connected!")
        }
        val channel = this.channel
        if (channel == null || !channel.isActive) {
            throw IllegalStateException("Channel not null or not active!")
        }

        packet.log((connection.nullCast<PlayConnection>()?.profiles?.other ?: OtherProfileManager.selected).log.reducedProtocolLog)
        channel.writeAndFlush(packet)
    }

    override fun channelRead0(context: ChannelHandlerContext?, message: Any?) {
    }

    override fun channelActive(context: ChannelHandlerContext) {
        try {
            context.channel().config().setOption(ChannelOption.TCP_NODELAY, true)
        } catch (_: Throwable) {
        }
        this.channel = context.channel()
        connected = true
    }

    override fun channelInactive(context: ChannelHandlerContext) {
        connected = false
    }

    companion object {
        private val NIO_THREAD_POOL by lazy { NioEventLoopGroup(NamedThreadFactory("Nio#%d")) }
        private val EPOLL_THREAD_POOL by lazy { EpollEventLoopGroup(NamedThreadFactory("Epoll#%d")) }
    }
}
