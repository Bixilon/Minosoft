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

package de.bixilon.minosoft.protocol.network.network.client.netty

import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.config.profile.profiles.other.OtherProfileManager
import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.network.connection.Connection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.network.network.client.ClientNetwork
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.NetworkException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.PacketHandleException
import de.bixilon.minosoft.protocol.network.network.client.netty.exceptions.ciritical.CriticalNetworkException
import de.bixilon.minosoft.protocol.network.network.client.netty.natives.NioNatives
import de.bixilon.minosoft.protocol.network.network.client.netty.natives.TransportNatives
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.compression.PacketDeflater
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.compression.PacketInflater
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.encryption.PacketDecryptor
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.encryption.PacketEncryptor
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.length.LengthDecoder
import de.bixilon.minosoft.protocol.network.network.client.netty.pipeline.length.LengthEncoder
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import javax.crypto.Cipher


@ChannelHandler.Sharable
class NettyClient(
    val connection: Connection,
) : SimpleChannelInboundHandler<Any>(), ClientNetwork {
    private var address: ServerAddress? = null
    override var connected by observed(false)
        private set
    override var state by observed(ProtocolStates.HANDSHAKE)
    override var compressionThreshold = -1
    override var encrypted: Boolean = false
        private set
    private var channel: Channel? = null
    private val packetQueue: MutableList<C2SPacket> = mutableListOf() // Used for pause sending
    private var sendingPaused = false
    private var detached = false
    override var receive = true
        set(value) {
            channel?.config()?.isAutoRead = value
            field = value
        }

    override fun connect(address: ServerAddress, native: Boolean) {
        this.address = address
        state = ProtocolStates.HANDSHAKE
        val natives = if (native) TransportNatives.get() else NioNatives
        val bootstrap = Bootstrap()
            .group(natives.pool)
            .channel(natives.channel)
            .handler(NetworkPipeline(this))

        val future = bootstrap.connect(address.hostname, address.port)
        future.addListener {
            if (!it.isSuccess) {
                handleError(it.cause())
            }
        }
    }

    override fun setupEncryption(encrypt: Cipher, decrypt: Cipher) {
        val channel = requireChannel()
        if (encrypted) {
            throw IllegalStateException("Already encrypted!")
        }
        channel.pipeline().addBefore(LengthEncoder.NAME, PacketEncryptor.NAME, PacketEncryptor(encrypt))
        channel.pipeline().addBefore(LengthDecoder.NAME, PacketDecryptor.NAME, PacketDecryptor(decrypt))
        encrypted = true
    }

    override fun setupCompression(threshold: Int) {
        val channel = channel ?: return
        val pipeline = channel.pipeline()
        if (threshold < 0) {
            // disable
            if (pipeline.get(PacketDeflater.NAME) != null) {
                channel.pipeline().remove(PacketDeflater.NAME)
            }
            if (pipeline.get(PacketInflater.NAME) != null) {
                channel.pipeline().remove(PacketInflater.NAME)
            }
        } else {
            // enable or update
            val inflater = channel.pipeline()[PacketInflater.NAME]?.nullCast<PacketInflater>()
            if (inflater == null) {
                channel.pipeline().addAfter(LengthDecoder.NAME, PacketInflater.NAME, PacketInflater(connection.version!!.maxPacketLength))
            }
            val deflater = channel.pipeline()[PacketDeflater.NAME]?.nullCast<PacketDeflater>()
            if (deflater != null) {
                deflater.threshold = threshold
            } else {
                channel.pipeline().addAfter(LengthEncoder.NAME, PacketDeflater.NAME, PacketDeflater(threshold))
            }
        }
    }

    override fun disconnect() {
        channel?.close()
        encrypted = false
        channel = null
        compressionThreshold = -1
        connected = false
    }

    override fun detach() {
        detached = true
        channel?.close()
    }

    override fun pauseSending(pause: Boolean) {
        this.sendingPaused = pause
        if (!sendingPaused) {
            DefaultThreadPool += {
                for (packet in packetQueue) {
                    send(packet)
                }
            }
        }
    }

    override fun send(packet: C2SPacket) {
        if (detached) return
        if (sendingPaused) {
            packetQueue += packet
            return
        }
        val channel = getChannel() ?: return

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
        context.channel().config().isAutoRead = this.receive
        this.channel = context.channel()
        connected = true
    }

    override fun channelInactive(context: ChannelHandlerContext) {
        Log.log(LogMessageType.NETWORK, LogLevels.VERBOSE) { "Connection closed ($address)" }
        if (detached) return
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
            val log = if (cause is PacketHandleException) cause.cause else cause
            Log.log(LogMessageType.NETWORK_IN, LogLevels.WARN) { log }
        }
        if (cause !is NetworkException || cause is CriticalNetworkException || state == ProtocolStates.LOGIN) {
            connection.error = cause
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

    private fun getChannel(): Channel? {
        val channel = this.channel
        if (!connected || channel == null) {
            return null
        }
        return channel
    }
}
