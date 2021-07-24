/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.network;

import de.bixilon.minosoft.data.registries.versions.Version;
import de.bixilon.minosoft.protocol.exceptions.PacketNotImplementedException;
import de.bixilon.minosoft.protocol.exceptions.PacketParseException;
import de.bixilon.minosoft.protocol.exceptions.UnknownPacketException;
import de.bixilon.minosoft.protocol.network.connection.Connection;
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection;
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection;
import de.bixilon.minosoft.protocol.network.socket.BlockingSocketNetwork;
import de.bixilon.minosoft.protocol.packets.c2s.AllC2SPacket;
import de.bixilon.minosoft.protocol.packets.c2s.C2SPacket;
import de.bixilon.minosoft.protocol.packets.c2s.PlayC2SPacket;
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket;
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket;
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.util.Pair;
import de.bixilon.minosoft.util.ServerAddress;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;
import kotlin.jvm.Synchronized;

@Deprecated
public abstract class Network {
    protected final Connection connection;
    protected int compressionThreshold = -1;

    protected Network(Connection connection) {
        this.connection = connection;
    }

    public static Network getNetworkInstance(Connection connection) {
        return new BlockingSocketNetwork(connection);
    }

    public abstract void connect(ServerAddress address);

    public abstract void sendPacket(C2SPacket packet);

    @Synchronized
    public abstract void disconnect();

    protected Pair<PacketTypes.S2C, S2CPacket> receiveS2CPacket(byte[] bytes) throws PacketParseException {
        if (this.compressionThreshold >= 0) {
            // compression is enabled
            InByteBuffer rawData = new InByteBuffer(bytes, this.connection);
            int packetSize = rawData.readVarInt();
            bytes = rawData.readRest();
            if (packetSize > 0) {
                // need to decompress data
                bytes = Util.decompress(bytes);
            }
        }
        var data = new InByteBuffer(bytes, this.connection);
        var packetId = data.readVarInt();

        PacketTypes.S2C packetType = null;

        try {
            packetType = this.connection.getPacketById(packetId);
            if (packetType == null) {
                throw new UnknownPacketException(String.format("Server sent us an unknown packet (id=0x%x, length=%d, data=%s)", packetId, bytes.length, data.getBase64()));
            }


            Version version = null;
            if (this.connection instanceof PlayConnection) {
                version = ((PlayConnection) this.connection).getVersion();
            }

            S2CPacket packet;
            try {
                if (packetType.getPlayFactory() != null) {
                    var playData = new PlayInByteBuffer(data.readRest(), ((PlayConnection) this.connection));
                    packet = packetType.getPlayFactory().invoke(playData);
                    if (playData.getBytesLeft() > 0) {
                        throw new PacketParseException(String.format("Could not parse packet %s (used=%d, available=%d, total=%d)", packetType, playData.getPointer(), playData.getBytesLeft(), playData.getSize()));
                    }
                    ((PlayS2CPacket) packet).check(((PlayConnection) this.connection));
                } else if (packetType.getStatusFactory() != null) {
                    var statusData = new InByteBuffer(data);
                    packet = packetType.getStatusFactory().invoke(statusData);
                    if (statusData.getBytesLeft() > 0) {
                        throw new PacketParseException(String.format("Could not parse packet %s (used=%d, available=%d, total=%d)", packetType, statusData.getPointer(), statusData.getBytesLeft(), statusData.getSize()));
                    }
                    ((StatusS2CPacket) packet).check((StatusConnection) this.connection);
                } else {
                    throw new PacketNotImplementedException(data, packetId, packetType, version, this.connection.getProtocolState());
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
            if (this.connection.getProtocolState() == ProtocolStates.PLAY) {
                throw new PacketParseException(e);
            }
            throw new UnknownPacketException(e);
        }
    }

    protected byte[] prepareC2SPacket(C2SPacket packet) {
        byte[] data;
        if (packet instanceof PlayC2SPacket) {
            var buffer = new PlayOutByteBuffer((PlayConnection) this.connection);
            ((PlayC2SPacket) packet).write(buffer);
            data = buffer.toByteArray();
        } else if (packet instanceof AllC2SPacket) {
            var buffer = new OutByteBuffer(this.connection);
            ((AllC2SPacket) packet).write(buffer);
            data = buffer.toByteArray();
        } else {
            throw new IllegalStateException();
        }

        OutByteBuffer outByteBuffer = new OutByteBuffer();
        outByteBuffer.writeVarInt(this.connection.getPacketId(PacketTypes.C2S.Companion.getPacketType(packet.getClass())));
        outByteBuffer.writeUnprefixedByteArray(data);

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
                lengthPrefixedBuffer.writeUnprefixedByteArray(compressed);
                outRawBuffer.prefixVarInt(lengthPrefixedBuffer.toByteArray().length); // length of total data is uncompressed length + compressed data
                outRawBuffer.writeUnprefixedByteArray(lengthPrefixedBuffer.toByteArray()); // write all bytes
            } else {
                outRawBuffer.writeVarInt(data.length + 1); // 1 for the compressed length (0)
                outRawBuffer.writeVarInt(0); // data is uncompressed, compressed size is 0
                outRawBuffer.writeUnprefixedByteArray(data);
            }
            data = outRawBuffer.toByteArray();
        } else {
            // append packet length
            OutByteBuffer bufferWithLengthPrefix = new OutByteBuffer(this.connection);
            bufferWithLengthPrefix.writeVarInt(data.length);
            bufferWithLengthPrefix.writeUnprefixedByteArray(data);
            data = bufferWithLengthPrefix.toByteArray();
        }
        return data;
    }

    protected void handlePacket(PacketTypes.S2C packetType, S2CPacket packet) {
        this.connection.handle(packetType, packet);
    }

    public void setCompressionThreshold(int threshold) {
        this.compressionThreshold = threshold;
    }

    public abstract void pauseSending(boolean pause);

    public abstract void pauseReceiving(boolean pause);
}
