package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketSetExperience implements ClientboundPacket {
    float bar;
    short level;
    short total;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                bar = buffer.readFloat();
                // maybe that should be an unsigned short, negative experience makes no sense. If you have negative exp the notchian client make strange things...
                level = buffer.readShort();
                total = buffer.readShort();
                break;
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Level update received. Now at %d levels, totally %d exp", level, total));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public float getBar() {
        return bar;
    }

    public short getLevel() {
        return level;
    }

    public short getTotal() {
        return total;
    }
}
