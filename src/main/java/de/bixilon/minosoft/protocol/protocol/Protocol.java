package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.clientbound.handshaking.PacketStatusPong;
import de.bixilon.minosoft.protocol.packets.clientbound.handshaking.PacketStatusResponse;

import java.util.HashMap;

public interface Protocol {
    HashMap<Packets.Clientbound, Class<? extends ClientboundPacket>> packetClassMapping = new HashMap<>();

    int getProtocolVersion();

    int getPacketCommand(Packets.Serverbound p);

    Packets.Clientbound getPacketByCommand(ConnectionState s, int command);

    static Class<? extends ClientboundPacket> getPacketByPacket(Packets.Clientbound p) {
        if (packetClassMapping.size() == 0) {
            // init
            initPacketClassMapping();
        }
        return packetClassMapping.get(p);
    }

    private static void initPacketClassMapping() {
        packetClassMapping.put(Packets.Clientbound.STATUS_RESPONSE, PacketStatusResponse.class);
        packetClassMapping.put(Packets.Clientbound.STATUS_PONG, PacketStatusPong.class);
    }
}