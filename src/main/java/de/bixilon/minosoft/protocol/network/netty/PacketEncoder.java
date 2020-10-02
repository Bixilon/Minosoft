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
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketEncryptionResponse;
import de.bixilon.minosoft.protocol.protocol.OutByteBuffer;
import de.bixilon.minosoft.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<ServerboundPacket> {
    final Connection connection;
    final NettyNetwork nettyNetwork;

    public PacketEncoder(Connection connection, NettyNetwork nettyNetwork) {
        this.connection = connection;
        this.nettyNetwork = nettyNetwork;
    }

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, ServerboundPacket packet, ByteBuf byteBuf) throws Exception {
        packet.log();
        byte[] data = packet.write(connection).getOutBytes();
        if (nettyNetwork.getCompressionThreshold() >= 0) {
            // compression is enabled
            // check if there is a need to compress it and if so, do it!
            OutByteBuffer outRawBuffer = new OutByteBuffer(connection);
            if (data.length >= nettyNetwork.getCompressionThreshold()) {
                // compress it
                OutByteBuffer compressedBuffer = new OutByteBuffer(connection);
                byte[] compressed = Util.compress(data);
                compressedBuffer.writeVarInt(data.length);
                compressedBuffer.writeBytes(compressed);
                outRawBuffer.writeVarInt(compressedBuffer.getOutBytes().length);
                outRawBuffer.writeBytes(compressedBuffer.getOutBytes());
            } else {
                outRawBuffer.writeVarInt(data.length + 1); // 1 for the compressed length (0)
                outRawBuffer.writeVarInt(0);
                outRawBuffer.writeBytes(data);
            }
            data = outRawBuffer.getOutBytes();
        } else {
            // append packet length
            OutByteBuffer bufferWithLengthPrefix = new OutByteBuffer(connection);
            bufferWithLengthPrefix.writeVarInt(data.length);
            bufferWithLengthPrefix.writeBytes(data);
            data = bufferWithLengthPrefix.getOutBytes();
        }
        byteBuf.writeBytes(data);
        if (packet instanceof PacketEncryptionResponse) {
            // enable encryption
            nettyNetwork.enableEncryption(((PacketEncryptionResponse) packet).getSecretKey());
        }
    }
}
