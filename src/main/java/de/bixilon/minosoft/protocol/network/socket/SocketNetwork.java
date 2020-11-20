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
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.network.Network;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.interfaces.PacketCompressionInterface;
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
import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.LinkedBlockingQueue;

public class SocketNetwork implements Network {
    final Connection connection;
    final LinkedBlockingQueue<ServerboundPacket> queue = new LinkedBlockingQueue<>();
    Thread socketRThread;
    Thread socketSThread;
    int compressionThreshold = -1;
    Socket socket;
    OutputStream outputStream;
    InputStream inputStream;
    Exception lastException;

    public SocketNetwork(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void connect(ServerAddress address) {
        lastException = null;
        // check if we are already connected or try to connect
        if (connection.isConnected() || connection.getConnectionState() == ConnectionStates.CONNECTING) {
            return;
        }
        // wait for data or send until it should disconnect
        // first send, then receive
        // something to send it, send it
        // send, flush and remove
        // everything sent for now, waiting for data
        // add to queue
        // Could not connect
        socketRThread = new Thread(() -> {
            try {
                socket = new Socket();
                socket.setSoTimeout(ProtocolDefinition.SOCKET_CONNECT_TIMEOUT);
                socket.connect(new InetSocketAddress(address.getHostname(), address.getPort()), ProtocolDefinition.SOCKET_CONNECT_TIMEOUT);
                // connected, use minecraft timeout
                socket.setSoTimeout(ProtocolDefinition.SOCKET_TIMEOUT);
                connection.setConnectionState(ConnectionStates.HANDSHAKING);
                socket.setKeepAlive(true);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();

                socketRThread.setName(String.format("%d/SocketR", connection.getConnectionId()));

                socketSThread = new Thread(() -> {
                    try {
                        while (connection.getConnectionState() != ConnectionStates.DISCONNECTING) {
                            // wait for data or send until it should disconnect

                            // check if still connected
                            if (!socket.isConnected() || socket.isClosed()) {
                                break;
                            }

                            ServerboundPacket packet = queue.take();
                            packet.log();
                            queue.remove(packet);
                            byte[] data = packet.write(connection).getOutBytes();
                            if (compressionThreshold >= 0) {
                                // compression is enabled
                                // check if there is a need to compress it and if so, do it!
                                OutByteBuffer outRawBuffer = new OutByteBuffer(connection);
                                if (data.length >= compressionThreshold) {
                                    // compress it
                                    OutByteBuffer compressedBuffer = new OutByteBuffer(connection);
                                    byte[] compressed = Util.compress(data);
                                    compressedBuffer.writeVarInt(data.length);
                                    compressedBuffer.writeBytes(compressed);
                                    outRawBuffer.prefixVarInt(compressedBuffer.getOutBytes().length);
                                } else {
                                    outRawBuffer.prefixVarInt(0);
                                    outRawBuffer.writeVarInt(data.length + 1); // 1 for the compressed length (0)
                                }
                                data = outRawBuffer.getOutBytes();
                            } else {
                                // append packet length
                                OutByteBuffer bufferWithLengthPrefix = new OutByteBuffer(connection);
                                bufferWithLengthPrefix.writeVarInt(data.length);
                                bufferWithLengthPrefix.writeBytes(data);
                                data = bufferWithLengthPrefix.getOutBytes();
                            }

                            outputStream.write(data);
                            outputStream.flush();
                            if (packet instanceof PacketEncryptionResponse packetEncryptionResponse) {
                                // enable encryption
                                enableEncryption(packetEncryptionResponse.getSecretKey());
                                // wake up other thread
                                socketRThread.interrupt();
                            }
                        }
                    } catch (IOException | InterruptedException ignored) {
                    }
                }, String.format("%d/SocketS", connection.getConnectionId()));
                socketSThread.start();

                while (connection.getConnectionState() != ConnectionStates.DISCONNECTING) {
                    // wait for data or send until it should disconnect
                    // first send, then receive

                    // check if still connected
                    if (!socket.isConnected() || socket.isClosed()) {
                        break;
                    }

                    // everything sent for now, waiting for data
                    int numRead = 0;
                    int length = 0;
                    int read;
                    do {
                        read = inputStream.read();
                        if (read == -1) {
                            disconnect();
                            return;
                        }
                        int value = (read & 0x7F);
                        length |= (value << (7 * numRead));

                        numRead++;
                        if (numRead > 5) {
                            throw new RuntimeException("VarInt is too big");
                        }
                    } while ((read & 0x80) != 0);
                    if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
                        Log.protocol(String.format("Server sent us a to big packet (%d bytes > %d bytes)", length, ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE));
                        inputStream.skip(length);
                        continue;
                    }
                    byte[] data = inputStream.readNBytes(length);

                    if (compressionThreshold >= 0) {
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
                            disconnect();
                            lastException = new UnknownPacketException(String.format("Invalid packet 0x%x", inPacketBuffer.getCommand()));
                            throw lastException;
                        }
                        Class<? extends ClientboundPacket> clazz = packet.getClazz();

                        if (clazz == null) {
                            throw new UnknownPacketException(String.format("Unknown packet (id=0x%x, name=%s, length=%d, dataLength=%d, version=%s, state=%s)", inPacketBuffer.getCommand(), packet, inPacketBuffer.getLength(), inPacketBuffer.getBytesLeft(), connection.getVersion(), connection.getConnectionState()));
                        }
                        try {
                            ClientboundPacket packetInstance = clazz.getConstructor().newInstance();
                            boolean success = packetInstance.read(inPacketBuffer);
                            if (inPacketBuffer.getBytesLeft() > 0 || !success) {
                                throw new PacketParseException(String.format("Could not parse packet %s (used=%d, available=%d, total=%d, success=%s)", packet, inPacketBuffer.getPosition(), inPacketBuffer.getBytesLeft(), inPacketBuffer.getLength(), success));
                            }

                            //set special settings to avoid miss timing issues
                            if (packetInstance instanceof PacketLoginSuccess) {
                                connection.setConnectionState(ConnectionStates.PLAY);
                            } else if (packetInstance instanceof PacketCompressionInterface compressionPacket) {
                                compressionThreshold = compressionPacket.getThreshold();
                            } else if (packetInstance instanceof PacketEncryptionRequest) {
                                // wait until response is ready
                                connection.handle(packetInstance);
                                try {
                                    Thread.sleep(Integer.MAX_VALUE);
                                } catch (InterruptedException ignored) {
                                }
                                continue;
                            }
                            connection.handle(packetInstance);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                            // safety first, but will not occur
                            e.printStackTrace();
                        }
                    } catch (Exception e) {
                        Log.printException(e, LogLevels.DEBUG);
                        Log.protocol(String.format("An error occurred while parsing an packet (%s): %s", packet, e));
                    }
                }
                disconnect();
            } catch (IOException e) {
                // Could not connect
                Log.printException(e, LogLevels.DEBUG);
                if (socketSThread != null) {
                    socketSThread.interrupt();
                }
                if (e instanceof SocketException && e.getMessage().equals("Socket closed")) {
                    return;
                }
                lastException = e;
                connection.setConnectionState(ConnectionStates.FAILED);
            }
        }, String.format("%d/Socket", connection.getConnectionId()));
        socketRThread.start();
    }

    @Override
    public void sendPacket(ServerboundPacket p) {
        queue.add(p);
    }

    @Override
    public void disconnect() {
        connection.setConnectionState(ConnectionStates.DISCONNECTING);
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        socketRThread.interrupt();
        socketSThread.interrupt();
        connection.setConnectionState(ConnectionStates.DISCONNECTED);
    }

    @Override
    public Exception getLastException() {
        return lastException;
    }

    private void enableEncryption(SecretKey secretKey) {
        Cipher cipherEncrypt = CryptManager.createNetCipherInstance(Cipher.ENCRYPT_MODE, secretKey);
        Cipher cipherDecrypt = CryptManager.createNetCipherInstance(Cipher.DECRYPT_MODE, secretKey);
        inputStream = new CipherInputStream(inputStream, cipherDecrypt);
        outputStream = new CipherOutputStream(outputStream, cipherEncrypt);
        Log.debug("Encryption enabled!");
    }
}
