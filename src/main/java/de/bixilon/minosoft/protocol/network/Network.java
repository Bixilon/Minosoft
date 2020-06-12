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
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketEncryptionResponse;
import de.bixilon.minosoft.protocol.protocol.*;
import de.bixilon.minosoft.util.Util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Network {
    private final Connection connection;
    private final List<ServerboundPacket> queue;
    private final List<byte[]> binQueue;
    private final List<byte[]> binQueueIn;
    private Socket socket;
    private boolean encryptionEnabled = false;
    private Cipher cipherEncrypt;
    private Cipher cipherDecrypt;
    private Thread packetThread;

    public Network(Connection c) {
        this.connection = c;
        this.queue = new ArrayList<>();
        this.binQueue = new ArrayList<>();
        this.binQueueIn = new ArrayList<>();
    }

    public void connect() {
        // wait for data or send until it should disconnect
        // first send, then receive
        // something to send it, send it
        // send, flush and remove
        // everything sent for now, waiting for data
        // add to queue
        // Could not connect
        Thread socketThread = new Thread(() -> {
            try {
                socket = new Socket(connection.getHost(), connection.getPort());
                connection.setConnectionState(ConnectionState.HANDSHAKING);
                socket.setKeepAlive(true);
                DataOutputStream dOut = new DataOutputStream(socket.getOutputStream());
                DataInputStream dIn = new DataInputStream(socket.getInputStream());


                while (connection.getConnectionState() != ConnectionState.DISCONNECTING) {
                    // wait for data or send until it should disconnect
                    // first send, then receive


                    while (binQueue.size() > 0) {
                        // something to send it, send it
                        byte[] b = binQueue.get(0);

                        // send, flush and remove
                        dOut.write(b);
                        dOut.flush();
                        binQueue.remove(0);
                    }

                    // everything sent for now, waiting for data

                    if (dIn.available() > 0) {
                        int numRead = 0;
                        int length = 0;
                        byte read;
                        do {
                            read = dIn.readByte();
                            int value = (read & 0b01111111);
                            length |= (value << (7 * numRead));

                            numRead++;
                            if (numRead > 5) {
                                throw new RuntimeException("VarInt is too big");
                            }
                        } while ((read & 0b10000000) != 0);

                        byte[] raw = dIn.readNBytes(length);
                        binQueueIn.add(raw);
                        packetThread.interrupt();
                    }
                    Util.sleep(1);

                }
                connection.setConnectionState(ConnectionState.DISCONNECTED);
            } catch (IOException e) {
                // Could not connect
                connection.setConnectionState(ConnectionState.DISCONNECTED);
                e.printStackTrace();
            }
        });
        socketThread.start();
    }

    public void startPacketThread() {
        // compressed data, makes packets to binary data
        // read data
        // safety first, but will not occur
        // sleep 1 ms
        packetThread = new Thread(() -> {
            // compressed data, makes packets to binary data
            while (connection.getConnectionState() != ConnectionState.DISCONNECTED) {

                while (queue.size() > 0) {
                    ServerboundPacket p = queue.get(0);
                    byte[] raw = p.write(connection.getVersion()).getOutBytes();
                    if (encryptionEnabled) {
                        // encrypt
                        byte[] encrypted = cipherEncrypt.update(raw);
                        binQueue.add(encrypted);
                    } else {
                        if (p instanceof PacketEncryptionResponse) {
                            // enable encryption
                            enableEncryption(((PacketEncryptionResponse) p).getSecretKey());
                        }
                        binQueue.add(raw);
                    }
                    queue.remove(0);
                }
                while (binQueueIn.size() > 0) {

                    // read data
                    byte[] decrypted = binQueueIn.get(0);
                    InPacketBuffer inPacketBuffer;
                    if (encryptionEnabled) {
                        // decrypt
                        decrypted = cipherDecrypt.update(decrypted);
                    }
                    try {
                        inPacketBuffer = new InPacketBuffer(decrypted);
                        Packets.Clientbound p = connection.getVersion().getProtocol().getPacketByCommand(connection.getConnectionState(), inPacketBuffer.getCommand());
                        Class<? extends ClientboundPacket> clazz = Protocol.getPacketByPacket(p);

                        if (clazz == null) {
                            Log.warn(String.format("[IN] Unknown packet with command 0x%x (%s) and %d bytes of data", inPacketBuffer.getCommand(), ((p != null) ? p.name() : "UNKNOWN"), inPacketBuffer.getBytesLeft()));
                            binQueueIn.remove(0);
                            continue;
                        }
                        try {
                            ClientboundPacket packet = clazz.getConstructor().newInstance();
                            packet.read(inPacketBuffer, connection.getVersion());
                            if (inPacketBuffer.getBytesLeft() > 0 && p != Packets.Clientbound.PLAY_ENTITY_METADATA) { // entity meta data uses mostly all data, but this happens in the handling thread
                                // warn not all data used
                                Log.protocol(String.format("[IN] Packet %s did not used all bytes sent", ((p != null) ? p.name() : "UNKNOWN")));
                            }

                                if (packet instanceof PacketLoginSuccess) {
                                    // login was okay, setting play status to avoid miss timing issues
                                    connection.setConnectionState(ConnectionState.PLAY);
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

                    binQueueIn.remove(0);
                }
                try {
                    // sleep, wait for an interrupt from other thread
                    Thread.sleep(100);
                } catch (InterruptedException ignored) {
                }

            }
        });
        packetThread.start();
    }

    public void sendPacket(ServerboundPacket p) {
        queue.add(p);
        packetThread.interrupt();
    }

    public void enableEncryption(SecretKey secretKey) {
        Log.debug("Enabling encryption...");
        cipherEncrypt = CryptManager.createNetCipherInstance(Cipher.ENCRYPT_MODE, secretKey);
        cipherDecrypt = CryptManager.createNetCipherInstance(Cipher.DECRYPT_MODE, secretKey);
        encryptionEnabled = true;
        Log.debug("Encryption enabled!");
    }
}
