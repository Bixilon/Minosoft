package de.bixilon.minosoft.protocol.packets.clientbound.handshaking;

import de.bixilon.minosoft.objects.ServerListPing;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketStatusResponse implements ClientboundPacket {
    ServerListPing response;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        // no version checking, is the same in all versions (1.7.x - 1.15.2)
        response = new ServerListPing(buffer.readJson());
    }

    @Override
    public void log() {
        // ToDo
    }
}
