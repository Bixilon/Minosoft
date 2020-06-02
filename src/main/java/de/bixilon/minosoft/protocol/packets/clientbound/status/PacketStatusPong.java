package de.bixilon.minosoft.protocol.packets.clientbound.status;

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketStatusPong implements ClientboundPacket {
    Long id;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        this.id = buffer.readLong();
    }

    @Override
    public void log() {
        // ToDo
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public Long getID() {
        return this.id;
    }
}
