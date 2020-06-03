package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketTimeUpdate implements ClientboundPacket {
    long worldAge;
    long timeOfDay;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                worldAge = buffer.readLong();
                timeOfDay = buffer.readLong();
                break;
        }
        log();
    }

    @Override
    public void log() {
        Log.protocol(String.format("Time Update packet received. Time is now %st (total %st)", timeOfDay, worldAge));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

}
