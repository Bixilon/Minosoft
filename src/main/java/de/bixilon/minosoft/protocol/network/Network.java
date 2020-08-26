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

package de.bixilon.minosoft.protocol.network;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.interfaces.PacketCompressionInterface;
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
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class Network {
    final Connection connection;
    final ArrayList<ServerboundPacket> queue = new ArrayList<>();
    Thread socketThread;
    int compressionThreshold = -1;
    Socket socket;
    OutputStream outputStream;
    InputStream cipherInputStream;
    InputStream inputStream;
    boolean encryptionEnabled = false;
    SecretKey secretKey;
    boolean connected;

    public Network(Connection connection) {
        this.connection = connection;
    }

    public void connect(ServerAddress address) {
        // wait for data or send until it should disconnect
        // first send, then receive
        // something to send it, send it
        // send, flush and remove
        // everything sent for now, waiting for data
        // add to queue
        // Could not connect
        socketThread = new Thread(() -> {
            try {
                socket = new Socket();
                socket.setSoTimeout(ProtocolDefinition.SOCKET_CONNECT_TIMEOUT);
                socket.connect(new InetSocketAddress(address.getHostname(), address.getPort()), ProtocolDefinition.SOCKET_CONNECT_TIMEOUT);
                connected = true;
                connection.setConnectionState(ConnectionStates.HANDSHAKING);
                socket.setKeepAlive(true);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                cipherInputStream = inputStream;

                while (connection.getConnectionState() != ConnectionStates.DISCONNECTING) {
                    // wait for data or send until it should disconnect
                    // first send, then receive

                    // check if still connected
                    if (!socket.isConnected() || socket.isClosed()) {
                        break;
                    }

                    while (queue.size() > 0) {
                        ServerboundPacket packet = queue.get(0);
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

                        outputStream.write(data);
                        outputStream.flush();
                        if (packet instanceof PacketEncryptionResponse) {
                            // enable encryption
                            secretKey = ((PacketEncryptionResponse) packet).getSecretKey();
                            enableEncryption(secretKey);
                        }
                    }

// everything sent for now, waiting for data
                    if (inputStream.available() > 0) { // available seems not to work in CipherInputStream
                        int numRead = 0;
                        int length = 0;
                        byte read;
                        do {
                            read = cipherInputStream.readNBytes(1)[0];
                            int value = (read & 0b01111111);
                            length |= (value << (7 * numRead));

                            numRead++;
                            if (numRead > 5) {
                                throw new RuntimeException("VarInt is too big");
                            }
                        } while ((read & 0b10000000) != 0);

                        byte[] data = cipherInputStream.readNBytes(length);

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
                                Log.fatal(String.format("Version packet enum does not contain a packet with id 0x%x. Your version.json is broken!", inPacketBuffer.getCommand()));
                                System.exit(1);
                            }
                            Class<? extends ClientboundPacket> clazz = packet.getClazz();

                            if (clazz == null) {
                                Log.warn(String.format("[IN] Received unknown packet (id=0x%x, name=%s, length=%d, dataLength=%d, version=%s, state=%s)", inPacketBuffer.getCommand(), packet, inPacketBuffer.getLength(), inPacketBuffer.getBytesLeft(), connection.getVersion(), connection.getConnectionState()));
                                continue;
                            }
                            try {
                                ClientboundPacket packetInstance = clazz.getConstructor().newInstance();
                                boolean success = packetInstance.read(inPacketBuffer);
                                if (inPacketBuffer.getBytesLeft() > 0 || !success) {
                                    // warn not all data used
                                    Log.warn(String.format("[IN] Could not parse packet %s (used=%d, available=%d, total=%d, success=%s)", packet, inPacketBuffer.getPosition(), inPacketBuffer.getBytesLeft(), inPacketBuffer.getLength(), success));
                                    continue;
                                }

                                //set special settings to avoid miss timing issues
                                if (packetInstance instanceof PacketLoginSuccess) {
                                    connection.setConnectionState(ConnectionStates.PLAY);
                                } else if (packetInstance instanceof PacketCompressionInterface) {
                                    compressionThreshold = ((PacketCompressionInterface) packetInstance).getThreshold();
                                }
                                connection.handle(packetInstance);
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                // safety first, but will not occur
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            Log.protocol(String.format("An error occurred while parsing an packet (%s): %s", packet, e));
                            e.printStackTrace();
                        }
                    }
                    Util.sleep(1);
                }
                socket.close();
                connected = false;
                connection.setConnectionState(ConnectionStates.DISCONNECTED);
            } catch (IOException e) {
                // Could not connect
                if (e instanceof SocketTimeoutException) {
                    connection.setConnectionState(ConnectionStates.FAILED);
                }
                e.printStackTrace();
            }
        });
        socketThread.setName(String.format("%d/Socket", connection.getConnectionId()));
        socketThread.start();
    }

    public void sendPacket(ServerboundPacket p) {
        queue.add(p);
        socketThread.interrupt();
    }

    public void enableEncryption(SecretKey secretKey) {
        Cipher cipherEncrypt = CryptManager.createNetCipherInstance(Cipher.ENCRYPT_MODE, secretKey);
        Cipher cipherDecrypt = CryptManager.createNetCipherInstance(Cipher.DECRYPT_MODE, secretKey);
        cipherInputStream = new CipherInputStream(inputStream, cipherDecrypt);
        outputStream = new CipherOutputStream(outputStream, cipherEncrypt);
        encryptionEnabled = true;
        Log.debug("Encryption enabled!");
    }

    public boolean isConnected() {
        return connected;
    }

    public void disconnect() {
        socketThread.interrupt();
    }
}
