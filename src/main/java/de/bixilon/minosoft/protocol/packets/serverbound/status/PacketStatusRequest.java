package de.bixilon.minosoft.protocol.packets.serverbound.status;

import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketStatusRequest implements ServerboundPacket {

    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        // no version checking, is the same in all versions (1.7.x - 1.15.2)
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.STATUS_REQUEST));
        return buffer;
    }

    @Override
    public void log() {
        // ToDo
    }
}
