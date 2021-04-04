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

import de.bixilon.minosoft.protocol.exceptions.PacketParseException;
import de.bixilon.minosoft.protocol.exceptions.PacketTooLongException;
import de.bixilon.minosoft.protocol.network.Network;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.serverbound.ServerboundPacket;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketEncryptionResponse;
import de.bixilon.minosoft.protocol.protocol.ConnectionStates;
import de.bixilon.minosoft.protocol.protocol.CryptManager;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.ServerAddress;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.logging.LogLevels;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.LinkedList;

public class NonBlockingSocketNetwork extends Network {
    private final PlayConnection connection;
    private final LinkedList<ServerboundPacket> queue = new LinkedList<>();
    private SocketChannel socketChannel;
    private Cipher decryptCipher;
    private Cipher encryptCipher;

    public NonBlockingSocketNetwork(PlayConnection connection) {
        super(connection);
        this.connection = connection;
    }

    @Override
    public void connect(ServerAddress address) {
        if (this.connection.isConnected() || this.connection.getConnectionState() == ConnectionStates.CONNECTING) {
            return;
        }
        this.lastException = null;
        this.connection.setConnectionState(ConnectionStates.CONNECTING);
        new Thread(() -> {
            try {
                InetSocketAddress socketAddress = new InetSocketAddress(InetAddress.getByName(address.getHostname()), address.getPort());

                Selector selector = Selector.open();
                this.socketChannel = SocketChannel.open();
                this.socketChannel.configureBlocking(false);
                this.socketChannel.connect(socketAddress);
                this.socketChannel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                while (this.socketChannel.isConnectionPending()) {
                    this.socketChannel.finishConnect();
                }
                this.connection.setConnectionState(ConnectionStates.HANDSHAKING);

                int readCount = 0;
                int packetLength = 0;
                ByteBuffer currentPacketBuffer = null;
                ByteBuffer receiveLengthBuffer = ByteBuffer.allocate(1);


                while (this.connection.getConnectionState() != ConnectionStates.DISCONNECTING && this.connection.getConnectionState() != ConnectionStates.DISCONNECTED) {
                    while (!this.queue.isEmpty()) {
                        ServerboundPacket packet = this.queue.getFirst();
                        this.queue.removeFirst();
                        ByteBuffer sendBuffer = ByteBuffer.wrap(encryptData(prepareServerboundPacket(packet)));

                        while (sendBuffer.hasRemaining()) {
                            this.socketChannel.write(sendBuffer);
                        }

                        if (packet instanceof PacketEncryptionResponse packetEncryptionResponse) {
                            // enable encryption
                            enableEncryption(packetEncryptionResponse.getSecretKey());
                        }

                    }
                    int bytesRead = 1;
                    if (currentPacketBuffer == null) {
                        while (bytesRead > 0) {
                            bytesRead = this.socketChannel.read(receiveLengthBuffer);

                            if (bytesRead == -1) {
                                disconnect();
                            }

                            if (bytesRead > 0) {
                                receiveLengthBuffer.flip();
                                int currentByte = receiveLengthBuffer.get();
                                currentByte = decryptByte((byte) currentByte);
                                int value = (currentByte & 0x7F);
                                packetLength |= (value << (7 * readCount));

                                readCount++;
                                if (readCount > 5) {
                                    throw new RuntimeException("VarInt is too big");
                                }
                                receiveLengthBuffer.clear();
                                if ((currentByte & 0x80) == 0) {
                                    if (packetLength > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
                                        throw new PacketTooLongException(packetLength);
                                    }

                                    currentPacketBuffer = ByteBuffer.allocate(packetLength);
                                    readCount = 0;
                                    packetLength = 0;
                                    break;
                                }
                            }
                        }
                    } else {
                        while (bytesRead > 0) {
                            bytesRead = this.socketChannel.read(currentPacketBuffer);
                            if (bytesRead == -1) {
                                disconnect();
                            }
                        }
                        if (!currentPacketBuffer.hasRemaining()) {
                            currentPacketBuffer.flip();
                            try {
                                var typeAndPacket = receiveClientboundPacket(decryptData(currentPacketBuffer.array()));
                                handlePacket(typeAndPacket.getKey(), typeAndPacket.getValue());
                            } catch (PacketParseException e) {
                                Log.printException(e, LogLevels.PROTOCOL);
                            }
                            currentPacketBuffer.clear();
                            currentPacketBuffer = null;
                        }
                    }
                    synchronized (this) {
                        // ToDo: how to remove this without using 100% cpu of one core??? This makes the ping worse and is just somehow stupid
                        try {
                            wait(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } catch (IOException | PacketTooLongException e) {
                if (e instanceof SocketException && e.getMessage().equals("Socket closed")) {
                    return;
                }
                Log.printException(e, LogLevels.PROTOCOL);
                this.lastException = e;
                this.connection.setConnectionState(ConnectionStates.FAILED);
            }
        }, String.format("Network#%d", this.connection.getConnectionId())).start();
    }

    @Override
    public void sendPacket(ServerboundPacket packet) {
        this.queue.add(packet);
    }

    @Override
    public void disconnect() {
        if (!this.connection.isConnected()) {
            return;
        }
        this.connection.setConnectionState(ConnectionStates.DISCONNECTING);
        this.queue.clear();
        try {
            this.socketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.connection.setConnectionState(ConnectionStates.DISCONNECTED);
    }

    private byte[] encryptData(byte[] data) {
        if (this.encryptCipher == null) {
            return data;
        }
        return this.encryptCipher.update(data);
    }

    private byte decryptByte(byte data) {
        if (this.decryptCipher == null) {
            return data;
        }
        return this.decryptCipher.update(new byte[]{data})[0];
    }

    private byte[] decryptData(byte[] data) {
        if (this.decryptCipher == null) {
            return data;
        }
        return this.decryptCipher.update(data);
    }

    protected void enableEncryption(SecretKey secretKey) {
        this.decryptCipher = CryptManager.createNetCipherInstance(Cipher.DECRYPT_MODE, secretKey);
        this.encryptCipher = CryptManager.createNetCipherInstance(Cipher.ENCRYPT_MODE, secretKey);
        Log.debug("Encryption enabled!");
    }
}
