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
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.watcher.DataWatcher.Companion.watched
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport
import de.bixilon.minosoft.gui.eros.dialog.ErosErrorReport.Companion.report
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.network.network.client.exceptions.NetworkException
import de.bixilon.minosoft.protocol.network.network.client.exceptions.ciritical.CriticalNetworkException
import de.bixilon.minosoft.protocol.network.network.client.pipeline.compression.PacketDeflater
import de.bixilon.minosoft.protocol.network.network.client.pipeline.compression.PacketInflater
import de.bixilon.minosoft.protocol.network.network.client.pipeline.encryption.PacketDecryptor
import de.bixilon.minosoft.protocol.network.network.client.pipeline.encryption.PacketEncryptor
import de.bixilon.minosoft.protocol.network.network.client.pipeline.length.LengthDecoder
import de.bixilon.minosoft.protocol.network.network.client.pipeline.length.LengthEncoder
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.ServerAddress
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import javax.crypto.Cipher


@ChannelHandler.Sharable
class NettyClient(
    val connection: Connection,
) : SimpleChannelInboundHandler<Any>() {
    private var errorReported = false
    private val reportErrors: Boolean get() = connection is PlayConnection && !errorReported  // dont report errors in status
    var connected by watched(false)
        private set
    var state by watched(ProtocolStates.HANDSHAKING)
    var compressionThreshold = -1
        set(value) {
            field = value
            val channel = channel ?: return
            val pipeline = channel.pipeline()
            if (value < 0) {
                // disable
                if (pipeline.get(PacketInflater.NAME) != null) {
                    channel.pipeline().remove(PacketInflater.NAME)
                }
                if (pipeline.get(PacketDeflater.NAME) != null) {
                    channel.pipeline().remove(PacketDeflater.NAME)
                }
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
    private val packetQueue: MutableList<C2SPacket> = mutableListOf() // Used for pause sending
    private var sendingPaused = false

    fun connect(address: ServerAddress, epoll: Boolean) {
        state = ProtocolStates.HANDSHAKING
        val threadPool: EventLoopGroup
        val channelClass: Class<out Channel>
        if (epoll && Epoll.isAvailable()) {
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

        val future = bootstrap.connect(address.hostname, address.port)
        future.addListener {
            if (!it.isSuccess) {
                handleError(it.cause())
            }
        }
    }

    fun setupEncryption(encrypt: Cipher, decrypt: Cipher) {
        val channel = requireChannel()
        if (encrypted) {
            throw IllegalStateException("Already encrypted!")
        }
        channel.pipeline().addBefore(LengthEncoder.NAME, PacketEncryptor.NAME, PacketEncryptor(encrypt))
        channel.pipeline().addBefore(LengthDecoder.NAME, PacketDecryptor.NAME, PacketDecryptor(decrypt))
        encrypted = true
    }

    fun disconnect() {
        channel?.close()
        encrypted = false
        channel = null
        compressionThreshold = -1
        connected = false
    }

    fun pauseSending(pause: Boolean) {
        this.sendingPaused = pause
        if (!sendingPaused) {
            DefaultThreadPool += {
                for (packet in packetQueue) {
                    send(packet)
                }
            }
        }
    }

    fun pauseReceiving(pause: Boolean) {
        val channel = requireChannel()
        channel.config()?.isAutoRead = !pause
    }

    fun send(packet: C2SPacket) {
        val channel = requireChannel()
        if (sendingPaused) {
            packetQueue += packet
            return
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

    fun handleError(error: Throwable) {
        var cause = error
        if (cause is DecoderException) {
            cause = error.cause ?: cause
        } else if (cause is EncoderException) {
            cause = error.cause ?: cause
        }
        if (RunConfiguration.DISABLE_EROS || connection !is StatusConnection) {
            Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.WARN) { cause }
        }
        if (cause !is NetworkException || cause is CriticalNetworkException || state == ProtocolStates.LOGIN) {
            connection.error = cause
            if (reportErrors && !ErosCrashReport.alreadyCrashed) {
                cause.report()
                errorReported = true
            }
            disconnect()
            return
        }
    }

    private fun requireChannel(): Channel {
        val channel = this.channel
        if (!connected || channel == null) {
            throw IllegalStateException("Not connected!")
        }
        return channel
    }

    companion object {
        private val NIO_THREAD_POOL by lazy { NioEventLoopGroup(NamedThreadFactory("Nio#%d")) }
        private val EPOLL_THREAD_POOL by lazy { EpollEventLoopGroup(NamedThreadFactory("Epoll#%d")) }
    }
}
