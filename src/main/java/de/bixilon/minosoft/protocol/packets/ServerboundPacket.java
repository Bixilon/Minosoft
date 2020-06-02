package de.bixilon.minosoft.protocol.packets;

import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

// packet to send to server
public interface ServerboundPacket extends Packet {
    OutPacketBuffer write(ProtocolVersion v);
}
