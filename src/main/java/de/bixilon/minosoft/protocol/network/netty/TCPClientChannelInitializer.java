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

import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;

import java.util.concurrent.TimeUnit;

public class TCPClientChannelInitializer extends ChannelInitializer<NioSocketChannel> {
    final Connection connection;
    final NettyNetwork nettyNetwork;

    public TCPClientChannelInitializer(Connection connection, NettyNetwork nettyNetwork) {
        this.connection = connection;
        this.nettyNetwork = nettyNetwork;
    }

    @Override
    protected void initChannel(NioSocketChannel socketChannel) {
        nettyNetwork.setNioChannel(socketChannel);
        socketChannel.pipeline().addLast("timeout", new ReadTimeoutHandler(ProtocolDefinition.SOCKET_TIMEOUT, TimeUnit.MILLISECONDS));
        socketChannel.pipeline().addLast("decoder", new PacketDecoder(connection, nettyNetwork));
        socketChannel.pipeline().addLast("encoder", new PacketEncoder(connection, nettyNetwork));
        socketChannel.pipeline().addLast(new PacketReceiver(connection));
    }
}
