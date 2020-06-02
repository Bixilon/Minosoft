package de.bixilon.minosoft.protocol.network;

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.ConnectionState;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Protocol;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.Util;

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
                        System.out.println(String.format("Sent packet (%s)", b[1]));
                    }

                    // everything sent for now, waiting for data

                    while (dIn.available() > 0) {
                            int numRead = 0;
                            int len = 0;
                            byte read;
                            do {
                                read = dIn.readByte();
                                int value = (read & 0b01111111);
                                len |= (value << (7 * numRead));

                                numRead++;
                                if (numRead > 5) {
                                    throw new RuntimeException("VarInt is too big");
                                }
                            } while ((read & 0b10000000) != 0);


                        byte[] in = dIn.readNBytes(len);
                        // add to queue
                        binQueueIn.add(in);

                    }
                    Util.sleep(1);

                }
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
                    binQueue.add(p.write(connection.getVersion()).getOutBytes());
                    queue.remove(0);
                }
                while (binQueueIn.size() > 0) {

                    // read data
                    InPacketBuffer inPacketBuffer = new InPacketBuffer(binQueueIn.get(0));
                    System.out.println("Received packet with command=" + inPacketBuffer.getCommand());
                    Class<? extends ClientboundPacket> clazz = Protocol.getPacketByPacket(connection.getVersion().getProtocol().getPacketByCommand(connection.getConnectionState(), inPacketBuffer.getCommand()));

                    if (clazz == null) {
                        System.out.println("Unknown packet received with command=" + inPacketBuffer.getCommand());
                        binQueueIn.remove(0);
                        continue;
                    }
                    try {
                        ClientboundPacket packet = clazz.getConstructor().newInstance();
                        packet.read(inPacketBuffer, connection.getVersion());
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

    public void disconnect() {
        connection.setConnectionState(ConnectionState.DISCONNECTING);

    }

    public void sendPacket(ServerboundPacket p) {
        queue.add(p);

    }
}
