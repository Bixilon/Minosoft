/*
 * Minosoft
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
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class PacketReceiver extends SimpleChannelInboundHandler<ClientboundPacket> {
    final Connection connection;

    public PacketReceiver(Connection connection) {
        this.connection = connection;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ClientboundPacket packet) {
        try {
            packet.log();
            packet.handle(connection.getHandler());
        } catch (Exception e) {
            if (Log.getLevel().ordinal() >= LogLevels.DEBUG.ordinal()) {
                e.printStackTrace();
            }
        }
    }
}
