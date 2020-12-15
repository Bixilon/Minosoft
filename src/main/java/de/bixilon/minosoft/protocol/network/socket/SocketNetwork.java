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

package de.bixilon.minosoft.protocol.network.socket;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.protocol.exceptions.PacketNotImplementedException;
import de.bixilon.minosoft.protocol.exceptions.PacketParseException;
import de.bixilon.minosoft.protocol.exceptions.PacketTooLongException;
import de.bixilon.minosoft.protocol.exceptions.UnknownPacketException;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.network.Network;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.interfaces.CompressionThresholdChange;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketEncryptionRequest;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginSuccess;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketEncryptionResponse;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.util.ServerAddress;
import de.bixilon.minosoft.util.Util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketNetwork implements Network {
    private final Connection connection;
    private final LinkedBlockingQueue<ServerboundPacket> queue = new LinkedBlockingQueue<>();
    private Thread socketReceiveThread;
    private Thread socketSendThread;
    private int compressionThreshold = -1;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Throwable lastException;

    public SocketNetwork(Connection connection) {
        this.connection = connection;
    }

    private static int readStreamVarInt(InputStream inputStream) throws IOException {
        int readCount = 0;
        int varInt = 0;
        int currentByte;
        do {
            currentByte = inputStream.read();
            if (currentByte == -1) {
                throw new SocketException("Socket closed");
            }
            int value = (currentByte & 0x7F);
            varInt |= (value << (7 * readCount));

            readCount++;
            if (readCount > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((currentByte & 0x80) != 0);
        return varInt;
    }

    @Override
    public void connect(ServerAddress address) {
        this.lastException = null;
        if (this.connection.isConnected() || this.connection.getConnectionState() == ConnectionStates.CONNECTING) {
            return;
        }
        this.socketReceiveThread = new Thread(() -> {
            try {
                this.socket = new Socket();
                this.socket.setSoTimeout(ProtocolDefinition.SOCKET_CONNECT_TIMEOUT);
                this.socket.connect(new InetSocketAddress(address.getHostname(), address.getPort()), ProtocolDefinition.SOCKET_CONNECT_TIMEOUT);
                // connected, use minecraft timeout
                this.socket.setSoTimeout(ProtocolDefinition.SOCKET_TIMEOUT);
                this.connection.setConnectionState(ConnectionStates.HANDSHAKING);
                this.socket.setKeepAlive(true);
                this.outputStream = this.socket.getOutputStream();
                this.inputStream = this.socket.getInputStream();


                initSendThread();

                this.socketReceiveThread.setName(String.format("%d/SocketR", this.connection.getConnectionId()));


                while (this.connection.getConnectionState() != ConnectionStates.DISCONNECTING) {
                    if (!this.socket.isConnected() || this.socket.isClosed()) {
                        break;
                    }
                    try {
                        handlePacket(receiveClientboundPacket(this.inputStream));
                    } catch (PacketParseException e) {
                        Log.printException(e, LogLevels.PROTOCOL);
                    }
                }
                disconnect();
            } catch (Exception e) {
                // Could not connect
                if (this.socketSendThread != null) {
                    this.socketSendThread.interrupt();
                }
                if (e instanceof SocketException && e.getMessage().equals("Socket closed")) {
                    return;
                }
                Log.printException(e, LogLevels.PROTOCOL);
                this.lastException = e;
                this.connection.setConnectionState(ConnectionStates.FAILED);
            }
        }, String.format("%d/Socket", this.connection.getConnectionId()));
        this.socketReceiveThread.start();
    }

    @Override
    public void sendPacket(ServerboundPacket packet) {
        this.queue.add(packet);
    }

    @Override
    public void disconnect() {
        this.connection.setConnectionState(ConnectionStates.DISCONNECTING);
        try {
            this.socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.socketReceiveThread.interrupt();
        this.socketSendThread.interrupt();
        this.connection.setConnectionState(ConnectionStates.DISCONNECTED);
    }

    @Override
    public Throwable getLastException() {
        return this.lastException;
    }

    private void initSendThread() {
        this.socketSendThread = new Thread(() -> {
            try {
                while (this.connection.getConnectionState() != ConnectionStates.DISCONNECTING) {
                    // wait for data or send until it should disconnect

                    // check if still connected
                    if (!this.socket.isConnected() || this.socket.isClosed()) {
                        break;
                    }

                    ServerboundPacket packet = this.queue.take();
                    packet.log();

                    this.outputStream.write(prepareServerboundPacket(packet));
                    this.outputStream.flush();
                    if (packet instanceof PacketEncryptionResponse packetEncryptionResponse) {
                        // enable encryption
                        enableEncryption(packetEncryptionResponse.getSecretKey());
                        // wake up other thread
                        this.socketReceiveThread.interrupt();
                    }
                }
            } catch (IOException | InterruptedException ignored) {
            }
        }, String.format("%d/SocketS", this.connection.getConnectionId()));
        this.socketSendThread.start();
    }

    private byte[] prepareServerboundPacket(ServerboundPacket packet) {
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

    private ClientboundPacket receiveClientboundPacket(InputStream inputStream) throws IOException, PacketParseException {
        int packetLength = readStreamVarInt(inputStream);

        if (packetLength > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            inputStream.skip(packetLength);
            throw new PacketTooLongException(packetLength);
        }

        byte[] bytes = this.inputStream.readNBytes(packetLength);
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
                throw new UnknownPacketException(String.format("Server sent us an unknown packet (id=0x%x, length=%d, data=%s)", data.getCommand(), packetLength, data.getBase64()));
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

    private void handlePacket(ClientboundPacket packet) {
        // set special settings to avoid miss timing issues
        if (packet instanceof PacketLoginSuccess) {
            this.connection.setConnectionState(ConnectionStates.PLAY);
        } else if (packet instanceof CompressionThresholdChange compressionPacket) {
            this.compressionThreshold = compressionPacket.getThreshold();
        } else if (packet instanceof PacketEncryptionRequest) {
            // wait until response is ready
            this.connection.handle(packet);
            try {
                Thread.sleep(Integer.MAX_VALUE);
            } catch (InterruptedException ignored) {
            }
            return;
        }
        this.connection.handle(packet);
    }

    private void enableEncryption(SecretKey secretKey) {
        this.inputStream = new CipherInputStream(this.inputStream, CryptManager.createNetCipherInstance(Cipher.DECRYPT_MODE, secretKey));
        this.outputStream = new CipherOutputStream(this.outputStream, CryptManager.createNetCipherInstance(Cipher.ENCRYPT_MODE, secretKey));
        Log.debug("Encryption enabled!");
    }
}
