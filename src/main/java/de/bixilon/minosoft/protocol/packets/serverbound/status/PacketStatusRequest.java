package de.bixilon.minosoft.protocol.packets.serverbound.status;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketStatusRequest implements ServerboundPacket {

    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        log();
        // no version checking, is the same in all versions (1.7.x - 1.15.2)
        return new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.STATUS_REQUEST));
    }

    @Override
    public void log() {
        Log.protocol("Sending status request packet");
    }
}
