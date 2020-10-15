/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network.netty;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.network.Network;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.util.ServerAddress;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import javax.crypto.SecretKey;

public class NettyNetwork implements Network {
    final Connection connection;
    NioSocketChannel nioSocketChannel;
    int compressionThreshold = -1;

    public NettyNetwork(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void connect(ServerAddress address) {
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();

        Bootstrap clientBootstrap = new Bootstrap();
        clientBootstrap.group(eventLoopGroup).channel(NioSocketChannel.class).handler(new TCPClientChannelInitializer(connection, this));

        try {
            ChannelFuture channelFuture = clientBootstrap.connect(address.getHostname(), address.getPort()).sync();
            if (channelFuture.isSuccess()) {
                connection.setConnectionState(ConnectionStates.HANDSHAKING);
            }
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            Log.info(String.format("connection failed: %s", e));
            connection.setConnectionState(ConnectionStates.FAILED);
        } finally {
            connection.setConnectionState(ConnectionStates.DISCONNECTED);
            eventLoopGroup.shutdownGracefully();
        }
    }

    @Override
    public void sendPacket(ServerboundPacket packet) {
        if (this.nioSocketChannel.eventLoop().inEventLoop()) {
            this.nioSocketChannel.writeAndFlush(packet);
            return;
        }
        this.nioSocketChannel.eventLoop().execute(() -> NettyNetwork.this.nioSocketChannel.writeAndFlush(packet));
    }

    @Override
    public void disconnect() {

    }

    @Override
    public Exception getLastException() {
        return null;
    }

    public int getCompressionThreshold() {
        return compressionThreshold;
    }

    public void setNioChannel(NioSocketChannel nioSocketChannel) {
        this.nioSocketChannel = nioSocketChannel;
    }

    public void enableEncryption(SecretKey key) {
        /*
        this.nioSocketChannel.pipeline().addBefore("decoder", "decrypt", new EncryptionHandler(key));
        this.nioSocketChannel.pipeline().addBefore("encoder", "encrypt", new DecryptionHandler(key));
        Log.debug("Encryption enabled!");
         */
        //ToDo
        Log.fatal("Encryption is not implemented in netty yet!");
        disconnect();
    }
}
