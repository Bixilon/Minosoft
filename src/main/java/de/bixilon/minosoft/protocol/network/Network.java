package de.bixilon.minosoft.protocol.network;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
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

                    if (dIn.available() == 0) {
                        // nothing to receive
                        Util.sleep(1);
                        continue;
                    }
                    // everything sent for now, waiting for data
                    List<Byte> raw = new ArrayList<>();
                    byte[] buffer = new byte[1];
                    while (true) {
                        if (raw.size() > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
                            raw = null;
                            break;
                        }
                        if (dIn.available() == 0) {
                            // packet end
                            break;
                        }
                        dIn.readFully(buffer, 0, 1);
                        raw.add(buffer[0]);
                    }
                    if (raw == null || raw.size() == 0) {
                        // data was tto long, ...
                        continue;
                    }
                    // convert to array
                    byte[] in = new byte[raw.size()];
                    for (int i = 0; i < raw.size(); i++) {
                        in[i] = raw.get(i);
                    }
                    // add to queue
                    binQueueIn.add(in);
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

        // compressed data, makes packets to binary data
        // read data
        // safety first, but will not occur
        // sleep 1 ms
        Thread packetThread = new Thread(() -> {
            // compressed data, makes packets to binary data
            while (connection.getConnectionState() != ConnectionState.DISCONNECTING) {

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
                    byte[] raw = binQueueIn.get(0);
                    InPacketBuffer inPacketBuffer;
                    if (encryptionEnabled) {
                        // decrypt
                        byte[] decrypted = cipherDecrypt.update(raw);
                        inPacketBuffer = new InPacketBuffer(decrypted);
                    } else {
                        inPacketBuffer = new InPacketBuffer(raw);
                    }
                    Packets.Clientbound p = connection.getVersion().getProtocol().getPacketByCommand(connection.getConnectionState(), inPacketBuffer.getCommand());
                    Class<? extends ClientboundPacket> clazz = Protocol.getPacketByPacket(p);

                    if (clazz == null) {
                        Log.warn(String.format("[IN] Unknown packet with command 0x%x (%s)", inPacketBuffer.getCommand(), ((p != null) ? p.name() : "UNKNOWN")));
                        binQueueIn.remove(0);
                        continue;
                    }
                    try {
                        ClientboundPacket packet = clazz.getConstructor().newInstance();
                        packet.read(inPacketBuffer, connection.getVersion());
                        connection.handle(packet);
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        // safety first, but will not occur
                        e.printStackTrace();
                    }

                    binQueueIn.remove(0);
                }
                Util.sleep(1); // sleep 1 ms

            }
            connection.setConnectionState(ConnectionState.DISCONNECTED);
        });
        packetThread.start();

    }

    public void sendPacket(ServerboundPacket p) {
        queue.add(p);
    }

    public void enableEncryption(SecretKey secretKey) {
        Log.debug("Enabling encryption...");
        cipherEncrypt = CryptManager.createNetCipherInstance(Cipher.ENCRYPT_MODE, secretKey);
        cipherDecrypt = CryptManager.createNetCipherInstance(Cipher.DECRYPT_MODE, secretKey);
        encryptionEnabled = true;
        Log.debug("Encryption enabled!");
    }
}
