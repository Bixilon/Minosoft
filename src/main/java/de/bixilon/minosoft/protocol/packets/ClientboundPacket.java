package de.bixilon.minosoft.protocol.packets;

import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

// packet to send to client
public interface ClientboundPacket extends Packet {
    void read(InPacketBuffer buffer, ProtocolVersion v);
}
