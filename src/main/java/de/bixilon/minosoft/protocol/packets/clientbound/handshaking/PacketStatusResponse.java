package de.bixilon.minosoft.protocol.packets.clientbound.handshaking;

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.*;

public class PacketStatusResponse implements ClientboundPacket {

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        // no version checking, is the same in all versions (1.7.x - 1.15.2)
        System.out.println(buffer.readString());
    }

    @Override
    public void log() {
        // ToDo
    }
}
