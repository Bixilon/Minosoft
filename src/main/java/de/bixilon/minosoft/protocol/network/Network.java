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
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginSuccess;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketChunkData;
import de.bixilon.minosoft.protocol.packets.clientbound.play.PacketLoginSetCompression;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketEncryptionResponse;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.util.Util;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Network {
    final Connection connection;
    final List<ServerboundPacket> queue;
    Thread socketThread;
    int compressionThreshold = -1;
    Socket socket;
    OutputStream outputStream;
    InputStream cipherInputStream;
    InputStream inputStream;
    boolean encryptionEnabled = false;
    SecretKey secretKey;
    boolean connected;

    public Network(Connection c) {
        this.connection = c;
        this.queue = new ArrayList<>();
    }

    public void connect() {
        // wait for data or send until it should disconnect
        // first send, then receive
        // something to send it, send it
        // send, flush and remove
        // everything sent for now, waiting for data
        // add to queue
        // Could not connect
        socketThread = new Thread(() -> {
            try {
                socket = new Socket(connection.getHost(), connection.getPort());
                connected = true;
                connection.setConnectionState(ConnectionState.HANDSHAKING);
                socket.setKeepAlive(true);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                cipherInputStream = inputStream;


                while (connection.getConnectionState() != ConnectionState.DISCONNECTING) {
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
                        byte[] data = packet.write(connection.getVersion()).getOutBytes();
                        if (compressionThreshold != -1) {
                            // compression is enabled
                            // check if there is a need to compress it and if so, do it!
                            OutByteBuffer outRawBuffer = new OutByteBuffer(connection.getVersion());
                            if (data.length >= compressionThreshold) {
                                // compress it
                                byte[] compressed = Util.compress(data);
                                OutByteBuffer buffer = new OutByteBuffer(connection.getVersion());
                                buffer.writeVarInt(compressed.length);
                                buffer.writeBytes(compressed);
                                outRawBuffer.writeVarInt(buffer.getBytes().size());
                                outRawBuffer.writeBytes(buffer.getOutBytes());
                            } else {
                                outRawBuffer.writeVarInt(data.length + 1); // 1 for the compressed length (0)
                                outRawBuffer.writeVarInt(0);
                                outRawBuffer.writeBytes(data);
                            }
                            data = outRawBuffer.getOutBytes();
                        } else {
                            // append packet length
                            OutByteBuffer bufferWithLengthPrefix = new OutByteBuffer(connection.getVersion());
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


                        if (compressionThreshold != -1) {
                            // compression is enabled
                            // check if there is a need to decompress it and if so, do it!
                            InByteBuffer rawBuffer = new InByteBuffer(data, connection.getVersion());
                            int packetSize = rawBuffer.readVarInt();
                            byte[] left = rawBuffer.readBytesLeft();
                            if (packetSize == 0) {
                                // no need
                                data = left;
                            } else {
                                // need to decompress data
                                data = Util.decompress(left, connection.getVersion()).readBytesLeft();
                            }
                        }

                        InPacketBuffer inPacketBuffer = new InPacketBuffer(data, connection.getVersion());
                        try {
                            Packets.Clientbound p = connection.getVersion().getProtocol().getPacketByCommand(connection.getConnectionState(), inPacketBuffer.getCommand());
                            Class<? extends ClientboundPacket> clazz = Protocol.getPacketByPacket(p);

                            if (clazz == null) {
                                Log.warn(String.format("[IN] Received unknown packet (id=0x%x, name=%s, length=%d, dataLength=%d, version=%s, state=%s)", inPacketBuffer.getCommand(), p, inPacketBuffer.getLength(), inPacketBuffer.getBytesLeft(), connection.getVersion(), connection.getConnectionState()));
                                continue;
                            }
                            try {
                                ClientboundPacket packet = clazz.getConstructor().newInstance();
                                boolean success;
                                if (packet instanceof PacketChunkData) {
                                    // this packets need to know if we are in an dimension with skylight...
                                    success = ((PacketChunkData) (packet)).read(inPacketBuffer, connection.getPlayer().getWorld().getDimension());
                                } else {
                                    success = packet.read(inPacketBuffer);
                                }
                                if (inPacketBuffer.getBytesLeft() > 0 || !success) {
                                    // warn not all data used
                                    Log.warn(String.format("[IN] Could not parse packet %s (used=%d, available=%d, total=%d, success=%s)", p, inPacketBuffer.getPosition(), inPacketBuffer.getBytesLeft(), inPacketBuffer.getLength(), success));

                                    continue;
                                }

                                if (packet instanceof PacketLoginSuccess) {
                                    // login was okay, setting play status to avoid miss timing issues
                                    connection.setConnectionState(ConnectionState.PLAY);
                                } else if (packet instanceof PacketLoginSetCompression) {
                                    // instantly set compression. because handling is to slow...
                                    compressionThreshold = ((PacketLoginSetCompression) packet).getThreshold();
                                }
                                connection.handle(packet);
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                                // safety first, but will not occur
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            Log.protocol("Received broken packet!");
                            e.printStackTrace();
                        }
                    }
                    Util.sleep(1);
                }
                socket.close();
                connected = false;
                connection.setConnectionState(ConnectionState.DISCONNECTED);
            } catch (IOException e) {
                // Could not connect
                connection.setConnectionState(ConnectionState.DISCONNECTED);
                e.printStackTrace();
            }
        });
        socketThread.setName("Socket-Thread");
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
