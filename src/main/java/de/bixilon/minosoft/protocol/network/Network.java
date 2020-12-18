/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.exceptions.PacketNotImplementedException;
import de.bixilon.minosoft.protocol.exceptions.PacketParseException;
import de.bixilon.minosoft.protocol.exceptions.UnknownPacketException;
import de.bixilon.minosoft.protocol.network.socket.NonBlockingSocketNetwork;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.interfaces.CompressionThresholdChange;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginSuccess;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.util.ServerAddress;
import de.bixilon.minosoft.util.Util;

public abstract class Network {
    protected final Connection connection;
    protected int compressionThreshold = -1;
    protected Throwable lastException;

    protected Network(Connection connection) {
        this.connection = connection;
    }

    public static Network getNetworkInstance(Connection connection) {
        return new NonBlockingSocketNetwork(connection);
    }

    public abstract void connect(ServerAddress address);

    public abstract void sendPacket(ServerboundPacket packet);

    public abstract void disconnect();

    public Throwable getLastException() {
        return this.lastException;
    }


    protected ClientboundPacket receiveClientboundPacket(byte[] bytes) throws PacketParseException {
        if (this.compressionThreshold >= 0) {
            // compression is enabled
            InByteBuffer rawData = new InByteBuffer(bytes, this.connection);
            int packetSize = rawData.readVarInt();
            bytes = rawData.readBytesLeft();
            if (packetSize > 0) {
                // need to decompress data
                bytes = Util.decompress(bytes);
            }
        }
        InPacketBuffer data = new InPacketBuffer(bytes, this.connection);

        Packets.Clientbound packetType = null;

        try {
            packetType = this.connection.getPacketByCommand(this.connection.getConnectionState(), data.getCommand());
            if (packetType == null) {
                throw new UnknownPacketException(String.format("Server sent us an unknown packet (id=0x%x, length=%d, data=%s)", data.getCommand(), bytes.length, data.getBase64()));
            }
            Class<? extends ClientboundPacket> clazz = packetType.getClazz();

            if (clazz == null) {
                throw new PacketNotImplementedException(data, packetType, this.connection);
            }
            ClientboundPacket packet = clazz.getConstructor().newInstance();
            boolean success = packet.read(data);
            if (data.getBytesLeft() > 0 || !success) {
                throw new PacketParseException(String.format("Could not parse packet %s (used=%d, available=%d, total=%d, success=%s)", packetType, data.getPosition(), data.getBytesLeft(), data.getLength(), success));
            }
            return packet;
        } catch (Throwable e) {
            Log.protocol(String.format("An error occurred while parsing a packet (%s): %s", packetType, e));
            if (this.connection.getConnectionState() == ConnectionStates.PLAY) {
                throw new PacketParseException(e);
            }
            throw new RuntimeException(e);
        }
    }

    protected byte[] prepareServerboundPacket(ServerboundPacket packet) {
        byte[] data = packet.write(this.connection).toByteArray();
        if (this.compressionThreshold >= 0) {
            // compression is enabled
            // check if there is a need to compress it and if so, do it!
            OutByteBuffer outRawBuffer = new OutByteBuffer(this.connection);
            if (data.length >= this.compressionThreshold) {
                // compress it
                OutByteBuffer lengthPrefixedBuffer = new OutByteBuffer(this.connection);
                byte[] compressed = Util.compress(data);
                lengthPrefixedBuffer.writeVarInt(data.length); // uncompressed length
                lengthPrefixedBuffer.writeBytes(compressed);
                outRawBuffer.prefixVarInt(lengthPrefixedBuffer.toByteArray().length); // length of total data is uncompressed length + compressed data
                outRawBuffer.writeBytes(lengthPrefixedBuffer.toByteArray()); // write all bytes
            } else {
                outRawBuffer.writeVarInt(data.length + 1); // 1 for the compressed length (0)
                outRawBuffer.writeVarInt(0); // data is uncompressed, compressed size is 0
                outRawBuffer.writeBytes(data);
            }
            data = outRawBuffer.toByteArray();
        } else {
            // append packet length
            OutByteBuffer bufferWithLengthPrefix = new OutByteBuffer(this.connection);
            bufferWithLengthPrefix.writeVarInt(data.length);
            bufferWithLengthPrefix.writeBytes(data);
            data = bufferWithLengthPrefix.toByteArray();
        }
        return data;
    }

    protected void handlePacket(ClientboundPacket packet) {
        // set special settings to avoid miss timing issues
        if (packet instanceof PacketLoginSuccess) {
            this.connection.setConnectionState(ConnectionStates.PLAY);
        } else if (packet instanceof CompressionThresholdChange compressionPacket) {
            this.compressionThreshold = compressionPacket.getThreshold();
        }
        this.connection.handle(packet);
    }

}
