package de.bixilon.minosoft.protocol.packets.serverbound.handshaking;

import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketStatusPing implements ServerboundPacket {
    final Long ms;

    public PacketStatusPing(Long ms) {
        this.ms = ms;
    }

    public PacketStatusPing(int ms) {
        this.ms = (long) ms;
    }

    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        // no version checking, is the same in all versions (1.7.x - 1.15.2)
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.STATUS_PING));
        buffer.writeLong(ms);
        return buffer;
    }

    @Override
    public void log() {
        // ToDo
    }
}
