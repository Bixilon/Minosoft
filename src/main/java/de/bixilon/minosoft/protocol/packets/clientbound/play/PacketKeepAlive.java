package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketKeepAlive implements ClientboundPacket {
    int id;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                id = buffer.readInteger();
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Keep alive packet received (%s)", id));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public int getId() {
        return id;
    }
}
