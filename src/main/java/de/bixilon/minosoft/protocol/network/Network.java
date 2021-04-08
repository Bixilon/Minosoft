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

import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.protocol.exceptions.PacketNotImplementedException;
import de.bixilon.minosoft.protocol.exceptions.PacketParseException;
import de.bixilon.minosoft.protocol.exceptions.UnknownPacketException;
import de.bixilon.minosoft.protocol.network.connection.Connection;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.network.connection.StatusConnection;
import de.bixilon.minosoft.protocol.network.socket.BlockingSocketNetwork;
import de.bixilon.minosoft.protocol.packets.clientbound.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.StatusClientboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.AllServerboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.util.Pair;
import de.bixilon.minosoft.util.ServerAddress;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;

public abstract class Network {
    protected final Connection connection;
    protected int compressionThreshold = -1;
    protected Throwable lastException;

    protected Network(Connection connection) {
        this.connection = connection;
    }

    public static Network getNetworkInstance(Connection connection) {
        return new BlockingSocketNetwork(connection);
    }

    public abstract void connect(ServerAddress address);

    public abstract void sendPacket(ServerboundPacket packet);

    public abstract void disconnect();

    public Throwable getLastException() {
        return this.lastException;
    }


    protected Pair<PacketTypes.Clientbound, ClientboundPacket> receiveClientboundPacket(byte[] bytes) throws PacketParseException {
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
        var data = new InByteBuffer(bytes, this.connection);
        var packetId = data.readVarInt();

        PacketTypes.Clientbound packetType = null;

        try {
            packetType = this.connection.getPacketById(packetId);
            if (packetType == null) {
                throw new UnknownPacketException(String.format("Server sent us an unknown packet (id=0x%x, length=%d, data=%s)", packetId, bytes.length, data.getBase64()));
            }


            Version version = null;
            if (this.connection instanceof PlayConnection) {
                version = ((PlayConnection) this.connection).getVersion();
            }

            ClientboundPacket packet;
            try {
                if (packetType.getPlayFactory() != null) {
                    var playData = new PlayInByteBuffer(data.readBytesLeft(), ((PlayConnection) this.connection));
                    packet = packetType.getPlayFactory().invoke(playData);
                    if (playData.getBytesLeft() > 0) {
                        throw new PacketParseException(String.format("Could not parse packet %s (used=%d, available=%d, total=%d)", packetType, playData.getPosition(), playData.getBytesLeft(), playData.getLength()));
                    }
                    ((PlayClientboundPacket) packet).check(((PlayConnection) this.connection));
                } else if (packetType.getStatusFactory() != null) {
                    var statusData = new InByteBuffer(data);
                    packet = packetType.getStatusFactory().invoke(statusData);
                    if (statusData.getBytesLeft() > 0) {
                        throw new PacketParseException(String.format("Could not parse packet %s (used=%d, available=%d, total=%d)", packetType, statusData.getPosition(), statusData.getBytesLeft(), statusData.getLength()));
                    }
                    ((StatusClientboundPacket) packet).check((StatusConnection) this.connection);
                } else {
                    throw new PacketNotImplementedException(data, packetId, packetType, version, this.connection.getConnectionState());
                }


            } catch (Throwable exception) {
                var errorHandler = packetType.getErrorHandler();
                if (errorHandler != null) {
                    errorHandler.onError(this.connection);
                }

                throw exception;
            }
            return new Pair<>(packetType, packet);
        } catch (Throwable e) {
            Log.protocol(String.format("An error occurred while parsing a packet (%s): %s", packetType, e));
            if (this.connection.getConnectionState() == ConnectionStates.PLAY) {
                throw new PacketParseException(e);
            }
            throw new UnknownPacketException(e);
        }
    }

    protected byte[] prepareServerboundPacket(ServerboundPacket packet) {
        byte[] data;
        if (packet instanceof PlayServerboundPacket) {
            var buffer = new OutPlayByteBuffer((PlayConnection) this.connection);
            ((PlayServerboundPacket) packet).write(buffer);
            data = buffer.toByteArray();
        } else if (packet instanceof AllServerboundPacket) {
            var buffer = new OutByteBuffer(this.connection);
            ((AllServerboundPacket) packet).write(buffer);
            data = buffer.toByteArray();
        } else {
            throw new IllegalStateException();
        }

        OutByteBuffer outByteBuffer = new OutByteBuffer();
        outByteBuffer.writeVarInt(this.connection.getPacketId(PacketTypes.Serverbound.Companion.getPacketType(packet.getClass())));
        outByteBuffer.writeBytes(data);

        data = outByteBuffer.toByteArray();


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

    protected void handlePacket(PacketTypes.Clientbound packetType, ClientboundPacket packet) {
        this.connection.handle(packetType, packet);
    }

    public void setCompressionThreshold(int threshold) {
        this.compressionThreshold = threshold;
    }
}
