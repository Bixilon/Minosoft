package de.bixilon.minosoft.protocol.packets.clientbound.handshaking;

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketStatusPong implements ClientboundPacket {

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        System.out.println("Pong received");
    }

    @Override
    public void log() {
        // ToDo
    }
}
