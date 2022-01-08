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
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import javax.crypto.Cipher


class NettyClient(
    val connection: Connection,
) {
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
                val delater = channel.pipeline()[PacketDeflater.NAME]?.nullCast<PacketDeflater>()
                if (delater == null) {
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

    fun connect(address: ServerAddress) {
        // val workerGroup = NioEventLoopGroup(DefaultThreadPool.threadCount - 1, DefaultThreadPool)
        val workerGroup = NioEventLoopGroup()
        val bootstrap = Bootstrap()
            .group(workerGroup)
            .channel(NioSocketChannel::class.java)
            .handler(NetworkPipeline(this))

        val future: ChannelFuture = bootstrap.connect(address.hostname, address.port).sync()
        future.addListener {
            if (!it.isSuccess) {
                disconnect()
                return@addListener
            }
            val channel = future.channel()
            this.channel = channel
            channel.config().setOption(ChannelOption.TCP_NODELAY, true)
            connected = true
            println("Connected!")
        }
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
        // ToDo: workerGroup.shutdownGracefully()
        connected = false
    }

    fun pauseSending(pause: Boolean) {}
    fun pauseReceiving(pause: Boolean) {}

    fun send(packet: C2SPacket) {
        val channel = this.channel
        if (channel?.isActive != true) {
            throw IllegalStateException("Channel not active!")
        }

        packet.log((connection.nullCast<PlayConnection>()?.profiles?.other ?: OtherProfileManager.selected).log.reducedProtocolLog)
        channel.writeAndFlush(packet)
    }
}
