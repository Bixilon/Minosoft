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
import de.bixilon.minosoft.protocol.packets.clientbound.interfaces.PacketCompressionInterface;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketEncryptionRequest;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginSuccess;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    final Connection connection;
    final NettyNetwork nettyNetwork;

    public PacketDecoder(Connection connection, NettyNetwork nettyNetwork) {
        this.connection = connection;
        this.nettyNetwork = nettyNetwork;
    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) {
        byteBuf.markReaderIndex();
        int numRead = 0;
        int length = 0;
        byte read;
        do
        {
            if (!byteBuf.isReadable()) {
                byteBuf.resetReaderIndex();
                return;
            }
            read = byteBuf.readByte();
            int value = (read & 0b01111111);
            length |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            Log.protocol(String.format("Server sent us a to big packet (%d bytes > %d bytes)", length, ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE));
            byteBuf.skipBytes(length);
            return;
        }
        if (byteBuf.readableBytes() < length) {
            byteBuf.resetReaderIndex();
            return;
        }

        byte[] data;
        ByteBuf dataBuf = byteBuf.readBytes(length);
        if (dataBuf.hasArray()) {
            data = dataBuf.array();
        } else {
            data = new byte[length];
            dataBuf.getBytes(0, data);
        }

        if (nettyNetwork.getCompressionThreshold() >= 0) {
            // compression is enabled
            // check if there is a need to decompress it and if so, do it!
            InByteBuffer rawBuffer = new InByteBuffer(data, connection);
            int packetSize = rawBuffer.readVarInt();
            byte[] left = rawBuffer.readBytesLeft();
            if (packetSize == 0) {
                // no need
                data = left;
            } else {
                // need to decompress data
                data = Util.decompress(left, connection).readBytesLeft();
            }
        }

        InPacketBuffer inPacketBuffer = new InPacketBuffer(data, connection);
        Packets.Clientbound packet = null;
        try {
            packet = connection.getPacketByCommand(connection.getConnectionState(), inPacketBuffer.getCommand());
            if (packet == null) {
                Log.fatal(String.format("Packet mapping does not contain a packet with id 0x%x. The server sends bullshit or your versions.json broken!", inPacketBuffer.getCommand()));
                nettyNetwork.disconnect();
                throw new RuntimeException("Invalid packet 0x%x" + inPacketBuffer.getCommand());
            }
            Class<? extends ClientboundPacket> clazz = packet.getClazz();

            if (clazz == null) {
                Log.warn(String.format("[IN] Received unknown packet (id=0x%x, name=%s, length=%d, dataLength=%d, version=%s, state=%s)", inPacketBuffer.getCommand(), packet, inPacketBuffer.getLength(), inPacketBuffer.getBytesLeft(), connection.getVersion(), connection.getConnectionState()));
                return;
            }
            try {
                ClientboundPacket packetInstance = clazz.getConstructor().newInstance();
                boolean success = packetInstance.read(inPacketBuffer);
                if (inPacketBuffer.getBytesLeft() > 0 || !success) {
                    // warn not all data used
                    Log.warn(String.format("[IN] Could not parse packet %s (used=%d, available=%d, total=%d, success=%s)", packet, inPacketBuffer.getPosition(), inPacketBuffer.getBytesLeft(), inPacketBuffer.getLength(), success));
                    return;
                }

                //set special settings to avoid miss timing issues
                if (packetInstance instanceof PacketLoginSuccess) {
                    connection.setConnectionState(ConnectionStates.PLAY);
                } else if (packetInstance instanceof PacketCompressionInterface) {
                    nettyNetwork.compressionThreshold = ((PacketCompressionInterface) packetInstance).getThreshold();
                } else if (packetInstance instanceof PacketEncryptionRequest) {
                    // wait until response is ready
                    list.add(packetInstance);
                    return;
                }
                list.add(packetInstance);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                // safety first, but will not occur
                e.printStackTrace();
            }
        } catch (Exception e) {
            Log.protocol(String.format("An error occurred while parsing an packet (%s): %s", packet, e));
            if (Log.getLevel().ordinal() >= LogLevels.DEBUG.ordinal()) {
                e.printStackTrace();
            }
        }
    }
}
